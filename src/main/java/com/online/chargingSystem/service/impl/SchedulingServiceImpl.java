package com.online.chargingSystem.service.impl;

import com.online.chargingSystem.entity.ChargingRequest;
import com.online.chargingSystem.entity.User;
import com.online.chargingSystem.entity.WaitingQueue;
import com.online.chargingSystem.entity.enums.ChargingPileType;
import com.online.chargingSystem.entity.enums.RequestStatus;
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

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import static com.online.chargingSystem.entity.enums.ChargingPileType.FAST;
import static com.online.chargingSystem.entity.enums.RequestStatus.WAITING_IN_WAITING_AREA;
import static com.online.chargingSystem.entity.enums.RequestStatus.CHARGING;
import static com.online.chargingSystem.entity.enums.RequestStatus.COMPLETED;

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
    private ChargingPileService chargingPileService;
    @Autowired
    private ChargingPileQueueService chargingPileQueueService;

    // 是否开启叫号
    private volatile boolean callNumberEnabled = true;

    // 用于生成快充和慢充的排队序号
    // AtomicInteger是一个提供原子操作的整数类
    private final AtomicInteger fastQueueCounter = new AtomicInteger(0);
    private final AtomicInteger slowQueueCounter = new AtomicInteger(0);

    // 获取用户最新一次充电请求
    private ChargingRequest getLatestRequest(long userId){
        List<ChargingRequest> request = chargingRequestMapper.findByUserId(userId);
        if(request.isEmpty()){
            return null;
        }
        return request.get(request.size() - 1);
    }

    @Override
    public void startCallNumber() {
        callNumberEnabled = true;
    }

    @Override
    public void stopCallNumber() {
        callNumberEnabled = false;
    }

    @Override
    public boolean isWaitingAreaFull() {
        return waitingQueue.getFastQueue().size() + waitingQueue.getSlowQueue().size() >= 6;
    }

    @Override
    public boolean isRequestAmountValid(Long userId, Double requestAmount) {
        User user = userMapper.selectById(userId);
        return requestAmount > 0 && requestAmount <= user.getBatteryCapacity();
    }

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
     * 生成排队号码
     * @param mode 充电模式（FAST/SLOW）
     * @return 排队号码（F1/T1格式）
     */
    private String generateQueueNumber(ChargingPileType mode) {
        switch (mode){
            case FAST:
                return "F" + fastQueueCounter.incrementAndGet();
            case SLOW:
                return "T" + slowQueueCounter.incrementAndGet();
        }
        return null;
    }

    // 获取排队号码
    @Override
    public String getQueueNumber(Long userId) {
        ChargingRequest request = getLatestRequest(userId);
        assert request != null;
        return request.getQueueNumber();
    }

    @Override
    public int getAheadNumber(Long userId) {
        ChargingRequest request = getLatestRequest(userId);
        assert request != null;
        
        // 获取对应的等待队列
        Queue<Long> targetQueue = request.getMode() == ChargingPileType.FAST ? 
            waitingQueue.getFastQueue() : waitingQueue.getSlowQueue();
            
        // 计算前车数量
        int aheadCount = 0;
        for (Long requestId : targetQueue) {
            if (requestId.equals(request.getId())) {
                break;
            }
            aheadCount++;
        }
        
        return aheadCount;
    }

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

    @Override
    public void modifyChargingAmount(Long userId, Double requestAmount) {
        ChargingRequest request = getLatestRequest(userId);
        assert request != null;
        request.setAmount(requestAmount);
        chargingRequestMapper.update(request);
    }

    // queue2是target queue
    private static void moveToAnotherQueue(Queue<Long> queue1, Queue<Long> queue2, long target) {
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


    @Override
    public boolean isInWaitingArea(Long userId) {
        ChargingRequest request = getLatestRequest(userId);
        assert request != null;
        return request.getStatus() == WAITING_IN_WAITING_AREA;
    }

    @Override
    @Transactional
    public void cancelAndRequeue(Long userId) {
        System.out.println("当前队列：");
        printWaitingQueues();

        ChargingRequest request = getLatestRequest(userId);
        assert request != null;

        cancel(userId);

        // 重新生成新请求并入等候区
        handleChargingRequest(userId, request.getAmount(), request.getMode());
        System.out.println("重新入队后的队列：");
        printWaitingQueues();
    }

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


    // 辅助方法：从等候区和所有充电区队列移除
    private void removeFromAllQueues(Long requestId, ChargingPileType mode) {
        // 1. 等候区
        waitingQueue.getFastQueue().remove(requestId);
        waitingQueue.getSlowQueue().remove(requestId);
        // 2. 充电区（如有统一管理类可遍历所有充电桩队列，这里假设无，留接口）
        // TODO: 如果有充电区队列管理类，在此补充遍历所有充电桩队列并remove(requestId)
    }

    // 调试用：打印当前等待区队列
    private void printWaitingQueues(){
        Queue<Long> fastQueue = waitingQueue.getFastQueue();
        Queue<Long> slowQueue = waitingQueue.getSlowQueue();
        System.out.println("快队列: " + fastQueue);
        System.out.println("慢队列: " + slowQueue);
    }

    @Override
    public ChargingRequest getNextRequest() {
        // 获取快充和慢充队列的第一个请求
        Long fastRequestId = waitingQueue.getFastQueue().peek();
        Long slowRequestId = waitingQueue.getSlowQueue().peek();
        
        System.out.println("当前等待队列状态：");
        printWaitingQueues();
        
        if (fastRequestId == null && slowRequestId == null) {
            System.out.println("等待队列为空，没有需要叫号的请求");
            return null;
        }
        
        // 优先处理快充请求
        if (fastRequestId != null) {
            ChargingRequest request = chargingRequestMapper.findById(fastRequestId);
            System.out.println("准备叫号快充请求: " + request.getId() + ", 排队号: " + request.getQueueNumber());
            return request;
        }
        
        ChargingRequest request = chargingRequestMapper.findById(slowRequestId);
        System.out.println("准备叫号慢充请求: " + request.getId() + ", 排队号: " + request.getQueueNumber());
        return request;
    }

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

    @Override
    public double calculateChargingTime(ChargingRequest request, String pileId) {
        // 获取充电桩功率
        double chargingPower = chargingPileService.getPileStatus(pileId).getChargingPower();
        
        // 计算充电时间（小时）= 充电量/充电功率
        return request.getAmount() / chargingPower;
    }

    @Override
    public String assignOptimalPile(ChargingRequest request) {
        printWaitingQueues();
        System.out.println("- 处理充电请求 " + request.getId() + " ...");
        // 获取所有可用的充电桩
        List<String> availablePiles = chargingPileQueueService.getAvailableAndInUsePiles(request.getMode());
        
        System.out.println("可用充电桩列表: " + availablePiles);
        
        if (availablePiles.isEmpty()) {
            System.out.println("没有可用的" + request.getMode() + "充电桩");
            return null;
        }
        
        // 计算每个充电桩的总时间（等待时间 + 充电时间）
        String optimalPile = null;
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
                optimalPile = pileId;
            }
        }
        
        if (optimalPile != null) {
            System.out.println("选择最优充电桩: " + optimalPile + ", 预计总时间: " + minTotalTime + "小时");
        }
        
        return optimalPile;
    }

    @Override
    @Transactional
    public void handleCallNumber() {
        // 检查是否可以叫号
        if (!(callNumberEnabled && canCallNumber())) {
            return;
        }
        
        System.out.println("\n=== 开始批量叫号 ===");
        
        // 处理快充队列
        processQueue(waitingQueue.getFastQueue(), ChargingPileType.FAST);
        
        // 处理慢充队列
        processQueue(waitingQueue.getSlowQueue(), ChargingPileType.SLOW);
        
        System.out.println("=== 批量叫号完成 ===");

        chargingPileQueueService.printPileQueues();
    }
    
    /**
     * 处理指定类型的等待队列
     * @param queue 等待队列
     * @param type 充电桩类型
     */
    private void processQueue(Queue<Long> queue, ChargingPileType type) {
        System.out.println("处理" + (type == FAST ? "快充" : "慢充") + "队列...");
        
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
            String optimalPile = assignOptimalPile(request);
            if (optimalPile == null) {
                System.out.println("无法为请求 " + request.getId() + " 分配充电桩，停止处理");
                break;
            }
            
            // 更新请求状态
//            request.setStatus(CHARGING); chj
//            request.setChargingPileId(optimalPile);
//            chargingRequestMapper.update(request);
            
            // 从等候区队列中移除
            queue.poll();
            
            // 将请求添加到充电桩队列
            chargingPileQueueService.addToQueue(optimalPile, request.getId());
            
            System.out.println("请求 " + request.getId() + " 已分配至充电桩 " + optimalPile + "\n");
            // TODO:调用开始充电接口
            chargingPileService.startCharging(request.getId(), optimalPile);
        }
        
        System.out.println((type == FAST ? "快充" : "慢充") + "队列处理完成\n");
    }
    
    // 充电完成处理
    @Override
    @Transactional
    public void handleChargingComplete(Long requestId) {
        ChargingRequest request = chargingRequestMapper.findById(requestId);
        if (request == null) {
            System.out.println("未找到请求: " + requestId);
            return;
        }
        
        System.out.println("处理充电完成请求: " + requestId);
        System.out.println("充电桩: " + request.getChargingPileId());
        
        // 更新请求状态为已完成
        request.setStatus(COMPLETED);
        chargingRequestMapper.update(request);
        
        // 从充电桩队列中移除
        chargingPileQueueService.removeFromQueue(request.getChargingPileId(), requestId);
        
        // 更新充电桩状态
        chargingPileService.startChargingPile(request.getChargingPileId());

        // TODO:调用结束充电接口
        chargingPileService.endCharging(request.getUserId(), request.getChargingPileId());
        
        System.out.println("充电完成处理完成");
        System.out.println("当前充电桩队列大小: " + chargingPileQueueService.getQueueSize(request.getChargingPileId()));
    }

    /**
     * 定时检查是否可以叫号
     * 每5秒检查一次
     */
    @Scheduled(fixedRate = 5000)

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

    @Override
    public boolean canCallNumber() {
        // 检查是否有充电桩故障
        List<String> faultPiles = chargingPileQueueService.getFaultPiles();
        if (!faultPiles.isEmpty()) {
            System.out.println("存在故障充电桩，无法叫号。故障充电桩: " + faultPiles);
            return false;
        }

        // 检查充电区是否有空位
        boolean hasAvailable = chargingPileQueueService.hasPileVacancy();
        if (!hasAvailable) {
            System.out.println("充电区已满，无法叫号");
        }
        return hasAvailable;
    }
} 