package com.online.chargingsystem.service.impl;

import com.online.chargingsystem.entity.*;
import com.online.chargingsystem.mapper.*;
import com.online.chargingsystem.service.DispatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class DispatchServiceImpl implements DispatchService {

    @Autowired
    private ChargingPileMapper chargingPileMapper;

    @Autowired
    private ChargingQueueMapper chargingQueueMapper;

    @Autowired
    private ChargingRequestMapper chargingRequestMapper;

    @Override
    public Object priorityDispatch(String carId) {
        // 1. 获取车辆信息
        ChargingRequest request = chargingRequestMapper.findByCarIdAndStatus(carId, "PENDING");
        if (request == null) {
            return null;
        }

        // 2. 根据优先级策略分配充电桩
        List<ChargingPile> availablePiles = chargingPileMapper.findByTypeAndStatus(
            request.getRequestMode(), "IDLE");
        
        if (availablePiles.isEmpty()) {
            // 如果没有可用充电桩，加入等待队列
            ChargingQueue queue = new ChargingQueue();
            queue.setCarId(carId);
            queue.setRequestMode(request.getRequestMode());
            queue.setRequestAmount(request.getRequestAmount());
            queue.setQueueType(request.getRequestMode());
            queue.setStatus("WAITING");
            queue.setQueueTime(LocalDateTime.now());
            chargingQueueMapper.insert(queue);
            return queue;
        }

        // 3. 分配充电桩并更新状态
        ChargingPile selectedPile = availablePiles.get(0);
        request.setPileId(selectedPile.getPileId());
        request.setStatus("ACCEPTED");
        chargingRequestMapper.update(request);
        
        selectedPile.setStatus("CHARGING");
        chargingPileMapper.update(selectedPile);

        return request;
    }

    @Override
    public Object timeOrderDispatch(String carId) {
        // 1. 获取车辆信息
        ChargingRequest request = chargingRequestMapper.findByCarIdAndStatus(carId, "PENDING");
        if (request == null) {
            return null;
        }

        // 2. 获取同类型充电桩的等待队列
        List<ChargingQueue> waitingQueues = chargingQueueMapper.findByQueueTypeAndStatus(
            request.getRequestMode(), "WAITING");
        
        // 3. 按照排队时间顺序分配充电桩
        List<ChargingPile> availablePiles = chargingPileMapper.findByTypeAndStatus(
            request.getRequestMode(), "IDLE");
        
        if (availablePiles.isEmpty()) {
            // 如果没有可用充电桩，加入等待队列
            ChargingQueue queue = new ChargingQueue();
            queue.setCarId(carId);
            queue.setRequestMode(request.getRequestMode());
            queue.setRequestAmount(request.getRequestAmount());
            queue.setQueueType(request.getRequestMode());
            queue.setStatus("WAITING");
            queue.setQueueTime(LocalDateTime.now());
            chargingQueueMapper.insert(queue);
            return queue;
        }

        // 4. 分配充电桩并更新状态
        ChargingPile selectedPile = availablePiles.get(0);
        request.setPileId(selectedPile.getPileId());
        request.setStatus("ACCEPTED");
        chargingRequestMapper.update(request);
        
        selectedPile.setStatus("CHARGING");
        chargingPileMapper.update(selectedPile);

        return request;
    }

    @Override
    public Object faultRecover(String pileId) {
        // 1. 更新充电桩状态为正常
        ChargingPile pile = chargingPileMapper.findByPileId(pileId);
        if (pile == null) {
            return null;
        }
        
        pile.setStatus("IDLE");
        chargingPileMapper.update(pile);

        // 2. 重新调度等待队列中的车辆
        List<ChargingQueue> waitingQueues = chargingQueueMapper.findByQueueTypeAndStatus(
            pile.getType(), "WAITING");
        
        if (!waitingQueues.isEmpty()) {
            ChargingQueue nextQueue = waitingQueues.get(0);
            return priorityDispatch(nextQueue.getCarId());
        }

        return pile;
    }

    @Override
    public Object singleDispatch(String carId) {
        // 1. 获取车辆信息
        ChargingRequest request = chargingRequestMapper.findByCarIdAndStatus(carId, "PENDING");
        if (request == null) {
            return null;
        }

        // 2. 分配充电桩
        List<ChargingPile> availablePiles = chargingPileMapper.findByTypeAndStatus(
            request.getRequestMode(), "IDLE");
        
        if (availablePiles.isEmpty()) {
            // 如果没有可用充电桩，加入等待队列
            ChargingQueue queue = new ChargingQueue();
            queue.setCarId(carId);
            queue.setRequestMode(request.getRequestMode());
            queue.setRequestAmount(request.getRequestAmount());
            queue.setQueueType(request.getRequestMode());
            queue.setStatus("WAITING");
            queue.setQueueTime(LocalDateTime.now());
            chargingQueueMapper.insert(queue);
            return queue;
        }

        // 3. 更新车辆状态
        ChargingPile selectedPile = availablePiles.get(0);
        request.setPileId(selectedPile.getPileId());
        request.setStatus("ACCEPTED");
        chargingRequestMapper.update(request);
        
        selectedPile.setStatus("CHARGING");
        chargingPileMapper.update(selectedPile);

        return request;
    }

    @Override
    public Object batchDispatch(String[] carIds) {
        // 1. 获取所有车辆信息
        List<ChargingRequest> requests = new ArrayList<>();
        for (String carId : carIds) {
            ChargingRequest request = chargingRequestMapper.findByCarIdAndStatus(carId, "PENDING");
            if (request != null) {
                requests.add(request);
            }
        }

        if (requests.isEmpty()) {
            return null;
        }

        // 2. 批量分配充电桩
        Map<String, Object> result = new HashMap<>();
        List<ChargingRequest> dispatchedRequests = new ArrayList<>();
        List<ChargingQueue> queuedRequests = new ArrayList<>();

        for (ChargingRequest request : requests) {
            List<ChargingPile> availablePiles = chargingPileMapper.findByTypeAndStatus(
                request.getRequestMode(), "IDLE");
            
            if (!availablePiles.isEmpty()) {
                // 分配充电桩
                ChargingPile selectedPile = availablePiles.get(0);
                request.setPileId(selectedPile.getPileId());
                request.setStatus("ACCEPTED");
                chargingRequestMapper.update(request);
                
                selectedPile.setStatus("CHARGING");
                chargingPileMapper.update(selectedPile);
                
                dispatchedRequests.add(request);
            } else {
                // 加入等待队列
                ChargingQueue queue = new ChargingQueue();
                queue.setCarId(request.getCarId());
                queue.setRequestMode(request.getRequestMode());
                queue.setRequestAmount(request.getRequestAmount());
                queue.setQueueType(request.getRequestMode());
                queue.setStatus("WAITING");
                queue.setQueueTime(LocalDateTime.now());
                chargingQueueMapper.insert(queue);
                
                queuedRequests.add(queue);
            }
        }

        result.put("dispatched", dispatchedRequests);
        result.put("queued", queuedRequests);
        return result;
    }
} 