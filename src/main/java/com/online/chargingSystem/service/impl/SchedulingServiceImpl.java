package com.online.chargingSystem.service.impl;

import com.online.chargingSystem.entity.ChargingRequest;
import com.online.chargingSystem.entity.User;
import com.online.chargingSystem.entity.WaitingQueue;
import com.online.chargingSystem.entity.enums.ChargingPileType;
import com.online.chargingSystem.entity.enums.RequestStatus;
import com.online.chargingSystem.mapper.ChargingRequestMapper;
import com.online.chargingSystem.mapper.UserMapper;
import com.online.chargingSystem.service.SchedulingService;
import org.springframework.beans.factory.annotation.Autowired;
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

@Service
public class SchedulingServiceImpl implements SchedulingService {

    @Autowired
    private ChargingRequestMapper chargingRequestMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WaitingQueue waitingQueue;

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
        System.out.println("请求" + request.getId() + "入队，排队号为" + request.getQueueNumber());

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

        // 1. 从等候区或充电区队列移除
        removeFromAllQueues(request.getId(), request.getMode());
        System.out.println("移除后的队列：");
        printWaitingQueues();

        // 2. 设置原请求为已取消
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

} 