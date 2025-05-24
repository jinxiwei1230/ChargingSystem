package com.online.chargingsystem.service;

public interface QueueService {
    Object getQueueInfo(String carId);
    Object queryQueueState(String queueList);
} 