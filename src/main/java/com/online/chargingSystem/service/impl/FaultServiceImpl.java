package com.online.chargingSystem.service.impl;

import com.online.chargingSystem.entity.ChargingPile;
import com.online.chargingSystem.entity.ChargingRequest;
import com.online.chargingSystem.entity.enums.ChargingPileStatus;
import com.online.chargingSystem.entity.enums.ChargingPileType;
import com.online.chargingSystem.entity.enums.RequestStatus;
import com.online.chargingSystem.mapper.ChargingPileMapper;
import com.online.chargingSystem.mapper.ChargingRequestMapper;
import com.online.chargingSystem.service.ChargingPileQueueService;
import com.online.chargingSystem.service.ChargingPileService;
import com.online.chargingSystem.service.FaultService;
import com.online.chargingSystem.service.SchedulingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;
import java.util.LinkedList;

@Service
public class FaultServiceImpl implements FaultService {

    @Autowired
    private ChargingRequestMapper chargingRequestMapper;
    @Autowired
    private ChargingPileMapper chargingPileMapper;
    @Autowired
    private ChargingPileService chargingPileService;
    @Autowired
    private ChargingPileQueueService chargingPileQueueService;
    @Autowired
    private SchedulingService schedulingService;

    // 是否开启故障处理定时任务
    private volatile boolean faultHandlingEnabled = false;

    // 是否开启故障恢复处理定时任务
    private volatile boolean faultRecoveryEnabled = false;

    // 当前故障处理策略
    private String currentFaultStrategy;

    // 是否已经收集和排序过请求
    private volatile boolean hasCollectedRequests = false;

    // 恢复的充电桩Id
    private volatile String recoveryPileId = null;

    @Override
    @Transactional
    public void handlePileFault(String pileId, String strategy) {
        if (strategy == null || (!"PRIORITY".equals(strategy) && !"TIME_ORDER".equals(strategy))) {
            throw new IllegalArgumentException("无效的故障处理策略: " + strategy);
        }

        System.out.println("\n=== 处理充电桩 " + pileId + " 故障 ===");

        // 处理充电桩
        chargingPileService.reportFault(pileId, "故障", "短路");

        // 1. 停止叫号服务
        schedulingService.stopCallNumber();

        // 2. 设置故障处理策略
        currentFaultStrategy = strategy;
        
        // 3. 重置收集标志
        hasCollectedRequests = false;
        
        // 4. 开启故障处理定时任务
        faultHandlingEnabled = true;
    }

    @Override
//    @Scheduled(fixedRate = 5000)
    @Scheduled(fixedRateString = "${scheduling.task.interval}")
    public void scheduledHandleFaultQueue() {
        if (!faultHandlingEnabled || currentFaultStrategy == null) {
            return;
        }

        System.out.println("\n=== 定时检查故障队列 ===");
        List<String> faultPiles = chargingPileQueueService.getFaultPiles();
        if (faultPiles.isEmpty()) {
            System.out.println("没有故障充电桩，停止故障处理定时任务");
            faultHandlingEnabled = false;
            currentFaultStrategy = null;
            schedulingService.startCallNumber();
            return;
        }

        boolean allProcessed = true;  // 故障队列中所有请求是否都调度完毕
        for (String faultPileId : faultPiles) {
            Queue<Long> faultQueue = chargingPileQueueService.getQueueRequests(faultPileId);
            if (faultQueue != null && !faultQueue.isEmpty()) {

                // 将faultQueue中的请求状态改变为WAITING_IN_CHARGING_AREA
                for(long requestId : faultQueue){
                    ChargingRequest request = chargingRequestMapper.findById(requestId);
                    request.setStatus(RequestStatus.WAITING_IN_CHARGING_AREA);
                    chargingRequestMapper.update(request);
                }

                // 获取故障充电桩类型
                ChargingPileType pileType = chargingPileMapper.findById(faultPileId).getType();
                List<String> availablePiles = chargingPileQueueService.getAvailableAndInUsePiles(pileType)
                        .stream()
                        .filter(pid -> !pid.equals(faultPileId))
                        .collect(Collectors.toList());

                // 选择对应的故障处理策略
                if (!availablePiles.isEmpty()) {
                    boolean processed;
                    if ("PRIORITY".equals(currentFaultStrategy)) {
                        processed = handlePriorityStrategy(faultQueue, availablePiles, pileType);
                    } else if ("TIME_ORDER".equals(currentFaultStrategy)) {
                        processed = handleTimeOrderStrategy(faultQueue, availablePiles, pileType);
                    } else {
                        System.out.println("未知的故障处理策略: " + currentFaultStrategy);
                        processed = false;
                    }
                    if (!processed) {
                        allProcessed = false;
                    }
                }
            }
        }

        // 故障处理完毕，重新开始叫号
        if (allProcessed) {
            System.out.println("所有故障队列处理完成，停止故障处理定时任务");
            faultHandlingEnabled = false;
            currentFaultStrategy = null;
            schedulingService.startCallNumber();
        }
        System.out.println("=== 故障队列检查完成 ===\n");
    }

    @Override
    public boolean handlePriorityStrategy(Queue<Long> faultQueue, List<String> availablePiles, ChargingPileType pileType) {
        System.out.println("使用优先级调度策略");

        // 如果充电区已满，直接返回false
        if (!chargingPileQueueService.hasPileVacancy()) {
            System.out.println("充电区已满，无法处理故障队列");
            return false;
        }

        boolean allRequestsProcessed = schedulingService.processQueue(faultQueue, pileType);
        if (!allRequestsProcessed) {
            System.out.println("部分请求已分配，剩余请求等待充电区空位");
        }
        return allRequestsProcessed;
    }

    @Override
    public boolean handleTimeOrderStrategy(Queue<Long> faultQueue, List<String> availablePiles, ChargingPileType pileType) {

        // 只在第一次执行时收集和排序请求
        if (!hasCollectedRequests) {
            // 1. 收集所有需要重新调度的请求
            List<Long> allRequests = new ArrayList<>();
            // 1.1 故障桩队列全部
            allRequests.addAll(faultQueue);

            // 1.2 其它桩队列中"尚未充电"的请求（队首不动，队首之外的全部）
            Map<String, Queue<Long>> pileQueues = new HashMap<>();
            for (String pid : availablePiles) {
                Queue<Long> queue = chargingPileQueueService.getQueueRequests(pid);
                if (queue != null && !queue.isEmpty()) {
                    pileQueues.put(pid, queue);
                    List<Long> list = new ArrayList<>(queue);
                    if (list.size() > 1) {
                        // 跳过队首（正在充电），只加后面的
                        allRequests.addAll(list.subList(1, list.size()));
                    }
                }
            }

            // 2. 按请求ID排序（请求ID从小到大，即先来先到）
            allRequests.sort(Long::compareTo);

            // 3. 清空所有参与调度的队列（故障桩队列全部清空，其它桩队列只保留队首）
            faultQueue.clear();
            for (Map.Entry<String, Queue<Long>> entry : pileQueues.entrySet()) {
                Queue<Long> queue = entry.getValue();
                if (queue.size() > 1) {
                    Long head = queue.poll(); // 保留队首
                    queue.clear();
                    queue.offer(head);
                }
            }

            // 4. 将排序后的请求重新放入故障队列
            for (Long requestId : allRequests) {
                faultQueue.offer(requestId);
            }

            hasCollectedRequests = true;
        }

        boolean allProcessed = handlePriorityStrategy(faultQueue, availablePiles, pileType);

        // 打印当前所有队列状态，用于调试
        System.out.println("\n--- 故障处理后的队列状态 ---");
        System.out.println("故障队列: " + faultQueue);
        for (String pid : availablePiles) {
            Queue<Long> queue = chargingPileQueueService.getQueueRequests(pid);
            if (queue != null) {
                System.out.println("充电桩 " + pid + " 队列: " + queue);
            }
        }
        System.out.println("--- 队列状态结束 ---\n");

        return allProcessed;
    }

    @Override
    @Transactional
    public void handlePileRecovery(String pileId) {
        // 停止故障处理
        faultHandlingEnabled = false;

        recoveryPileId = pileId;

        // 直接恢复充电桩状态
        // 处理充电桩
        chargingPileService.resolveFault(pileId, "故障已修复");

        System.out.println("\n=== 恢复充电桩 " + pileId + " 故障 ===");

        // 获取故障充电桩类型
        ChargingPileType pileType = chargingPileMapper.findById(pileId).getType();
        
        // 获取同类型的其他充电桩
        List<String> otherPiles = chargingPileQueueService.getAvailableAndInUsePiles(pileType)
                .stream()
                .filter(pid -> !pid.equals(pileId))
                .toList();

        // 检查其他同类型充电桩中是否有车辆排队
        boolean hasWaitingVehicles = otherPiles.stream()
                .anyMatch(pid -> {
                    Queue<Long> queue = chargingPileQueueService.getQueueRequests(pid);
                    return queue != null && queue.size() > 1; // 队首正在充电，所以size>1表示有等待车辆
                });

        if (hasWaitingVehicles) {

            System.out.println("同类型充电桩中尚有车辆排队，开启故障恢复处理");
            // 停止叫号服务
            schedulingService.stopCallNumber();

            // 重置收集标志
            hasCollectedRequests = false;

            // 开启故障恢复处理定时任务
            faultRecoveryEnabled = true;
        } else {
            System.out.println("同类型充电桩中没有等待车辆，直接恢复充电桩");
        }
    }

    @Override
//    @Scheduled(fixedRate = 5000)
    @Scheduled(fixedRateString = "${scheduling.task.interval}")
    public void scheduledHandleFaultRecoveryQueue() {
        if (!faultRecoveryEnabled) {
            return;
        }

        System.out.println("\n=== 定时检查故障恢复队列 ===");

        ChargingPileType pileType = chargingPileMapper.findById(recoveryPileId).getType();

        // 恢复队列不为空，直接开始充电
        Queue<Long> recoveryPileQueue = chargingPileQueueService.getQueueRequests(recoveryPileId);
        if (!recoveryPileQueue.isEmpty()) {
            chargingPileService.startCharging(recoveryPileQueue.peek(), recoveryPileId);
        }

        // 获取同类型的其他充电桩
        List<String> otherPiles = chargingPileQueueService.getAvailableAndInUsePiles(pileType)
                .stream()
                .filter(pid -> !pid.equals(recoveryPileId))
                .toList();

        // 收集所有需要重新调度的请求
        List<Long> allRequests = new ArrayList<>();

        // 从其他充电桩队列中收集等待的请求
        for (String pid : otherPiles) {
            Queue<Long> queue = chargingPileQueueService.getQueueRequests(pid);
            if (queue != null && queue.size() > 1) {
                List<Long> list = new ArrayList<>(queue);
                // 跳过队首（正在充电），只加后面的
                allRequests.addAll(list.subList(1, list.size()));
            }
        }

        boolean allProcessed = true;
        if (!allRequests.isEmpty()) {
            // 按请求ID排序（请求ID从小到大，即先来先到）
            allRequests.sort(Long::compareTo);

            // 清空其他充电桩队列中等待的请求（保留队首）
            for (String pid : otherPiles) {
                Queue<Long> queue = chargingPileQueueService.getQueueRequests(pid);
                if (queue != null && queue.size() > 1) {
                    Long head = queue.poll(); // 保留队首
                    queue.clear();
                    queue.offer(head);
                }
            }

            // 创建一个临时队列来存储需要重新调度的请求
            Queue<Long> tempQueue = new LinkedList<>(allRequests);

            // 使用调度服务的processQueue方法处理请求
            boolean processed = schedulingService.processQueue(tempQueue, pileType);
            if (!processed) {
                allProcessed = false;
                System.out.println("部分请求未能完成调度");
            }
        }

        if (allProcessed) {
            System.out.println("所有故障恢复队列处理完成，停止故障恢复处理定时任务");
            faultRecoveryEnabled = false;
            schedulingService.startCallNumber();
        }
        System.out.println("=== 故障恢复队列检查完成 ===\n");
    }

} 