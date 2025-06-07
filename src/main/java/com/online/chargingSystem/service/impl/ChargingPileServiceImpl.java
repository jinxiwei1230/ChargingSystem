package com.online.chargingSystem.service.impl;

import com.online.chargingSystem.entity.ChargingPile;
import com.online.chargingSystem.entity.ChargingRequest;
import com.online.chargingSystem.entity.ChargingOrder;
import com.online.chargingSystem.entity.ChargingDetail;
import com.online.chargingSystem.entity.PricePeriod;
import com.online.chargingSystem.entity.enums.ChargingPileStatus;
import com.online.chargingSystem.entity.enums.RequestStatus;
import com.online.chargingSystem.entity.enums.OrderStatus;
import com.online.chargingSystem.entity.enums.PeriodType;
import com.online.chargingSystem.dto.ChargingPileQueueDTO;
import com.online.chargingSystem.dto.ChargingReportDTO;
import com.online.chargingSystem.dto.ChargingReportSummaryDTO;
import com.online.chargingSystem.dto.ChargingQueueInfoDTO;
import com.online.chargingSystem.mapper.*;
import com.online.chargingSystem.service.ChargingPileService;
import com.online.chargingSystem.service.ChargingPileQueueService;
import com.online.chargingSystem.service.SchedulingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Lazy;
import com.online.chargingSystem.entity.User;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Queue;

@Service
public class ChargingPileServiceImpl implements ChargingPileService {

    @Autowired
    private ChargingPileMapper chargingPileMapper;

    @Autowired
    private ChargingRequestMapper chargingRequestMapper;

    @Autowired
    private ChargingOrderMapper chargingOrderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ChargingDetailMapper chargingDetailMapper;

    @Autowired
    private ChargingPileQueueService chargingPileQueueService;

    @Autowired
    private ChargingReportMapper chargingReportMapper;

    @Autowired
    @Lazy
    private SchedulingService schedulingService;

    @Override
    public ChargingPile getPileStatus(String pileId) {
        return chargingPileMapper.findById(pileId);
    }

    //充电桩故障
    @Override
    @Transactional
    public boolean reportFault(String pileId, String faultType, String description) {
        // 1. 获取充电桩信息
        ChargingPile pile = chargingPileMapper.findById(pileId);
        if (pile == null) {
            return false;
        }

        // 2. 获取充电桩队列
        Queue<Long> queue = chargingPileQueueService.getQueueRequests(pileId);
        if (queue != null && !queue.isEmpty()) {
            // 3. 获取正在充电的请求（队列第一个）
            Long chargingRequestId = queue.peek();
            System.out.println("-----充电桩故障正在充电的请求id:-----");
            System.out.println(chargingRequestId);
            ChargingRequest request = chargingRequestMapper.findById(chargingRequestId);
            System.out.println("-----充电桩故障正在充电的请求:-----");
            System.out.println(request);
            if (request != null && request.getStatus() == RequestStatus.CHARGING) {
                // 4. 停止充电，更新请求状态
//               request.setStatus(RequestStatus.FAULTED);
//               chargingRequestMapper.update(request);

                // 5. 生成故障订单
                ChargingOrder order = new ChargingOrder();
                order.setOrderId(generateOrderId());
                order.setUserId(request.getUserId());
                order.setCarId(userMapper.selectById(request.getUserId()).getCarNumber());
                order.setRequestId(request.getId());
                order.setPileId(pileId);
                order.setOrderDate(LocalDate.now());
                order.setOrderStatus(OrderStatus.FAULTED);
                order.setStartTime(request.getChargingStartTime());
                order.setEndTime(LocalDateTime.now());
                order.setCreateTime(LocalDateTime.now());

                // 6. 计算充电时长和充电量
                double chargingDurationMinutes = calculateChargingDuration(request);
                double chargingPower = getChargingPower(pileId);
                double totalKwh = (chargingDurationMinutes / 60.0) * chargingPower;
                order.setTotalDuration(BigDecimal.valueOf(chargingDurationMinutes));
                order.setTotalKwh(BigDecimal.valueOf(totalKwh));

                // 7. 生成详单并计算费用
                List<ChargingDetail> details = generateChargingDetails(order, pileId);
                calculateOrderTotalFees(order, details);

                // 8. 保存订单和详单
                chargingOrderMapper.insert(order);
                for (ChargingDetail detail : details) {
                    chargingDetailMapper.insert(detail);
                }
            }
        }

        // 9. 设置充电桩状态为故障
        pile.setStatus(ChargingPileStatus.FAULT);
        return chargingPileMapper.update(pile) > 0;
    }

    @Override
    @Transactional
    public boolean resolveFault(String pileId, String resolution) {
        ChargingPile pile = chargingPileMapper.findById(pileId);
        if (pile == null) {
            return false;
        }
        pile.setStatus(ChargingPileStatus.AVAILABLE);
        return chargingPileMapper.update(pile) > 0;
    }

    @Override
    @Transactional
    public boolean powerOn(String pileId) {
        ChargingPile pile = chargingPileMapper.findById(pileId);
        if (pile == null) {
            return false;
        }
        pile.setStatus(ChargingPileStatus.AVAILABLE);
        return chargingPileMapper.update(pile) > 0;
    }

    @Override
    @Transactional
    public boolean setParameters(String pileId, Double chargingPower) {
        return chargingPileMapper.updateParameters(pileId, chargingPower) > 0;
    }

    @Override
    @Transactional
    public boolean startChargingPile(String pileId) {
        ChargingPile pile = chargingPileMapper.findById(pileId);
        if (pile == null) {
            return false;
        }
        pile.setStatus(ChargingPileStatus.IN_USE);
        return chargingPileMapper.update(pile) > 0;
    }

    @Override
    @Transactional
    public boolean powerOff(String pileId) {
        ChargingPile pile = chargingPileMapper.findById(pileId);
        if (pile == null) {
            return false;
        }
        pile.setStatus(ChargingPileStatus.OFFLINE);
        return chargingPileMapper.update(pile) > 0;
    }

    @Override//重复不用
    public ChargingPile queryPileState(String pileId) {
        return chargingPileMapper.findById(pileId);
    }

    @Override
    public List<ChargingPileQueueDTO> getPileQueueInfo(String pileId) {
        return chargingPileMapper.findPileQueueInfo(pileId);
    }

    @Override
    public Map<String, List<ChargingPileQueueDTO>> getAllPileQueueInfo() {
        // 1. 从数据库获取所有充电桩的等候队列信息
        List<ChargingPileQueueDTO> allQueueInfo = chargingPileMapper.findAllPileQueueInfo();
        // 2. 将等候队列信息按充电桩ID分组
        return allQueueInfo.stream()
                .collect(Collectors.groupingBy(ChargingPileQueueDTO::getPileId));
    }


    @Override
    @Transactional
    public boolean startCharging(Long requestId, String pileId) {
        // 1. 检查充电桩状态
        ChargingPile pile = chargingPileMapper.findById(pileId);
        if (pile == null || pile.getStatus() != ChargingPileStatus.AVAILABLE) {
            return false;
        }
        System.out.println("充电桩状态：" + pile.getStatus());
        System.out.println("111111111111111111111111");

        // 2. 检查用户是否在充电区队列头
        Long queueHead = chargingPileQueueService.getQueueHead(pileId);
        System.out.println(queueHead);
        System.out.println(requestId);
        if (queueHead == null || !queueHead.equals(requestId)) {
            System.out.println("queueHead:" + queueHead);
            System.out.println("requestId:" + requestId);

            System.out.println("00000000000000000000");
            return false;
        }
        System.out.println("用户在充电区队列头：" + requestId);

        // 3. 更新充电桩状态为使用中
        pile.setStatus(ChargingPileStatus.IN_USE);
        System.out.println("222222222222");
        System.out.println(pile);
        chargingPileMapper.update(pile);

        // 4. 更新充电请求状态为充电中
        ChargingRequest request = chargingRequestMapper.findById(requestId);
        if (request != null) {
            request.setStatus(RequestStatus.CHARGING);
//            request.setChargingPileId(pileId); cly
            request.setChargingStartTime(LocalDateTime.now());
            System.out.println("开始充电时间");
            System.out.println(request.getChargingStartTime());
            chargingRequestMapper.update(request);
        }

        return true;
    }

    private double getChargingPower(String pileId) {
        // A和B为快充30kW，C、D、E为慢充7kW
        return (pileId.startsWith("A") || pileId.startsWith("B")) ? 30.0 : 7.0;
    }

    @Override
    @Transactional
    public boolean endCharging(Long userId, String pileId) {
        // 1. 检查充电桩状态
        ChargingPile pile = chargingPileMapper.findById(pileId);
        if (pile == null || pile.getStatus() != ChargingPileStatus.IN_USE) {
            return false;
        }

        // 2. 更新充电桩状态为空闲
        pile.setStatus(ChargingPileStatus.AVAILABLE);
        chargingPileMapper.update(pile);

        // 3. 更新充电请求状态为已完成
        ChargingRequest request = chargingRequestMapper.findByUserIdAndStatus(userId, "CHARGING");
        if (request != null) {
            request.setStatus(RequestStatus.COMPLETED);
            chargingRequestMapper.update(request);

            // 计算充电时长和充电量
            double chargingDurationMinutes = calculateChargingDuration(request);
            double chargingPower = getChargingPower(pileId);
            double totalKwh = (chargingDurationMinutes / 60.0) * chargingPower;

            // 4. 查找是否存在故障订单 eenndd
            ChargingOrder faultOrder = chargingOrderMapper.findByRequestIdAndStatus(request.getId(), OrderStatus.FAULTED);
            
            if (faultOrder != null) {
                // 4.1 存在故障订单，更新故障订单
                // 更新订单信息
                faultOrder.setOrderStatus(OrderStatus.COMPLETED);
                faultOrder.setEndTime(LocalDateTime.now());
                faultOrder.setTotalDuration(faultOrder.getTotalDuration().add(BigDecimal.valueOf(chargingDurationMinutes)));
                faultOrder.setTotalKwh(faultOrder.getTotalKwh().add(BigDecimal.valueOf(totalKwh)));
                
                // 生成故障后的详单
                List<ChargingDetail> newDetails = generateChargingDetails(faultOrder, pileId);
                calculateOrderTotalFees(faultOrder, newDetails);
                
                // 保存详单
                for (ChargingDetail detail : newDetails) {
                    chargingDetailMapper.insert(detail);
                }
                
                // 更新订单
                chargingOrderMapper.update(faultOrder);
            } else {
                // 4.2 不存在故障订单，按正常流程处理
                // 生成充电订单
                ChargingOrder order = new ChargingOrder();
                order.setOrderId(generateOrderId());
                order.setUserId(userId);
                order.setCarId(userMapper.selectById(userId).getCarNumber());
                order.setRequestId(request.getId());
                order.setPileId(pileId);
                order.setOrderDate(LocalDate.now());
                order.setOrderStatus(OrderStatus.COMPLETED);
                order.setStartTime(request.getChargingStartTime());
                order.setEndTime(LocalDateTime.now());
                order.setCreateTime(LocalDateTime.now());
                order.setTotalDuration(BigDecimal.valueOf(chargingDurationMinutes));
                order.setTotalKwh(BigDecimal.valueOf(totalKwh));

                // 生成详单并计算费用
                List<ChargingDetail> details = generateChargingDetails(order, pileId);
                calculateOrderTotalFees(order, details);
                
                // 保存订单和详单
                chargingOrderMapper.insert(order);
                for (ChargingDetail detail : details) {
                    chargingDetailMapper.insert(detail);
                }
            }

            // 5. 更新充电桩统计数据
            // 更新充电次数
            pile.setChargingTimes(pile.getChargingTimes() + 1);
            // 更新总充电时长（分钟转小时）
            double newTotalDuration = pile.getTotalChargingDuration() + (chargingDurationMinutes / 60.0);
            pile.setTotalChargingDuration(newTotalDuration);
            // 更新总充电量
            double newTotalAmount = pile.getTotalChargingAmount() + totalKwh;
            pile.setTotalChargingAmount(newTotalAmount);
            // 更新更新时间
            pile.setUpdatedAt(LocalDateTime.now());
            // 保存更新
            chargingPileMapper.update(pile);
        }
        return true;
    }

    private String generateOrderId() {
        // 生成订单号：ORD + 年月日 + 4位随机数
        return String.format("ORD%s%04d", 
            LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")),
            new Random().nextInt(10000));
    }

    private double calculateChargingDuration(ChargingRequest request) {
        // 计算充电时长（分钟）
        if (request.getChargingStartTime() == null) {
            // 如果开始充电时间为空，使用加入队列时间作为开始时间
            return ChronoUnit.MINUTES.between(request.getQueueJoinTime(), LocalDateTime.now());
        }
        return ChronoUnit.MINUTES.between(request.getChargingStartTime(), LocalDateTime.now());
    }

    //生成详单
    private List<ChargingDetail> generateChargingDetails(ChargingOrder order, String pileId) {
        List<ChargingDetail> details = new ArrayList<>();
        LocalDateTime startTime = order.getStartTime();
        LocalDateTime endTime = order.getEndTime();
        double remainingKwh = order.getTotalKwh().doubleValue();
        int periodSeq = 1;

        // 获取所有电价时段
        List<PricePeriod> pricePeriods = getPricePeriods();
        
        // 按时间顺序遍历每个时段
        for (PricePeriod period : pricePeriods) {
            LocalDateTime periodStart = getDateTimeForTime(period.getStart());
            LocalDateTime periodEnd = getDateTimeForTime(period.getEnd());
            
            // 如果充电时间与当前时段有重叠
            if (startTime.isBefore(periodEnd) && endTime.isAfter(periodStart)) {
                // 计算重叠时间段的开始和结束时间
                LocalDateTime overlapStart = startTime.isAfter(periodStart) ? startTime : periodStart;
                LocalDateTime overlapEnd = endTime.isBefore(periodEnd) ? endTime : periodEnd;
                
                // 计算该时段的实际充电时长（分钟）
                long actualMinutes = ChronoUnit.MINUTES.between(overlapStart, overlapEnd);
                System.out.println("实际充电时长");
                System.out.println(actualMinutes);
                
                // 计算充电量（根据充电桩类型设置功率：A和B为快充30kW，C、D、E为慢充7kW）
                double chargingPower = getChargingPower(pileId);
                System.out.println("功率");
                System.out.println(chargingPower);
                
                // 将分钟转换为小时用于计算充电量
                double billingHours = actualMinutes / 60.0;
                double kwh = billingHours * chargingPower;
                System.out.println("kwh");
                System.out.println(kwh);
                
                if (kwh > remainingKwh) {
                    kwh = remainingKwh;
                }
                remainingKwh -= kwh;
                System.out.println("kwh");
                System.out.println(kwh);

                // 创建详单
                ChargingDetail detail = new ChargingDetail();
                detail.setOrderId(order.getOrderId());
                detail.setPeriodSeq(periodSeq++);
                detail.setPeriodType(period.getType());
                detail.setStartTime(overlapStart);
                detail.setEndTime(overlapEnd);
                detail.setDuration(BigDecimal.valueOf(actualMinutes));
                detail.setKwh(BigDecimal.valueOf(kwh));
                detail.setChargeRate(BigDecimal.valueOf(period.getPrice()));
                detail.setServiceRate(BigDecimal.valueOf(0.8)); // 服务费单价固定为0.8元/度
                
                // 计算该时段的费用
                BigDecimal chargeFee = detail.getKwh().multiply(detail.getChargeRate());
                BigDecimal serviceFee = detail.getKwh().multiply(detail.getServiceRate());
                detail.setChargeFee(chargeFee);
                detail.setServiceFee(serviceFee);
                detail.setSubTotal(chargeFee.add(serviceFee));
                
                System.out.println("--------------------------");
                System.out.println(detail);
                details.add(detail);
                
                if (remainingKwh <= 0) {
                    break;
                }
            }
        }

        return details;
    }

    private void calculateOrderTotalFees(ChargingOrder order, List<ChargingDetail> details) {
        BigDecimal totalChargeFee = BigDecimal.ZERO;
        BigDecimal totalServiceFee = BigDecimal.ZERO;
        
        for (ChargingDetail detail : details) {
            totalChargeFee = totalChargeFee.add(detail.getChargeFee());
            totalServiceFee = totalServiceFee.add(detail.getServiceFee());
        }
        
        order.setTotalChargeFee(totalChargeFee);
        order.setTotalServiceFee(totalServiceFee);
        order.setTotalFee(totalChargeFee.add(totalServiceFee));
    }

    private LocalDateTime getDateTimeForTime(String timeStr) {
        LocalTime time = LocalTime.parse(timeStr);
        LocalDate today = LocalDate.now();
        return LocalDateTime.of(today, time);
    }

    private List<PricePeriod> getPricePeriods() {
        // 从配置文件中读取电价时段
        return Arrays.asList(
            new PricePeriod(1, PeriodType.STANDARD, "07:00:00", "10:00:00", 0.70),
            new PricePeriod(2, PeriodType.PEAK, "10:00:00", "15:00:00", 1.00),
            new PricePeriod(3, PeriodType.STANDARD, "15:00:00", "18:00:00", 0.70),
            new PricePeriod(4, PeriodType.PEAK, "18:00:00", "21:00:00", 1.00),
            new PricePeriod(5, PeriodType.STANDARD, "21:00:00", "23:00:00", 0.70),
            new PricePeriod(6, PeriodType.VALLEY, "23:00:00", "07:00:00", 0.40)
        );
    }

    @Override
    public List<ChargingReportDTO> getChargingReport(String pileId, String timeType) {
        LocalDateTime start;
        LocalDateTime end;
        LocalDateTime now = LocalDateTime.now();

        // 根据时间类型设置查询时间范围
        switch (timeType.toLowerCase()) {
            case "day":
                start = now.toLocalDate().atStartOfDay();
                end = start.plusDays(1).minusSeconds(1);
                break;
            case "week":
                start = now.toLocalDate().with(java.time.DayOfWeek.MONDAY).atStartOfDay();
                end = start.plusWeeks(1).minusSeconds(1);
                break;
            case "month":
                start = now.toLocalDate().withDayOfMonth(1).atStartOfDay();
                end = start.plusMonths(1).minusSeconds(1);
                break;
            default:
                throw new IllegalArgumentException("Invalid time type: " + timeType);
        }

        return chargingReportMapper.getChargingReportByPileId(pileId, start, end);
    }

    @Override
    public ChargingReportSummaryDTO getChargingReportSummary(String timeType) {
        LocalDateTime start;
        LocalDateTime end;
        LocalDateTime now = LocalDateTime.now();

        // 根据时间类型设置查询时间范围
        switch (timeType.toLowerCase()) {
            case "day":
                start = now.toLocalDate().atStartOfDay();
                end = start.plusDays(1).minusSeconds(1);
                break;
            case "week":
                start = now.toLocalDate().with(java.time.DayOfWeek.MONDAY).atStartOfDay();
                end = start.plusWeeks(1).minusSeconds(1);
                break;
            case "month":
                start = now.toLocalDate().withDayOfMonth(1).atStartOfDay();
                end = start.plusMonths(1).minusSeconds(1);
                break;
            default:
                throw new IllegalArgumentException("Invalid time type: " + timeType);
        }

        // 获取总体汇总数据
        ChargingReportDTO summary = chargingReportMapper.getChargingReportSummary(start, end);
        
        // 获取所有充电桩的详细数据
        List<ChargingReportDTO> pileDetails = chargingReportMapper.getChargingReportByPileId(null, start, end);

        // 组装汇总报表数据
        ChargingReportSummaryDTO result = new ChargingReportSummaryDTO();
        result.setSummary(summary);
        result.setPileDetails(pileDetails);

        return result;
    }

    @Override
    public List<ChargingQueueInfoDTO> getChargingQueueDetails(String pileId) {
        // 获取充电桩的等候队列
        Queue<Long> queue = chargingPileQueueService.getQueueRequests(pileId);
        if (queue == null || queue.isEmpty()) {
            System.out.println("充电桩队列为空1111111111111111");
            return new ArrayList<>();
        }
        System.out.println("充电桩队列：");
        System.out.println(queue);
        
        // 转换为DTO
        List<ChargingQueueInfoDTO> result = new ArrayList<>();
        int position = 1;
        
        // 跳过第一个请求（正在充电的）
        boolean isFirst = true;
        for (Long requestId : queue) {
            if (isFirst) {
                isFirst = false;
                continue;
            }
            
            ChargingRequest request = chargingRequestMapper.findById(requestId);
            if (request != null) {
                User user = userMapper.selectById(request.getUserId());
                if (user != null) {
                    ChargingQueueInfoDTO dto = new ChargingQueueInfoDTO();
                    dto.setPileId(pileId);
                    dto.setUserId(request.getUserId());
                    dto.setCarNumber(user.getCarNumber());
                    dto.setBatteryCapacity(user.getBatteryCapacity());
                    dto.setRequestAmount(request.getAmount());
                    dto.setQueueJoinTime(request.getQueueJoinTime());
                    dto.setWaitingTime(schedulingService.calculateWaitingTime(pileId));
                    dto.setQueuePosition(position++);
                    result.add(dto);
                }
            }
        }
        
        return result;
    }

    @Override
    public List<ChargingPile> findAll() {
        return chargingPileMapper.findAll();
    }
} 