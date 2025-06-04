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
import com.online.chargingSystem.mapper.*;
import com.online.chargingSystem.service.ChargingPileService;
import com.online.chargingSystem.service.ChargingPileQueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    public ChargingPile getPileStatus(String pileId) {
        return chargingPileMapper.findById(pileId);
    }

    @Override
    @Transactional
    public boolean reportFault(String pileId, String faultType, String description) {
        ChargingPile pile = chargingPileMapper.findById(pileId);
        if (pile == null) {
            return false;
        }
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
            chargingRequestMapper.update(request);
        }

        return true;
    }


    @Override
    @Transactional
    public boolean endCharging(Long userId, String pileId) {
        // 1. 检查充电桩状态
        ChargingPile pile = chargingPileMapper.findById(pileId);
        if (pile == null || pile.getStatus() != ChargingPileStatus.IN_USE) {
            return false;
        }
        System.out.println("充电桩状态：" + pile.getStatus());

        // 2. 更新充电桩状态为空闲
        pile.setStatus(ChargingPileStatus.AVAILABLE);
        chargingPileMapper.update(pile);
        System.out.println("充电桩状态更新为：" + pile.getStatus());

        // 3. 更新充电请求状态为已完成
        ChargingRequest request = chargingRequestMapper.findByUserIdAndStatus(userId, "CHARGING");
        if (request != null) {
            request.setStatus(RequestStatus.COMPLETED);
            chargingRequestMapper.update(request);
            System.out.println("充电请求状态更新为：" + request.getStatus());

            // 4. 生成充电订单
            ChargingOrder order = new ChargingOrder();
            order.setOrderId(generateOrderId());
            order.setUserId(userId);
            // 添加插入car_id -cly
            order.setCarId(userMapper.selectById(userId).getCarNumber());
            order.setRequestId(request.getId());
            order.setPileId(pileId);
            order.setOrderDate(LocalDate.now());
            order.setOrderStatus(OrderStatus.COMPLETED);
            order.setStartTime(request.getQueueJoinTime());
            order.setEndTime(LocalDateTime.now());
            order.setCreateTime(LocalDateTime.now());

            // 5. 计算充电时长和充电量
            double chargingDuration = calculateChargingDuration(request);
            // double totalKwh = request.getAmount(); // 原来的计算方式，暂时保留
            // 根据充电桩类型计算总充电量
            double chargingPower = pileId.startsWith("F") ? 30.0 : 7.0; // F开头为快充，其他为慢充
            double totalKwh = chargingDuration * chargingPower;
            order.setTotalDuration(BigDecimal.valueOf(chargingDuration));
            order.setTotalKwh(BigDecimal.valueOf(totalKwh));

            // 6. 生成详单并计算费用
            List<ChargingDetail> details = generateChargingDetails(order, pileId);
            
            // 7. 计算订单总费用
            calculateOrderTotalFees(order, details);
            
            // 8. 保存订单和详单
            chargingOrderMapper.insert(order);
            for (ChargingDetail detail : details) {
                chargingDetailMapper.insert(detail);
            }
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
        // 计算充电时长（小时）
        return ChronoUnit.HOURS.between(request.getQueueJoinTime(), LocalDateTime.now());
    }

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
                
                // 计算该时段的充电时长（小时）
                double duration = ChronoUnit.MINUTES.between(overlapStart, overlapEnd) / 60.0;
                
                // 计算充电量（根据充电桩类型设置功率：快充30kW，慢充7kW）
                double chargingPower = pileId.startsWith("F") ? 30.0 : 7.0; // F开头为快充，其他为慢充
                double kwh = duration * chargingPower;
                if (kwh > remainingKwh) {
                    kwh = remainingKwh;
                }
                remainingKwh -= kwh;

                // 创建详单
                ChargingDetail detail = new ChargingDetail();
                detail.setOrderId(order.getOrderId());
                detail.setPeriodSeq(periodSeq++);
                detail.setPeriodType(period.getType());
                detail.setStartTime(overlapStart);
                detail.setEndTime(overlapEnd);
                detail.setDuration(BigDecimal.valueOf(duration));
                detail.setKwh(BigDecimal.valueOf(kwh));
                detail.setChargeRate(BigDecimal.valueOf(period.getPrice()));
                detail.setServiceRate(BigDecimal.valueOf(0.8)); // 服务费单价固定为0.8元/度
                
                // 计算该时段的费用
                BigDecimal chargeFee = detail.getKwh().multiply(detail.getChargeRate());
                BigDecimal serviceFee = detail.getKwh().multiply(detail.getServiceRate());
                detail.setChargeFee(chargeFee);
                detail.setServiceFee(serviceFee);
                detail.setSubTotal(chargeFee.add(serviceFee));
                
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
} 