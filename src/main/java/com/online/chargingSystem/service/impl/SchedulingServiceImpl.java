package com.online.chargingSystem.service.impl;

import com.online.chargingSystem.entity.ChargingPile;
import com.online.chargingSystem.entity.ChargingRequest;
import com.online.chargingSystem.entity.User;
import com.online.chargingSystem.entity.WaitingQueue;
import com.online.chargingSystem.entity.enums.ChargingPileStatus;
import com.online.chargingSystem.entity.enums.ChargingPileType;
import com.online.chargingSystem.entity.enums.RequestStatus;
import com.online.chargingSystem.mapper.ChargingPileMapper;
import com.online.chargingSystem.mapper.ChargingRequestMapper;
import com.online.chargingSystem.mapper.UserMapper;
import com.online.chargingSystem.service.SchedulingService;
import com.online.chargingSystem.service.ChargingPileService;
import com.online.chargingSystem.service.ChargingPileQueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Lazy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.online.chargingSystem.entity.enums.ChargingPileType.FAST;
import static com.online.chargingSystem.entity.enums.RequestStatus.WAITING_IN_WAITING_AREA;

@Service
@EnableScheduling
public class SchedulingServiceImpl implements SchedulingService {

    @Autowired
    private ChargingRequestMapper chargingRequestMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WaitingQueue waitingQueue;
    @Autowired
    @Lazy
    private ChargingPileService chargingPileService;
    @Autowired
    private ChargingPileQueueService chargingPileQueueService;
    @Autowired
    private ChargingPileMapper chargingPileMapper;

    // 是否开启叫号
    private volatile boolean callNumberEnabled = true;

    // 用于生成快充和慢充的排队序号
    private final AtomicInteger fastQueueCounter = new AtomicInteger(0);
    private final AtomicInteger slowQueueCounter = new AtomicInteger(0);

    // 处理充电请求
    @Override
    @Transactional
    public ChargingRequest handleChargingRequest(Long userId, Double requestAmount, ChargingPileType mode) {
        // 构造充电请求
        ChargingRequest request = new ChargingRequest();
        request.setUserId(userId);
        request.setAmount(requestAmount);
        request.setMode(mode);
        request.setStatus(WAITING_IN_WAITING_AREA);
        request.setRequestTime(LocalDateTime.now());
        request.setQueueJoinTime(LocalDateTime.now());

        // 生成排队号码
        String queueNumber = generateQueueNumber(mode);
        request.setQueueNumber(queueNumber);
        System.out.println("充电请求：=======================");
        System.out.println(request);
        // 保存充电请求
        chargingRequestMapper.insert(request);

        //将请求添加到队列
        switch (mode){
            case FAST:
                waitingQueue.getFastQueue().offer(request.getId());
                break;
            case SLOW:
                waitingQueue.getSlowQueue().offer(request.getId());
                break;
        }
        System.out.println("** 请求 " + request.getId() + " 入队，排队号为 " + request.getQueueNumber() + " **");

        return request;
    }

    /**
     * 定时检查是否可以叫号
     * 每5秒检查一次
     */
//    @Scheduled(fixedRate = 5000)
    @Scheduled(fixedRateString = "${scheduling.task.interval}")
    public void scheduledCallNumber() {
        try {
            // 检查是否可以叫号
            if (callNumberEnabled && canCallNumber()) {
                System.out.println("\n=== 开始叫号检查 ===");
                handleCallNumber();
                System.out.println("=== 叫号检查完成 ===\n");
            }
        } catch (Exception e) {
            System.err.println("叫号过程发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 叫号逻辑
    @Override
    @Transactional
    public void handleCallNumber() {
        // 检查是否可以叫号
        if (!(callNumberEnabled && canCallNumber())) {
            return;
        }

        System.out.println("\n=== 开始批量叫号 ===");
        printWaitingQueues();

        // 处理快充队列
        processQueue(waitingQueue.getFastQueue(), ChargingPileType.FAST);

        // 处理慢充队列
        processQueue(waitingQueue.getSlowQueue(), ChargingPileType.SLOW);

        System.out.println("=== 批量叫号完成 ===");

        chargingPileQueueService.printPileQueues();
    }

    // 判断是否能叫号
    @Override
    public boolean canCallNumber() {
        // 检查充电区是否有空位
        boolean hasAvailable = chargingPileQueueService.hasPileVacancy();
        if (!hasAvailable) {
            System.out.println("充电区已满，无法叫号");
        }
        return hasAvailable;
    }

    // 处理指定类型的等待队列
    @Transactional
    public boolean processQueue(Queue<Long> queue, ChargingPileType type) {
        System.out.println("处理" + (type == FAST ? "快充" : "慢充") + "队列...");

        boolean allRequestsProcessed = false;  // 该队列中的请求是否都调度完毕

        while (!queue.isEmpty()) {
            // 获取队列中的第一个请求
            Long requestId = queue.peek();
            ChargingRequest request = chargingRequestMapper.findById(requestId);
            if (request == null) {
                queue.poll(); // 移除无效请求
                continue;
            }

            // 获取可用充电桩
            List<String> availablePiles = chargingPileQueueService.getAvailableAndInUsePiles(type);
            if (availablePiles.isEmpty()) {
                System.out.println("没有可用的" + (type == FAST ? "快充" : "慢充") + "充电桩，停止处理");
                break;
            }

            // 为请求分配最优充电桩
            String optimalPileId = assignOptimalPile(request);
            if (optimalPileId == null) {
                System.out.println("无法为请求 " + requestId + " 分配充电桩，停止处理");
                break;
            }

            // 从等候区队列中移除
            queue.poll();

            // 将请求添加到充电桩队列
            chargingPileQueueService.addToQueue(optimalPileId, requestId);

            // 更新请求信息
            request.setChargingPileId(optimalPileId);
            request.setStatus(RequestStatus.WAITING_IN_CHARGING_AREA);
            chargingRequestMapper.update(request);

            System.out.println("请求 " + requestId + " 已分配至充电桩 " + optimalPileId + "\n");

            // 如果充电桩空闲，直接开始充电
            if(chargingPileMapper.findById(optimalPileId).getStatus() == ChargingPileStatus.AVAILABLE){
                chargingPileService.startCharging(requestId, optimalPileId);
            }
        }

        if(queue.isEmpty()){
            allRequestsProcessed = true;
            System.out.println((type == FAST ? "快充" : "慢充") + "队列处理完成\n");
        }else{
            System.out.println("还有未能调度的请求");
        }
        return allRequestsProcessed;
    }

    // 获取用户最新一次充电请求
    private ChargingRequest getLatestRequest(long userId){
        List<ChargingRequest> request = chargingRequestMapper.findByUserId(userId);
        if(request.isEmpty()){
            return null;
        }
        return request.get(request.size() - 1);
    }

    // 等候区是否已满，0未满，1满
    @Override
    public boolean isWaitingAreaFull() {
        return waitingQueue.getFastQueue().size() + waitingQueue.getSlowQueue().size() >= 6;
    }

    // 请求充电量是否合理，0不合理，1合理
    @Override
    public boolean isRequestAmountValid(Long userId, Double requestAmount) {
        User user = userMapper.selectById(userId);
        return requestAmount > 0 && requestAmount <= user.getBatteryCapacity();
    }

    // 生成排队号码
    private String generateQueueNumber(ChargingPileType mode) {
        return switch (mode) {
            case FAST -> "F" + fastQueueCounter.incrementAndGet();
            case SLOW -> "T" + slowQueueCounter.incrementAndGet();
        };
    }

    // 获取排队号码
    @Override
    public String getQueueNumber(Long userId) {
        ChargingRequest request = getLatestRequest(userId);
        assert request != null;
        return request.getQueueNumber();
    }

    // 获取本充电模式下前车等待数量
    @Override
    public int getAheadNumber(Long userId) {
        ChargingRequest request = getLatestRequest(userId);
        assert request != null;

        // 获取对应的等待队列
        Queue<Long> targetQueue = request.getMode() == ChargingPileType.FAST ? 
            waitingQueue.getFastQueue() : waitingQueue.getSlowQueue();

        switch (request.getStatus()){
            case WAITING_IN_WAITING_AREA:{
                // 计算等候区前车数量
                int waitingAreaCount = 0;
                for (Long requestId : targetQueue) {
                    if (requestId.equals(request.getId())) {
                        break;
                    }
                    waitingAreaCount++;
                }

                // 计算充电区等待车辆数量
                int chargingAreaCount = 0;
                List<String> piles = chargingPileQueueService.getAvailableAndInUsePiles(request.getMode());
                for (String pileId : piles) {
                    Queue<Long> pileQueue = chargingPileQueueService.getQueueRequests(pileId);
                    if (pileQueue != null && !pileQueue.isEmpty()) {
                        chargingAreaCount += pileQueue.size() - 1;
                    }
                }

                return waitingAreaCount + chargingAreaCount;
            }
            case WAITING_IN_CHARGING_AREA:{
                return 1;
            }
            default:{
                return 0;
            }
        }
    }

    // 修改充电模式
    @Override
    @Transactional
    public void modifyChargingMode(Long userId, ChargingPileType mode) {
        ChargingRequest request = getLatestRequest(userId);
        assert request != null;
        request.setMode(mode);

        // 重新生成排队号
        request.setQueueNumber(generateQueueNumber(mode));
        chargingRequestMapper.update(request);

        // 排到队列的末尾
        Queue<Long> fastQueue = waitingQueue.getFastQueue();
        Queue<Long> slowQueue = waitingQueue.getSlowQueue();
        switch (mode){
            case FAST:
                moveToAnotherQueue(slowQueue, fastQueue, request.getId());
                break;
            case SLOW:
                moveToAnotherQueue(fastQueue, slowQueue, request.getId());
                break;
        }

        // 查看队列状态
        printWaitingQueues();
    }

    // 修改充电量
    @Override
    @Transactional
    public void modifyChargingAmount(Long userId, Double requestAmount) {
        ChargingRequest request = getLatestRequest(userId);
        assert request != null;
        request.setAmount(requestAmount);
        chargingRequestMapper.update(request);
    }

    // 将请求移动到另一个队列
    // queue2是target queue
    @Transactional
    protected void moveToAnotherQueue(Queue<Long> queue1, Queue<Long> queue2, long target) {
        // 创建一个临时队列用于存储非目标元素
        Queue<Long> tempQueue = new LinkedList<>();
        while (!queue1.isEmpty()) {
            long element = queue1.poll();
            if (element == target) {
                queue2.add(element);
            } else {
                tempQueue.add(element);
            }
        }

        while (!tempQueue.isEmpty()) {
            queue1.add(tempQueue.poll());
        }
    }

    // 取消充电并回到等候区重新排队
    @Override
    @Transactional
    public ChargingRequest cancelAndRequeue(Long userId) {
        System.out.println("当前队列：");
        printWaitingQueues();

        ChargingRequest request = getLatestRequest(userId);
        assert request != null;

        cancel(userId);

        // 重新生成新请求并入等候区
        ChargingRequest newrequest = handleChargingRequest(userId, request.getAmount(), request.getMode());
        System.out.println("重新入队后的队列：");
        printWaitingQueues();
        
        return newrequest;
    }

    // 取消充电并离开
    @Override
    @Transactional
    public void cancel(Long userId) {
        ChargingRequest request = getLatestRequest(userId);
        assert request != null;

        // 1. 从等候区队列移除
        waitingQueue.getFastQueue().remove(request.getId());
        waitingQueue.getSlowQueue().remove(request.getId());
        
        // 2. 从充电桩队列移除（如果在充电区）
        if (request.getChargingPileId() != null) {
            chargingPileQueueService.removeFromQueue(request.getChargingPileId(), request.getId());
        }
        
        // 3. 设置原请求为已取消
        request.setStatus(RequestStatus.CANCELED);
        chargingRequestMapper.update(request);
    }

    // 充电完成处理
    @Override
    @Transactional
    public boolean handleChargingComplete(Long userId) {
        ChargingRequest request = getLatestRequest(userId);
        assert request != null;
        long requestId = request.getId();
        String pileId = request.getChargingPileId();

        System.out.println("处理充电完成请求: " + requestId);
        System.out.println("充电桩: " + pileId);

        // 调用结束充电接口
        if(!chargingPileService.endCharging(request.getUserId(), pileId)){
            return false;
        }

        // 从充电桩队列中移除
        chargingPileQueueService.removeFromQueue(pileId, requestId);

        System.out.println("充电完成处理完成");
        int currentQueueSize = chargingPileQueueService.getQueueSize(pileId);
        System.out.println("当前充电桩队列大小: " + currentQueueSize);

        // 队列不为空，则下一个请求开始充电
        if(currentQueueSize > 0){
            long nextRequestId = chargingPileQueueService.getQueueHead(pileId);
            System.out.println("下一个请求：" + nextRequestId);
            chargingPileService.startCharging(nextRequestId, pileId);
        }

        return true;
    }

    // 计算等候时间
    @Override
    public double calculateWaitingTime(String pileId) {
        // 获取充电桩队列中的所有请求
        Queue<Long> queue = chargingPileQueueService.getQueueRequests(pileId);
        if (queue == null || queue.isEmpty()) {
            return 0;
        }
        
        // 计算所有请求的充电时间总和
        return queue.stream()
                .map(requestId -> chargingRequestMapper.findById(requestId))
                .filter(Objects::nonNull)
                .mapToDouble(request -> calculateChargingTime(request, pileId))
                .sum();
    }

    // 计算充电时间
    @Override
    public double calculateChargingTime(ChargingRequest request, String pileId) {
        // 获取充电桩功率
        double chargingPower = chargingPileService.getPileStatus(pileId).getChargingPower();
        
        // 计算充电时间（小时）= 充电量/充电功率
        return request.getAmount() / chargingPower;
    }

    // 选择最优充电桩
    @Override
    public String assignOptimalPile(ChargingRequest request) {
        System.out.println("- 处理充电请求 " + request.getId() + " ...");
        // 获取所有可用的充电桩
        List<String> availablePiles = chargingPileQueueService.getAvailableAndInUsePiles(request.getMode());
        
        System.out.println("可用充电桩列表: " + availablePiles);
        
        if (availablePiles.isEmpty()) {
            System.out.println("没有可用的" + request.getMode() + "充电桩");
            return null;
        }
        
        // 计算每个充电桩的总时间（等待时间 + 充电时间）
        String optimalPileId = null;
        double minTotalTime = Double.MAX_VALUE;
        
        for (String pileId : availablePiles) {
            // 检查充电桩队列是否已满
            if (chargingPileQueueService.isQueueFull(pileId)) {
                System.out.println("充电桩 " + pileId + " 队列已满，跳过");
                continue;
            }
            
            double waitingTime = calculateWaitingTime(pileId);
            double chargingTime = calculateChargingTime(request, pileId);
            double totalTime = waitingTime + chargingTime;
            
            System.out.println("充电桩 " + pileId + " 预计总时间: " + totalTime + "小时 (等待: " + waitingTime + "小时, 充电: " + chargingTime + "小时)");
            
            if (totalTime < minTotalTime) {
                minTotalTime = totalTime;
                optimalPileId = pileId;
            }
        }
        
        if (optimalPileId != null) {
            System.out.println("选择最优充电桩: " + optimalPileId + ", 预计总时间: " + minTotalTime + "小时");
        }
        
        return optimalPileId;
    }

    // 调试用：打印当前等待区队列
    private void printWaitingQueues(){
        Queue<Long> fastQueue = waitingQueue.getFastQueue();
        Queue<Long> slowQueue = waitingQueue.getSlowQueue();
        System.out.println("快队列: " + fastQueue);
        System.out.println("慢队列: " + slowQueue);
    }

    // 判断是否在等候区
    @Override
    public boolean isInWaitingArea(Long userId) {
        ChargingRequest request = getLatestRequest(userId);
        assert request != null;
        return request.getStatus() == RequestStatus.WAITING_IN_WAITING_AREA;
    }

    // 判断是否在充电等候区
    @Override
    public boolean isInChargingArea(Long userId) {
        ChargingRequest request = getLatestRequest(userId);
        assert request != null;
        return request.getStatus() == RequestStatus.WAITING_IN_CHARGING_AREA;
    }

    // 判断是否在充电
    @Override
    public boolean isCharging(Long userId) {
        ChargingRequest request = getLatestRequest(userId);
        assert request != null;
        return request.getStatus() == RequestStatus.CHARGING;
    }

    // 开启叫号
    @Override
    public void startCallNumber() {
        System.out.println("...开始叫号...");
        callNumberEnabled = true;
    }

    // 停止叫号
    @Override
    public void stopCallNumber() {
        System.out.println("...停止叫号...");
        callNumberEnabled = false;
    }

    @Override
    public List<ChargingRequest> getWaitingAreaQueue() {
        // 获取快充和慢充队列中的所有请求ID
        List<Long> allRequestIds = new ArrayList<>();
        allRequestIds.addAll(waitingQueue.getFastQueue());
        allRequestIds.addAll(waitingQueue.getSlowQueue());

        // 按请求ID排序
        allRequestIds.sort(Long::compareTo);

        // 获取所有请求的详细信息
        return allRequestIds.stream()
                .map(chargingRequestMapper::findById)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
} 