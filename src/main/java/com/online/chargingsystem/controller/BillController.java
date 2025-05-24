package com.online.chargingsystem.controller;

import com.online.chargingsystem.common.Result;
import com.online.chargingsystem.service.BillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 账单控制器
 * 处理账单相关的请求，包括查询账单信息等
 */
@RestController
@RequestMapping("/api/bill")
public class BillController {

    @Autowired
    private BillService billService;

    /**
     * 获取账单信息
     * 根据账单ID获取账单详细信息
     * @param billId 账单ID
     * @return 账单信息，包含充电费用、时长等
     */
    @GetMapping("/info")
    public Result<?> getBillInfo(@RequestParam String billId) {
        Object result = billService.getBillInfo(billId);
        return result != null ? Result.success(result) : Result.error("获取账单信息失败");
    }
} 