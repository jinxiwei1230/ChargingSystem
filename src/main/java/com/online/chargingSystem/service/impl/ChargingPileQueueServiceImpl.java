package com.online.chargingSystem.service.impl;

import com.online.chargingSystem.entity.ChargingPile;
import com.online.chargingSystem.entity.ChargingPileQueue;
import com.online.chargingSystem.entity.enums.ChargingPileStatus;
import com.online.chargingSystem.entity.enums.ChargingPileType;
import com.online.chargingSystem.mapper.ChargingPileMapper;
import com.online.chargingSystem.service.ChargingPileQueueService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

// 充电桩队列管理器
@Data
@Component
@Service
public class ChargingPileQueueServiceImpl implements ChargingPileQueueService, CommandLineRunner {
    // 所有充电桩的队列
    public ConcurrentHashMap<String, ChargingPileQueue> pileQueues = new ConcurrentHashMap<>();
    
    @Autowired
    private ChargingPileMapper chargingPileMapper;

    // 初始化5个充电桩的队列
    @Override
    public void run(String... args) {
        // 快充充电桩 A, B
        initPileQueue("A", ChargingPileType.FAST);
        initPileQueue("B", ChargingPileType.FAST);
        
        // 慢充充电桩 C, D, E
        initPileQueue("C", ChargingPileType.SLOW);
        initPileQueue("D", ChargingPileType.SLOW);
        initPileQueue("E", ChargingPileType.SLOW);
        
        System.out.println("充电桩队列初始化完成，共初始化" + pileQueues.size() + "个充电桩队列");
    }

    // 初始化充电桩队列
    @Override
    public void initPileQueue(String pileId, ChargingPileType type) {
        ChargingPileQueue queue = new ChargingPileQueue();
        queue.setPileId(pileId);
        queue.setQueue(new LinkedList<>());
        pileQueues.put(pileId, queue);
    }

    // 获取指定充电桩的队列
    @Override
    public ChargingPileQueue getPileQueue(String pileId) {
        return pileQueues.get(pileId);
    }

    // 将请求添加到充电桩队列
    @Override
    public void addToQueue(String pileId, Long requestId) {
        ChargingPileQueue queue = getOrCreateQueue(pileId);
        if (!isQueueFull(pileId)) {
            queue.getQueue().offer(requestId);
        }
    }
    
    // 从充电桩队列中移除请求
    @Override
    public void removeFromQueue(String pileId, Long requestId) {
        ChargingPileQueue queue = pileQueues.get(pileId);
        if (queue != null) {
            queue.getQueue().remove(requestId);
        }
    }
    
    // 检查充电桩队列是否已满
    @Override
    public boolean isQueueFull(String pileId) {
        ChargingPileQueue queue = pileQueues.get(pileId);
        return queue != null && queue.getQueue().size() >= ChargingPileQueue.QUEUE_LENGTH;
    }
    
    // 获取充电桩队列中的请求数量
    @Override
    public int getQueueSize(String pileId) {
        ChargingPileQueue queue = pileQueues.get(pileId);
        return queue != null ? queue.getQueue().size() : 0;
    }
    
    // 获取充电桩队列中的所有请求ID
    @Override
    public Queue<Long> getQueueRequests(String pileId) {
        ChargingPileQueue queue = pileQueues.get(pileId);
        return queue != null ? queue.getQueue() : null;
    }
    
    // 获取或创建充电桩队列
    private ChargingPileQueue getOrCreateQueue(String pileId) {
        return pileQueues.computeIfAbsent(pileId, id -> {
            ChargingPileQueue queue = new ChargingPileQueue();
            queue.setPileId(id);
            return queue;
        });
    }
    
    @Override
    public List<String> getFaultPiles() {
        return chargingPileMapper.findByStatus(ChargingPileStatus.FAULT)
                .stream()
                .map(ChargingPile::getId)
                .collect(Collectors.toList());
    }

    // 检查是否有任意一个充电桩的队列还有空位
    @Override
    public boolean hasPileVacancy() {

        return pileQueues.values().stream()
                .anyMatch(queue -> queue.getQueue().size() < ChargingPileQueue.QUEUE_LENGTH);
    }
    
    @Override
    public List<String> getAvailableAndInUsePiles(ChargingPileType type) {
        // 获取指定类型的所有可用充电桩
        List<ChargingPile> piles = chargingPileMapper.findByTypeAndStatus(type, ChargingPileStatus.AVAILABLE);
        // 获取指定类型的所有正在使用的充电桩
        List<ChargingPile> inUsePiles = chargingPileMapper.findByTypeAndStatus(type, ChargingPileStatus.IN_USE);
        // 合并两个列表
        piles.addAll(inUsePiles);
        // 返回所有充电桩的ID
        return piles.stream()
                .map(ChargingPile::getId)
                .collect(Collectors.toList());
    }

    @Override
    public void printPileQueues() {
        System.out.println("\n--- 充电桩队列状态 ---");
        
        // 打印快充充电桩队列
        System.out.println("快充充电桩队列：");
        pileQueues.entrySet().stream()
                .filter(entry -> entry.getKey().equals("A") || entry.getKey().equals("B"))
                .forEach(entry -> {
                    String pileId = entry.getKey();
                    Queue<Long> queue = entry.getValue().getQueue();
                    System.out.printf("充电桩 %s: %s%n", pileId, queue);
                });
        
        // 打印慢充充电桩队列
        System.out.println("\n慢充充电桩队列：");
        pileQueues.entrySet().stream()
                .filter(entry -> entry.getKey().equals("C") || entry.getKey().equals("D") || entry.getKey().equals("E"))
                .forEach(entry -> {
                    String pileId = entry.getKey();
                    Queue<Long> queue = entry.getValue().getQueue();
                    System.out.printf("充电桩 %s: %s%n", pileId, queue);
                });
        
        System.out.println("--- 充电桩队列状态结束 ---\n");
    }

    //chj
    @Override
    public Long getQueueHead(String pileId) {
        ChargingPileQueue queue = pileQueues.get(pileId);
        if (queue == null || queue.getQueue().isEmpty()) {
            System.out.println("该充电桩队列为空！");
            return null;
        }
        System.out.println("充电桩队列：");
        System.out.println(queue);
        return queue.getQueue().peek();
    }

} 