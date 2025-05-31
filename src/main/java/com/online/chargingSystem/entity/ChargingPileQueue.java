package com.online.chargingSystem.entity;

import java.util.Queue;
import lombok.Data;

// 充电区队列，每个充电桩各一个
@Data
public class ChargingPileQueue {
    // 队列对应充电桩编号(如A/B/C/D/E)
    private String pileId;
    //队列长度=2
    private static final int QUEUE_LENGTH = 2;
    //队列，存储请求id
    private Queue<Long> queue;
}
