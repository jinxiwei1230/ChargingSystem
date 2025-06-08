package com.online.chargingSystem.service;

import com.online.chargingSystem.entity.ChargingOrder;
import com.online.chargingSystem.entity.ChargingRequest;
import com.online.chargingSystem.entity.ChargingPile;
import com.online.chargingSystem.entity.enums.ChargingPileStatus;
import com.online.chargingSystem.entity.enums.RequestStatus;
import com.online.chargingSystem.mapper.ChargingOrderMapper;
import com.online.chargingSystem.mapper.ChargingRequestMapper;
import com.online.chargingSystem.mapper.ChargingPileMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class ChargingProgressService {
    
    @Autowired
    private ChargingOrderMapper chargingOrderMapper;
    
    @Autowired
    private ChargingRequestMapper chargingRequestMapper;
    
    @Autowired
    private ChargingPileMapper chargingPileMapper;
    
    @Autowired
    private SchedulingService schedulingService;
    
    // 存储正在充电的请求状态
    private final ConcurrentHashMap<Long, ChargingProgress> chargingProgressMap = new ConcurrentHashMap<>();
    
    // 充电进度内部类
    private static class ChargingProgress {
        private final String chargingPileId;
        private final Long userId;
        private final double powerRate;
        private final double totalPower;
        private final double previousChargedPower;
        private double currentChargedPower;
        private LocalDateTime startTime;

        public ChargingProgress(Long requestId, String chargingPileId, Long userId, double powerRate, double totalPower, double previousChargedPower) {
            this.chargingPileId = chargingPileId;
            this.userId = userId;
            this.powerRate = powerRate;
            this.totalPower = totalPower;
            this.previousChargedPower = previousChargedPower;
            this.currentChargedPower = 0.0;
            this.startTime = LocalDateTime.now();
        }

        public String getChargingPileId() {
            return chargingPileId;
        }

        public Long getUserId() {
            return userId;
        }

        public double getTotalPower() {
            return totalPower;
        }

        public void addChargedPower(double power) {
            this.currentChargedPower += power;
        }

        public double getCurrentChargedPower() {
            return currentChargedPower;
        }

        public LocalDateTime getStartTime() {
            return startTime;
        }
    }
    
    /**
     * 开始监控充电进度
     * @param requestId 充电请求ID
     */
    @Async
    public void startChargingProgress(Long requestId) {
        // 获取充电请求信息
        ChargingRequest request = chargingRequestMapper.findById(requestId);
        if (request == null) {
            System.out.println("充电请求不存在：" + requestId);
            return;
        }

        // 获取充电桩信息
        String chargingPileId = request.getChargingPileId();
        ChargingPile chargingPile = chargingPileMapper.findById(chargingPileId);
        if (chargingPile == null) {
            System.out.println("充电桩不存在：" + chargingPileId);
            return;
        }

        // 获取充电桩功率
        Double power = chargingPile.getChargingPower();
        if (power == null || power <= 0) {
            System.out.println("充电桩功率无效：" + power);
            return;
        }

        // 获取总充电量
        Double totalPower = request.getAmount();
        if (totalPower == null || totalPower <= 0) {
            System.out.println("充电量无效：" + totalPower);
            return;
        }

        // 检查是否有故障订单
        ChargingOrder faultOrder = chargingOrderMapper.findByRequestId(requestId);
        Double previousChargedPower = 0.0;
        if (faultOrder != null) {
            previousChargedPower = faultOrder.getTotalKwh().doubleValue();
            totalPower -= previousChargedPower;
            System.out.println("存在故障订单，已充电量：" + previousChargedPower + "，剩余充电量：" + totalPower);
        }

        // 创建充电进度对象
        ChargingProgress progress = new ChargingProgress(
            requestId,
            chargingPileId,
            request.getUserId(),
            power,
            totalPower,
            previousChargedPower
        );

        // 启动监控线程
        Thread monitorThread = new Thread(() -> monitorChargingProgress(requestId));
        monitorThread.start();

        // 保存监控状态
        chargingProgressMap.put(requestId, progress);
        System.out.println("开始监控充电进度，请求ID：" + requestId);
    }
    
    /**
     * 监控充电进度
     * @param requestId 充电请求ID
     */
    private void monitorChargingProgress(Long requestId) {
        ChargingProgress progress = chargingProgressMap.get(requestId);
        if (progress == null) {
            System.out.println("未找到充电进度信息：" + requestId);
            return;
        }

        try {
            while (!Thread.currentThread().isInterrupted()) {
                // 检查请求是否还在监控列表中
                if (!chargingProgressMap.containsKey(requestId)) {
                    System.out.println("请求已从监控列表中移除：" + requestId);
                    break;
                }

                // 获取当前充电量
                double currentPower = getCurrentPower(progress);
                System.out.println(requestId + "：当前充电量: " + currentPower + " 度, 目标充电量: " + (progress.getTotalPower()+ progress.previousChargedPower) + " 度");

                // 检查是否完成充电
                if (currentPower >= (progress.getTotalPower()+ progress.previousChargedPower)) {
                    System.out.println("充电完成，请求ID：" + requestId);
                    // 调用调度服务的充电完成处理方法
                    schedulingService.handleChargingComplete(progress.getUserId());
                    break;
                }

                // 每秒检查一次
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            System.out.println("监控线程被中断：" + requestId);
        } finally {
            // 移除监控状态
            chargingProgressMap.remove(requestId);
            System.out.println("移除监控状态：" + requestId);
        }
    }
    
    /**
     * 获取当前充电进度
     * @param requestId 充电请求ID
     * @return 当前充电量（千瓦时）
     */
    public double getCurrentProgress(Long requestId) {
        ChargingProgress progress = chargingProgressMap.get(requestId);
        if (progress == null) return 0.0;
        return getCurrentPower(progress);
    }

    private double getCurrentPower(ChargingProgress progress) {
        long seconds = java.time.Duration.between(progress.getStartTime(), LocalDateTime.now()).getSeconds();
        double currentChargedPower = (progress.powerRate * seconds) / 3600.0; // 转换为千瓦时
        return currentChargedPower + progress.previousChargedPower;
    }
    
    /**
     * 检查充电是否完成
     * @param requestId 充电请求ID
     * @return 是否完成
     */
    public boolean isChargingCompleted(Long requestId) {
        ChargingProgress progress = chargingProgressMap.get(requestId);
        return progress != null && getCurrentPower(progress) >= progress.getTotalPower()+ progress.previousChargedPower;
    }
    
    /**
     * 移除充电进度监控
     * @param requestId 充电请求ID
     */
    public void removeChargingProgress(Long requestId) {
        chargingProgressMap.remove(requestId);
    }


} 