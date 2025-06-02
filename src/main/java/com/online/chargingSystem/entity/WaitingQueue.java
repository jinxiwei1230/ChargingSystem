package com.online.chargingSystem.entity;

import com.online.chargingSystem.entity.enums.ChargingPileType;
import com.online.chargingSystem.entity.enums.QueueStatus;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.Queue;

// 等候区队列（有两条：慢充、快充）
@Data
@Component
public class WaitingQueue {
    // 队列最大长度
    private static final int QUEUE_SIZE = 6;
    //慢充队列，存储请求id
    private Queue<Long> fastQueue = new LinkedList<>();
    //慢充队列，存储请求id
    private Queue<Long> slowQueue = new LinkedList<>();
} 