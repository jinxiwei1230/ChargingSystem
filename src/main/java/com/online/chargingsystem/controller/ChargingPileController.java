package com.online.chargingsystem.controller;

import com.online.chargingsystem.common.Result;
import com.online.chargingsystem.service.ChargingPileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
     * 查询指定充电桩的当前状态
     * @param pileId 充电桩ID
     * @return 充电桩状态信息
     */
    @GetMapping("/status")
    public Result<?> getPileStatus(@RequestParam String pileId) {
        Object result = chargingPileService.getPileStatus(pileId);
        return result != null ? Result.success(result) : Result.error("充电桩不存在");
    }

    /**
     * 报告充电桩故障
     * 记录充电桩故障信息并触发故障处理流程
     * @param pileId 充电桩ID
     * @param faultType 故障类型
     * @param description 故障描述
     * @return 故障报告结果
     */
    @PostMapping("/fault/report")
    public Result<?> reportFault(@RequestParam String pileId,
                            @RequestParam String faultType,
                            @RequestParam String description) {
        Object result = chargingPileService.reportFault(pileId, faultType, description);
        return result != null ? Result.success("故障报告成功", result) : Result.error("故障报告失败");
    }

    /**
     * 恢复充电桩
     * 将故障充电桩恢复到正常工作状态
     * @param pileId 充电桩ID
     * @param resolution 故障解决方案
     * @return 恢复结果
     */
    @PostMapping("/fault/resolve")
    public Result<?> resolveFault(@RequestParam String pileId,
                             @RequestParam String resolution) {
        Object result = chargingPileService.resolveFault(pileId, resolution);
        return result != null ? Result.success("故障恢复成功", result) : Result.error("故障恢复失败");
    }

    @PostMapping("/power-on")
    public Result<?> powerOn(@RequestParam String pileId) {
        Object result = chargingPileService.powerOn(pileId);
        return result != null ? Result.success("开机成功", result) : Result.error("开机失败");
    }

    @PostMapping("/set-parameters")
    public Result<?> setParameters(@RequestBody Object parameters) {
        Object result = chargingPileService.setParameters(parameters);
        return result != null ? Result.success("参数设置成功", result) : Result.error("参数设置失败");
    }

    @PostMapping("/start")
    public Result<?> startChargingPile(@RequestParam String pileId) {
        Object result = chargingPileService.startChargingPile(pileId);
        return result != null ? Result.success("启动成功", result) : Result.error("启动失败");
    }

    @PostMapping("/power-off")
    public Result<?> powerOff(@RequestParam String pileId) {
        Object result = chargingPileService.powerOff(pileId);
        return result != null ? Result.success("关机成功", result) : Result.error("关机失败");
    }

    @GetMapping("/state")
    public Result<?> queryPileState(@RequestParam String pileId) {
        Object result = chargingPileService.queryPileState(pileId);
        return result != null ? Result.success(result) : Result.error("查询失败");
    }
} 