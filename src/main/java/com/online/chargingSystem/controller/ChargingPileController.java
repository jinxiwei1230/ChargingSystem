package com.online.chargingSystem.controller;

import com.online.chargingSystem.common.Result;
import com.online.chargingSystem.dto.ChargingQueueInfoDTO;
import com.online.chargingSystem.service.ChargingPileService;
import com.online.chargingSystem.service.ChargingProgressService;
import com.online.chargingSystem.service.ChargingPileQueueService;
import com.online.chargingSystem.entity.ChargingPile;
import com.online.chargingSystem.entity.ChargingRequest;
import com.online.chargingSystem.entity.ChargingOrder;
import com.online.chargingSystem.entity.PricePeriod;
import com.online.chargingSystem.entity.User;
import com.online.chargingSystem.entity.enums.OrderStatus;
import com.online.chargingSystem.entity.enums.RequestStatus;
import com.online.chargingSystem.entity.enums.PeriodType;
import com.online.chargingSystem.mapper.ChargingRequestMapper;
import com.online.chargingSystem.mapper.ChargingOrderMapper;
import com.online.chargingSystem.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import lombok.Data;
import com.online.chargingSystem.dto.ChargingReportDTO;
import com.online.chargingSystem.dto.ChargingReportSummaryDTO;
import com.online.chargingSystem.entity.ChargingDetail;
import com.online.chargingSystem.mapper.ChargingDetailMapper;

import java.util.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.time.format.DateTimeFormatter;

/**
 * 充电桩控制器
 * 处理充电桩相关的请求，包括查询充电桩状态、故障报告、故障恢复等
 */
@RestController
@RequestMapping("/api/charging-pile")
public class ChargingPileController {

    private static final Logger logger = LoggerFactory.getLogger(ChargingPileController.class);

    @Autowired
    private ChargingPileService chargingPileService;

    @Autowired
    private ChargingProgressService chargingProgressService;

    @Autowired
    private ChargingPileQueueService chargingPileQueueService;

    @Autowired
    private ChargingRequestMapper chargingRequestMapper;

    @Autowired
    private ChargingOrderMapper chargingOrderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ChargingDetailMapper chargingDetailMapper;

    /**
     * 获取充电桩状态
     * @param pileId 充电桩ID
     * @return 充电桩状态信息
     */
    @GetMapping("/status")
    public Result<?> getPileStatus(@RequestParam String pileId) {
        ChargingPile result = chargingPileService.getPileStatus(pileId);
        return result != null ? Result.success(result) : Result.error("充电桩不存在");
    }

    /**
     * 报告充电桩故障
     * @param request 故障报告请求
     * @return 故障报告结果
     */
    @PostMapping("/fault/report")
    public Result<?> reportFault(@RequestBody FaultReportRequest request) {
        boolean result = chargingPileService.reportFault(request.getPileId(), request.getFaultType(), request.getDescription());
        return result ? Result.success("故障报告成功") : Result.error("故障报告失败");
    }

    /**
     * 恢复充电桩
     * @param request 故障恢复请求
     * @return 恢复结果
     */
    @PostMapping("/fault/resolve")
    public Result<?> resolveFault(@RequestBody FaultResolveRequest request) {
        boolean result = chargingPileService.resolveFault(request.getPileId(), request.getResolution());
        return result ? Result.success("故障恢复成功") : Result.error("故障恢复失败");
    }

    /**
     * 开启充电桩
     * @param pileId 充电桩ID
     * @return 开启结果
     */
    @PostMapping("/power-on")
    public Result<?> powerOn(@RequestParam String pileId) {
        boolean result = chargingPileService.powerOn(pileId);
        return result ? Result.success("开机成功") : Result.error("开机失败");
    }

    /**
     * 设置充电桩参数
     * @param request 参数设置请求
     * @return 设置结果
     */
    @PostMapping("/set-parameters")
    public Result<?> setParameters(@RequestBody ParametersRequest request) {
        boolean result = chargingPileService.setParameters(request.getPileId(), request.getChargingPower());
        return result ? Result.success("参数设置成功") : Result.error("参数设置失败");
    }

    /**
     * 启动充电桩
     * @param pileId 充电桩ID
     * @return 启动结果
     */
    @PostMapping("/start")
    public Result<?> startChargingPile(@RequestParam String pileId) {
        boolean result = chargingPileService.startChargingPile(pileId);
        return result ? Result.success("启动成功") : Result.error("启动失败");
    }

    /**
     * 关闭充电桩
     * @param pileId 充电桩ID
     * @return 关闭结果
     */
    @PostMapping("/power-off")
    public Result<?> powerOff(@RequestParam String pileId) {
        boolean result = chargingPileService.powerOff(pileId);
        return result ? Result.success("关机成功") : Result.error("关机失败");
    }

    /**
     * 查询充电桩状态
     * @param pileId 充电桩ID
     * @return 充电桩状态信息
     */
    @GetMapping("/state")
    public Result<?> queryPileState(@RequestParam String pileId) {
        ChargingPile result = chargingPileService.queryPileState(pileId);
        return result != null ? Result.success(result) : Result.error("查询失败");
    }

    /**
     * 开始充电（队列头+空闲充电桩）
     * @param userId 用户ID
     * @param pileId 充电桩ID
     * @return 操作结果
     */
    @PostMapping("/charging/start")
    public Result<?> startCharging(@RequestParam Long userId,
                                  @RequestParam String pileId) {
        boolean result = chargingPileService.startCharging(userId, pileId);
        return result ? Result.success("开始充电成功") : Result.error("开始充电失败");
    }

    /**
     * 结束充电（让充电桩空闲、生成详单、订单）
     * @param userId 用户ID
     * @param pileId 充电桩ID
     * @return 操作结果
     */
    @PostMapping("/charging/end")
    public Result<?> endCharging(@RequestParam Long userId,
                                @RequestParam String pileId) {
        boolean result = chargingPileService.endCharging(userId, pileId);
        return result ? Result.success("结束充电成功") : Result.error("结束充电失败");
    }

    /**
     * 获取充电桩报表数据
     * @param pileId 充电桩ID（可选）
     * @param timeType 时间类型（day/week/month）
     * @return 报表数据
     */
    @GetMapping("/report")
    public Result<List<ChargingReportDTO>> getChargingReport(
            @RequestParam(required = false) String pileId,
            @RequestParam String timeType) {
        List<ChargingReportDTO> result = chargingPileService.getChargingReport(pileId, timeType);
        return Result.success(result);
    }

    /**
     * 获取所有充电桩的汇总报表数据
     * @param timeType 时间类型（day/week/month）
     * @return 汇总报表数据（包含总体数据和每个充电桩的详细数据）
     */
    @GetMapping("/report/summary")
    public Result<ChargingReportSummaryDTO> getChargingReportSummary(
            @RequestParam String timeType) {
        ChargingReportSummaryDTO result = chargingPileService.getChargingReportSummary(timeType);
        return Result.success(result);
    }

    /**
     * 获取所有充电桩信息
     * @return 所有充电桩信息列表
     */
    @GetMapping("/all")
    public Result<List<ChargingPile>> getAllChargingPiles() {
        List<ChargingPile> result = chargingPileService.findAll();
        return Result.success(result);
    }

    /**
     * 获取所有充电桩的等候队列信息（按充电桩分类）
     * @return 按充电桩分类的等候队列信息
     */
    @GetMapping("/all-queues")
    public Result<Map<String, List<ChargingQueueInfoDTO>>> getAllChargingQueues() {
        Map<String, List<ChargingQueueInfoDTO>> result = chargingPileService.getAllChargingQueues();
        return Result.success(result);
    }

    
    /**
     * 获取充电桩等候队列详细信息
     * @param pileId 充电桩ID
     * @return 等候队列详细信息
     */
    @GetMapping("/queue-details")
    public Result<List<ChargingQueueInfoDTO>> getChargingQueueDetails(@RequestParam String pileId) {
        List<ChargingQueueInfoDTO> result = chargingPileService.getChargingQueueDetails(pileId);
        return Result.success(result);
    }

    /**
     * 获取所有充电桩队列中所有请求（包括正在充电）的充电量信息
     * @return 每个充电桩队列中所有请求的充电量信息
     */
    @GetMapping("/all-queue-charging-status")
    public Result<?> getAllQueueChargingStatus() {
        logger.info("开始获取所有充电桩队列的完整充电状态信息");
        
        // 获取所有充电桩
        List<ChargingPile> allPiles = chargingPileService.findAll();
        Map<String, List<Map<String, Object>>> result = new HashMap<>();
        
        // 遍历每个充电桩
        for (ChargingPile pile : allPiles) {
            String pileId = pile.getId();
            List<Map<String, Object>> chargingStatusList = new ArrayList<>();
            
            // 获取充电桩的等候队列
            Queue<Long> queue = chargingPileQueueService.getQueueRequests(pileId);
            
            logger.info("充电桩 {} 的完整队列信息：", pileId);
            
            if (queue != null && !queue.isEmpty()) {
                int position = 1;
                // 遍历所有请求，包括正在充电的
                for (Long requestId : queue) {
                    ChargingRequest request = chargingRequestMapper.findById(requestId);
                    if (request != null) {
                        User user = userMapper.selectById(request.getUserId());
                        if (user != null) {
                            // 获取当前充电量
                            double currentPower = chargingProgressService.getCurrentChargingPower(request.getId());
                            double targetPower = request.getAmount();
                            double percentage = 0.0;
                            
                            // 计算当前费用
                            BigDecimal totalChargeFee = BigDecimal.ZERO;
                            BigDecimal totalServiceFee = BigDecimal.ZERO;
                            BigDecimal totalFee = BigDecimal.ZERO;
                            double totalChargedPower = 0.0;
                            
                            // 检查是否存在故障订单
                            ChargingOrder faultOrder = chargingOrderMapper.findByRequestIdAndStatus(request.getId(), OrderStatus.FAULTED);
                            if (faultOrder != null) {
                                // 直接使用故障订单的费用和充电量
                                totalChargeFee = totalChargeFee.add(faultOrder.getTotalChargeFee());
                                totalServiceFee = totalServiceFee.add(faultOrder.getTotalServiceFee());
                                totalFee = totalFee.add(faultOrder.getTotalFee());
                                totalChargedPower = faultOrder.getTotalKwh().doubleValue();
                            }
                            
                            // 计算当前时段的费用
                            if (request.getStatus() == RequestStatus.CHARGING) {
                                // 获取所有电价时段
                                List<PricePeriod> pricePeriods = Arrays.asList(
                                    new PricePeriod(1, PeriodType.STANDARD, "07:00:00", "10:00:00", 0.70),
                                    new PricePeriod(2, PeriodType.PEAK, "10:00:00", "15:00:00", 1.00),
                                    new PricePeriod(3, PeriodType.STANDARD, "15:00:00", "18:00:00", 0.70),
                                    new PricePeriod(4, PeriodType.PEAK, "18:00:00", "21:00:00", 1.00),
                                    new PricePeriod(5, PeriodType.STANDARD, "21:00:00", "23:00:00", 0.70),
                                    new PricePeriod(6, PeriodType.VALLEY, "23:00:00", "07:00:00", 0.40)
                                );
                                
                                // 获取充电开始时间和当前时间
                                LocalDateTime startTime = request.getChargingStartTime();
                                LocalDateTime endTime = LocalDateTime.now();
                                
                                // 获取充电桩信息
                                ChargingPile chargingPile = chargingPileService.getPileStatus(pileId);
                                if (chargingPile != null) {
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
                                            
                                            // 使用充电桩的实际功率
                                            double chargingPower = chargingPile.getChargingPower();
                                            double billingHours = actualMinutes / 60.0;
                                            double kwh = billingHours * chargingPower;
                                            
                                            // 计算该时段的费用
                                            BigDecimal chargeFee = BigDecimal.valueOf(kwh * period.getPrice());
                                            BigDecimal serviceFee = BigDecimal.valueOf(kwh * 0.8); // 服务费单价固定为0.8元/度
                                            
                                            totalChargeFee = totalChargeFee.add(chargeFee);
                                            totalServiceFee = totalServiceFee.add(serviceFee);
                                            totalFee = totalChargeFee.add(totalServiceFee);
                                            totalChargedPower += kwh;
                                            
                                            logger.info("时段费用计算: 时段={}-{}, 充电量={}kWh, 电价={}元/度, 电费={}元, 服务费={}元",
                                                period.getStart(), period.getEnd(), kwh, period.getPrice(), chargeFee, serviceFee);
                                        }
                                    }
                                }
                            }
                            
                            if (targetPower > 0) {
                                percentage = (totalChargedPower / targetPower) * 100;
                                percentage = java.lang.Math.min(percentage, 100.0);
                            }
                            
                            Map<String, Object> chargingStatus = new HashMap<>();
                            chargingStatus.put("userId", request.getUserId());
                            chargingStatus.put("carNumber", user.getCarNumber());
                            chargingStatus.put("currentPower", String.format("%.2f", totalChargedPower));
                            chargingStatus.put("targetPower", String.format("%.2f", targetPower));
                            chargingStatus.put("percentage", String.format("%.1f", percentage));
                            chargingStatus.put("queuePosition", position++);
                            chargingStatus.put("status", request.getStatus().name());
                            chargingStatus.put("totalChargeFee", String.format("%.2f", totalChargeFee));
                            chargingStatus.put("totalServiceFee", String.format("%.2f", totalServiceFee));
                            chargingStatus.put("totalFee", String.format("%.2f", totalFee));
                            
                            chargingStatusList.add(chargingStatus);
                            
                            // 记录每个请求的充电状态
                            logger.info("充电桩 {} - 用户ID: {}, 车牌号: {}, 状态: {}, 当前充电量: {}kWh, 目标充电量: {}kWh, 完成度: {}%, 队列位置: {}, 电费: {}元, 服务费: {}元, 总费用: {}元", 
                                pileId,
                                request.getUserId(),
                                user.getCarNumber(),
                                request.getStatus().name(),
                                String.format("%.2f", totalChargedPower),
                                String.format("%.2f", targetPower),
                                String.format("%.1f", percentage),
                                position - 1,
                                String.format("%.2f", totalChargeFee),
                                String.format("%.2f", totalServiceFee),
                                String.format("%.2f", totalFee));
                        }
                    }
                }
            }
            
            result.put(pileId, chargingStatusList);
            logger.info("充电桩 {} 共有 {} 个请求在队列中", pileId, chargingStatusList.size());
        }
        
        logger.info("所有充电桩队列完整状态获取完成");
        return Result.success(result);
    }

    private LocalDateTime getDateTimeForTime(String timeStr) {
        LocalTime time = LocalTime.parse(timeStr);
        LocalDate today = LocalDate.now();
        return LocalDateTime.of(today, time);
    }


}

@Data
class PowerRequest {
    private String pileId;
}

@Data
class ParametersRequest {
    private String pileId;
    private Double chargingPower;
}

@Data
class FaultReportRequest {
    private String pileId;
    private String faultType;
    private String description;
}

@Data
class FaultResolveRequest {
    private String pileId;
    private String resolution;
} 