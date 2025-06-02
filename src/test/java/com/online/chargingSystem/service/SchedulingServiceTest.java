package com.online.chargingSystem.service;

import com.online.chargingSystem.entity.ChargingRequest;
import com.online.chargingSystem.entity.User;
import com.online.chargingSystem.entity.WaitingQueue;
import com.online.chargingSystem.entity.enums.ChargingPileType;
import com.online.chargingSystem.entity.enums.RequestStatus;
import com.online.chargingSystem.mapper.ChargingRequestMapper;
import com.online.chargingSystem.mapper.UserMapper;
import com.online.chargingSystem.service.impl.SchedulingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class SchedulingServiceTest {

    @InjectMocks
    private SchedulingServiceImpl schedulingService;

    @Mock
    private ChargingRequestMapper chargingRequestMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private WaitingQueue waitingQueue;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testIsWaitingAreaFull_WhenFull() {
        // 准备测试数据
        Queue<Long> fastQueue = new LinkedList<>();
        Queue<Long> slowQueue = new LinkedList<>();
        when(waitingQueue.getFastQueue()).thenReturn(fastQueue);
        when(waitingQueue.getSlowQueue()).thenReturn(slowQueue);
        
        // 添加元素到队列
        fastQueue.add(1L);
        fastQueue.add(2L);
        fastQueue.add(3L);
        slowQueue.add(4L);
        slowQueue.add(5L);
        slowQueue.add(6L);

        // 执行测试
        boolean result = schedulingService.isWaitingAreaFull();

        // 验证结果
        assertTrue(result);
    }

    @Test
    void testIsWaitingAreaFull_WhenNotFull() {
        // 准备测试数据
        Queue<Long> fastQueue = new LinkedList<>();
        Queue<Long> slowQueue = new LinkedList<>();
        when(waitingQueue.getFastQueue()).thenReturn(fastQueue);
        when(waitingQueue.getSlowQueue()).thenReturn(slowQueue);
        
        // 添加元素到队列
        fastQueue.add(1L);
        fastQueue.add(2L);
        slowQueue.add(3L);
        slowQueue.add(4L);

        // 执行测试
        boolean result = schedulingService.isWaitingAreaFull();

        // 验证结果
        assertFalse(result);
    }

    @Test
    void testIsRequestAmountValid_WhenValid() {
        // 准备测试数据
        Long userId = 1L;
        Double requestAmount = 50.0;
        User user = new User();
        user.setBatteryCapacity(100.0);
        when(userMapper.selectById(userId)).thenReturn(user);

        // 执行测试
        boolean result = schedulingService.isRequestAmountValid(userId, requestAmount);

        // 验证结果
        assertTrue(result);
    }

    @Test
    void testIsRequestAmountValid_WhenInvalid() {
        // 准备测试数据
        Long userId = 1L;
        Double requestAmount = 150.0;
        User user = new User();
        user.setBatteryCapacity(100.0);
        when(userMapper.selectById(userId)).thenReturn(user);

        // 执行测试
        boolean result = schedulingService.isRequestAmountValid(userId, requestAmount);

        // 验证结果
        assertFalse(result);
    }

    @Test
    void testHandleChargingRequest_Success() {
        // 准备测试数据
        Long userId = 1L;
        Double requestAmount = 50.0;
        ChargingPileType mode = ChargingPileType.FAST;
        ChargingRequest expectedRequest = new ChargingRequest();
        expectedRequest.setUserId(userId);
        expectedRequest.setAmount(requestAmount);
        expectedRequest.setMode(mode);
        expectedRequest.setStatus(RequestStatus.WAITING_IN_WAITING_AREA);

        when(chargingRequestMapper.insert(any(ChargingRequest.class))).thenReturn(1);
        Queue<Long> fastQueue = new LinkedList<>();
        when(waitingQueue.getFastQueue()).thenReturn(fastQueue);

        // 执行测试
        ChargingRequest result = schedulingService.handleChargingRequest(userId, requestAmount, mode);

        // 验证结果
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(requestAmount, result.getAmount());
        assertEquals(mode, result.getMode());
        assertEquals(RequestStatus.WAITING_IN_WAITING_AREA, result.getStatus());
        verify(chargingRequestMapper).insert(any(ChargingRequest.class));
    }

    @Test
    void testGetQueueNumber_Success() {
        // 准备测试数据
        Long userId = 1L;
        ChargingRequest request = new ChargingRequest();
        request.setQueueNumber("F1");
        List<ChargingRequest> requests = new LinkedList<>();
        requests.add(request);
        when(chargingRequestMapper.findByUserId(userId)).thenReturn(requests);

        // 执行测试
        String result = schedulingService.getQueueNumber(userId);

        // 验证结果
        assertEquals("F1", result);
    }

    @Test
    void testModifyChargingMode_Success() {
        // 准备测试数据
        Long userId = 1L;
        ChargingPileType newMode = ChargingPileType.SLOW;
        ChargingRequest request = new ChargingRequest();
        request.setId(1L);
        request.setMode(ChargingPileType.FAST);
        List<ChargingRequest> requests = new LinkedList<>();
        requests.add(request);
        when(chargingRequestMapper.findByUserId(userId)).thenReturn(requests);
        Queue<Long> fastQueue = new LinkedList<>();
        Queue<Long> slowQueue = new LinkedList<>();
        when(waitingQueue.getFastQueue()).thenReturn(fastQueue);
        when(waitingQueue.getSlowQueue()).thenReturn(slowQueue);

        // 执行测试
        schedulingService.modifyChargingMode(userId, newMode);

        // 验证结果
        verify(chargingRequestMapper).update(any(ChargingRequest.class));
    }

    @Test
    void testModifyChargingAmount_Success() {
        // 准备测试数据
        Long userId = 1L;
        Double newAmount = 60.0;
        ChargingRequest request = new ChargingRequest();
        request.setId(1L);
        request.setAmount(50.0);
        List<ChargingRequest> requests = new LinkedList<>();
        requests.add(request);
        when(chargingRequestMapper.findByUserId(userId)).thenReturn(requests);

        // 执行测试
        schedulingService.modifyChargingAmount(userId, newAmount);

        // 验证结果
        verify(chargingRequestMapper).update(any(ChargingRequest.class));
    }

    @Test
    void testIsInWaitingArea_WhenInWaitingArea() {
        // 准备测试数据
        Long userId = 1L;
        ChargingRequest request = new ChargingRequest();
        request.setStatus(RequestStatus.WAITING_IN_WAITING_AREA);
        List<ChargingRequest> requests = new LinkedList<>();
        requests.add(request);
        when(chargingRequestMapper.findByUserId(userId)).thenReturn(requests);

        // 执行测试
        boolean result = schedulingService.isInWaitingArea(userId);

        // 验证结果
        assertTrue(result);
    }

    @Test
    void testIsInWaitingArea_WhenNotInWaitingArea() {
        // 准备测试数据
        Long userId = 1L;
        ChargingRequest request = new ChargingRequest();
        request.setStatus(RequestStatus.CHARGING);
        List<ChargingRequest> requests = new LinkedList<>();
        requests.add(request);
        when(chargingRequestMapper.findByUserId(userId)).thenReturn(requests);

        // 执行测试
        boolean result = schedulingService.isInWaitingArea(userId);

        // 验证结果
        assertFalse(result);
    }

    @Test
    void testCancelAndRequeue_Success() {
        // 准备测试数据
        Long userId = 1L;
        ChargingRequest request = new ChargingRequest();
        request.setId(1L);
        request.setAmount(50.0);
        request.setMode(ChargingPileType.FAST);
        List<ChargingRequest> requests = new LinkedList<>();
        requests.add(request);
        when(chargingRequestMapper.findByUserId(userId)).thenReturn(requests);
        
        // 模拟队列
        Queue<Long> fastQueue = new LinkedList<>();
        Queue<Long> slowQueue = new LinkedList<>();
        when(waitingQueue.getFastQueue()).thenReturn(fastQueue);
        when(waitingQueue.getSlowQueue()).thenReturn(slowQueue);
        
        // 模拟handleChargingRequest的行为
        doAnswer(invocation -> {
            ChargingRequest newRequest = (ChargingRequest) invocation.getArgument(0);
            newRequest.setId(2L);
            return 1;
        }).when(chargingRequestMapper).insert(any(ChargingRequest.class));

        // 执行测试
        schedulingService.cancelAndRequeue(userId);

        // 验证结果
        verify(chargingRequestMapper).update(any(ChargingRequest.class));
        verify(chargingRequestMapper).insert(any(ChargingRequest.class));
    }

    @Test
    void testCancel_Success() {
        // 准备测试数据
        Long userId = 1L;
        ChargingRequest request = new ChargingRequest();
        request.setId(1L);
        request.setMode(ChargingPileType.FAST);
        List<ChargingRequest> requests = new LinkedList<>();
        requests.add(request);
        when(chargingRequestMapper.findByUserId(userId)).thenReturn(requests);
        Queue<Long> fastQueue = new LinkedList<>();
        Queue<Long> slowQueue = new LinkedList<>();
        when(waitingQueue.getFastQueue()).thenReturn(fastQueue);
        when(waitingQueue.getSlowQueue()).thenReturn(slowQueue);

        // 执行测试
        schedulingService.cancel(userId);

        // 验证结果
        verify(chargingRequestMapper).update(any(ChargingRequest.class));
    }

    @Test
    void testGetAheadNumber_WhenInFastQueue() {
        // 准备测试数据
        Long userId = 1L;
        ChargingRequest request = new ChargingRequest();
        request.setId(3L);
        request.setMode(ChargingPileType.FAST);
        List<ChargingRequest> requests = new LinkedList<>();
        requests.add(request);
        when(chargingRequestMapper.findByUserId(userId)).thenReturn(requests);
        
        // 模拟快充队列中有3辆车，用户的车在第3位
        Queue<Long> fastQueue = new LinkedList<>();
        fastQueue.add(1L);
        fastQueue.add(2L);
        fastQueue.add(3L);
        when(waitingQueue.getFastQueue()).thenReturn(fastQueue);
        
        // 执行测试
        int result = schedulingService.getAheadNumber(userId);
        
        // 验证结果
        assertEquals(2, result);
    }
    
    @Test
    void testGetAheadNumber_WhenInSlowQueue() {
        // 准备测试数据
        Long userId = 1L;
        ChargingRequest request = new ChargingRequest();
        request.setId(2L);
        request.setMode(ChargingPileType.SLOW);
        List<ChargingRequest> requests = new LinkedList<>();
        requests.add(request);
        when(chargingRequestMapper.findByUserId(userId)).thenReturn(requests);
        
        // 模拟慢充队列中有2辆车，用户的车在第2位
        Queue<Long> slowQueue = new LinkedList<>();
        slowQueue.add(1L);
        slowQueue.add(2L);
        when(waitingQueue.getSlowQueue()).thenReturn(slowQueue);
        
        // 执行测试
        int result = schedulingService.getAheadNumber(userId);
        
        // 验证结果
        assertEquals(1, result);
    }
    
    @Test
    void testGetAheadNumber_WhenFirstInQueue() {
        // 准备测试数据
        Long userId = 1L;
        ChargingRequest request = new ChargingRequest();
        request.setId(1L);
        request.setMode(ChargingPileType.FAST);
        List<ChargingRequest> requests = new LinkedList<>();
        requests.add(request);
        when(chargingRequestMapper.findByUserId(userId)).thenReturn(requests);
        
        // 模拟快充队列中只有1辆车，用户的车在第1位
        Queue<Long> fastQueue = new LinkedList<>();
        fastQueue.add(1L);
        when(waitingQueue.getFastQueue()).thenReturn(fastQueue);
        
        // 执行测试
        int result = schedulingService.getAheadNumber(userId);
        
        // 验证结果
        assertEquals(0, result);
    }
} 