package com.online.chargingSystem.service.impl;

import com.online.chargingSystem.entity.ChargingRequest;
import com.online.chargingSystem.entity.WaitingQueue;
import com.online.chargingSystem.entity.enums.ChargingPileType;
import com.online.chargingSystem.entity.enums.RequestStatus;
import com.online.chargingSystem.mapper.ChargingRequestMapper;
import com.online.chargingSystem.service.ChargingPileQueueService;
import com.online.chargingSystem.service.ChargingPileService;
import com.online.chargingSystem.entity.ChargingPile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static com.online.chargingSystem.entity.enums.ChargingPileType.FAST;
import static com.online.chargingSystem.entity.enums.RequestStatus.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class SchedulingServiceImplTest {

    @InjectMocks
    private SchedulingServiceImpl schedulingService;

    @Mock
    private ChargingRequestMapper chargingRequestMapper;

    @Mock
    private ChargingPileQueueService chargingPileQueueService;

    @Mock
    private ChargingPileService chargingPileService;

    @Mock
    private WaitingQueue waitingQueue;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testHandleCallNumber_WithValidRequests() {
        // 准备测试数据
        Queue<Long> fastQueue = new LinkedList<>();
        fastQueue.add(1L);
        fastQueue.add(2L);

        Queue<Long> slowQueue = new LinkedList<>();
        slowQueue.add(3L);

        LocalDateTime now = LocalDateTime.now();

        ChargingRequest request1 = new ChargingRequest();
        request1.setId(1L);
        request1.setStatus(WAITING_IN_CHARGING_AREA);
        request1.setMode(FAST);
        request1.setUserId(100L);
        request1.setAmount(50.0);
        request1.setQueueNumber("F1");
        request1.setQueueJoinTime(now);
        request1.setRequestTime(now);

        ChargingRequest request2 = new ChargingRequest();
        request2.setId(2L);
        request2.setStatus(WAITING_IN_CHARGING_AREA);
        request2.setMode(FAST);
        request2.setUserId(101L);
        request2.setAmount(60.0);
        request2.setQueueNumber("F2");
        request2.setQueueJoinTime(now);
        request2.setRequestTime(now);

        ChargingRequest request3 = new ChargingRequest();
        request3.setId(3L);
        request3.setStatus(WAITING_IN_CHARGING_AREA);
        request3.setMode(ChargingPileType.SLOW);
        request3.setUserId(102L);
        request3.setAmount(40.0);
        request3.setQueueNumber("S1");
        request3.setQueueJoinTime(now);
        request3.setRequestTime(now);

        // 创建充电桩对象
        ChargingPile fastPile = new ChargingPile();
        fastPile.setId("A");
        fastPile.setChargingPower(120.0);
        fastPile.setType(FAST);

        ChargingPile slowPile = new ChargingPile();
        slowPile.setId("B");
        slowPile.setChargingPower(60.0);
        slowPile.setType(ChargingPileType.SLOW);

        // 模拟行为
        when(waitingQueue.getFastQueue()).thenReturn(fastQueue);
        when(waitingQueue.getSlowQueue()).thenReturn(slowQueue);
        when(chargingRequestMapper.findById(1L)).thenReturn(request1);
        when(chargingRequestMapper.findById(2L)).thenReturn(request2);
        when(chargingRequestMapper.findById(3L)).thenReturn(request3);
        when(chargingPileQueueService.getAvailableAndInUsePiles(FAST))
                .thenReturn(List.of("A"));
        when(chargingPileQueueService.getAvailableAndInUsePiles(ChargingPileType.SLOW))
                .thenReturn(List.of("B"));
        when(chargingPileQueueService.getQueueSize(any())).thenReturn(0);
        when(chargingPileQueueService.hasPileVacancy()).thenReturn(true);
        when(chargingPileQueueService.getFaultPiles()).thenReturn(List.of());
        when(chargingPileService.getPileStatus("A")).thenReturn(fastPile);
        when(chargingPileService.getPileStatus("B")).thenReturn(slowPile);
        when(chargingPileQueueService.isQueueFull(any())).thenReturn(false);

        // 执行测试
        schedulingService.handleCallNumber();

        // 验证结果
        verify(chargingRequestMapper, times(3)).update(any(ChargingRequest.class));
        verify(chargingPileQueueService, times(3)).addToQueue(any(), any());
        verify(chargingPileService, times(3)).startCharging(any(), any());
        verify(chargingPileService).startCharging(100L, "A");
        verify(chargingPileService).startCharging(101L, "A");
        verify(chargingPileService).startCharging(102L, "B");
        assert fastQueue.isEmpty();
        assert slowQueue.isEmpty();
    }

    @Test
    void testHandleChargingComplete_WithValidRequest() {
        // 准备测试数据
        Long requestId = 1L;
        LocalDateTime now = LocalDateTime.now();
        
        ChargingRequest request = new ChargingRequest();
        request.setId(requestId);
        request.setUserId(100L);
        request.setChargingPileId("A");
        request.setStatus(CHARGING);
        request.setAmount(50.0);
        request.setQueueNumber("F1");
        request.setQueueJoinTime(now);
        request.setRequestTime(now);

        // 模拟行为
        when(chargingRequestMapper.findById(requestId)).thenReturn(request);
        when(chargingPileQueueService.getQueueSize("A")).thenReturn(0);

        // 执行测试
        schedulingService.handleChargingComplete(requestId);

        // 验证结果
        verify(chargingRequestMapper).update(argThat(req -> 
            req.getStatus() == COMPLETED
        ));
        verify(chargingPileQueueService).removeFromQueue("A", requestId);
        verify(chargingPileService).startChargingPile("A");
        verify(chargingPileService).endCharging(100L, "A");
    }

    @Test
    void testHandleChargingComplete_WithInvalidRequest() {
        // 准备测试数据
        Long requestId = 1L;

        // 模拟行为
        when(chargingRequestMapper.findById(requestId)).thenReturn(null);

        // 执行测试
        schedulingService.handleChargingComplete(requestId);

        // 验证结果
        verify(chargingRequestMapper, never()).update(any(ChargingRequest.class));
        verify(chargingPileQueueService, never()).removeFromQueue(any(), any());
        verify(chargingPileService, never()).startChargingPile(any());
        verify(chargingPileService, never()).endCharging(any(), any());
    }
} 