package com.online.chargingsystem.controller;

import com.online.chargingsystem.common.Result;
import com.online.chargingsystem.service.DetailedListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 充电详单控制器
 * 处理充电详单相关的请求，包括查询充电详单信息等
 */
@RestController
@RequestMapping("/api/detailed-list")
public class DetailedListController {

    @Autowired
    private DetailedListService detailedListService;

    /**
     * 获取充电详单
     * 根据账单ID获取充电详单信息
     * @param billId 账单ID
     * @return 充电详单信息
     */
    @GetMapping("/info")
    public Result<?> requestDetailedList(@RequestParam String billId) {
        Object result = detailedListService.requestDetailedList(billId);
        return result != null ? Result.success(result) : Result.error("获取充电详单失败");
    }
} 