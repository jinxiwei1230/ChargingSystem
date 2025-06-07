package com.online.chargingSystem.controller;

import com.online.chargingSystem.common.Result;
import com.online.chargingSystem.entity.ChargingPile;
import com.online.chargingSystem.service.ChargingPileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import lombok.Data;
import com.online.chargingSystem.dto.ChargingPileQueueDTO;
import com.online.chargingSystem.dto.ChargingReportDTO;
import com.online.chargingSystem.dto.ChargingReportSummaryDTO;
import com.online.chargingSystem.dto.ChargingQueueInfoDTO;

import java.util.List;
import java.util.Map;

/**
 * 充电桩控制器
 * 处理充电桩相关的请求，包括查询充电桩状态、故障报告、故障恢复等
 */
@RestController
@RequestMapping("/api/charging-pile")
public class ChargingPileController {

    @Autowired
    private ChargingPileService chargingPileService;

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
     * 获取所有充电桩的等候队列信息
     * @return 所有充电桩的等候队列信息
     */
    @GetMapping("/queue-info")
    public Result<Map<String, List<ChargingPileQueueDTO>>> getAllPileQueueInfo() {
        Map<String, List<ChargingPileQueueDTO>> result = chargingPileService.getAllPileQueueInfo();
        return Result.success(result);
    }

    /**
     * 获取指定充电桩的等候队列信息
     * @param pileId 充电桩ID
     * @return 指定充电桩的等候队列信息
     */
    @GetMapping("/{pileId}/queue-info")
    public Result<List<ChargingPileQueueDTO>> getPileQueueInfo(@PathVariable String pileId) {
        List<ChargingPileQueueDTO> result = chargingPileService.getPileQueueInfo(pileId);
        return Result.success(result);
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