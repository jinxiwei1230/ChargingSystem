# 充电系统接口文档

## 获取所有充电桩队列充电状态

### 接口描述
获取所有充电桩队列中所有请求（包括正在充电）的充电量信息和费用信息。

### 请求信息
- 请求路径：`/api/charging-pile/all-queue-charging-status`
- 请求方法：GET
- 请求参数：无

### 响应信息
```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "A": [
            {
                "userId": 1,
                "carNumber": "京A12345",
                "currentPower": "0.35",
                "targetPower": "2.00",
                "percentage": "17.5",
                "queuePosition": 1,
                "status": "CHARGING",
                "totalChargeFee": "1.40",
                "totalServiceFee": "0.28",
                "totalFee": "1.68"
            }
        ],
        "B": [],
        "C": []
    }
}
```

### 响应字段说明
| 字段名 | 类型 | 说明 |
|--------|------|------|
| code | Integer | 响应码，200表示成功 |
| message | String | 响应消息 |
| data | Object | 响应数据 |
| data.{pileId} | Array | 充电桩ID对应的队列信息 |
| data.{pileId}[].userId | Long | 用户ID |
| data.{pileId}[].carNumber | String | 车牌号 |
| data.{pileId}[].currentPower | String | 当前充电量（千瓦时） |
| data.{pileId}[].targetPower | String | 目标充电量（千瓦时） |
| data.{pileId}[].percentage | String | 充电完成百分比 |
| data.{pileId}[].queuePosition | Integer | 队列位置 |
| data.{pileId}[].status | String | 充电状态（CHARGING/WAITING等） |
| data.{pileId}[].totalChargeFee | String | 电费（元） |
| data.{pileId}[].totalServiceFee | String | 服务费（元） |
| data.{pileId}[].totalFee | String | 总费用（元） |

### 费用计算说明
1. 电费计算：
   - 07:00-10:00：0.70元/度
   - 10:00-15:00：1.00元/度
   - 15:00-18:00：0.70元/度
   - 18:00-21:00：1.00元/度
   - 21:00-23:00：0.70元/度
   - 23:00-07:00：4.00元/度

2. 服务费计算：
   - 固定费率：0.8元/度

3. 总费用 = 电费 + 服务费

### 注意事项
1. 当前充电量不包含故障订单的充电量
2. 如果存在故障订单，费用会包含故障订单的费用
3. 费用计算基于当前时段的电价
4. 所有金额保留2位小数
5. 充电完成百分比保留1位小数

### 错误码说明
| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 500 | 服务器内部错误 |

### 示例
请求：
```http
GET /api/charging-pile/all-queue-charging-status
```

响应：
```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "A": [
            {
                "userId": 1,
                "carNumber": "京A12345",
                "currentPower": "0.35",
                "targetPower": "2.00",
                "percentage": "17.5",
                "queuePosition": 1,
                "status": "CHARGING",
                "totalChargeFee": "1.40",
                "totalServiceFee": "0.28",
                "totalFee": "1.68"
            }
        ],
        "B": [],
        "C": []
    }
}
``` 