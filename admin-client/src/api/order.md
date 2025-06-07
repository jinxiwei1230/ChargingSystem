# 订单管理 API 文档

## 1. 获取用户订单列表

### 接口描述
获取指定用户的所有订单信息，支持分页和状态筛选。

### 请求信息
- **请求路径**：`/api/orders/user/{userId}`
- **请求方法**：GET

### 路径参数
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |

### 查询参数
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| status | String | 否 | 订单状态筛选 |
| startDate | String | 否 | 开始日期，格式：yyyy-MM-dd |
| endDate | String | 否 | 结束日期，格式：yyyy-MM-dd |
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页条数，默认10 |

### 响应信息
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "total": 100,
        "pages": 10,
        "current": 1,
        "records": [
            {
                "orderId": "ORD20231125F1001",
                "userId": 1,
                "carId": "京A12345",
                "pileId": "F1",
                "orderDate": "2023-11-25",
                "orderStatus": "COMPLETED",
                "totalKwh": 50.00,
                "totalDuration": 5.00,
                "totalFee": 115.00,
                "startTime": "2023-11-25T10:00:00",
                "endTime": "2023-11-25T15:00:00"
            }
        ]
    }
}
```

### 订单状态说明
| 状态值 | 说明 |
|--------|------|
| CREATED | 订单已创建 |
| CHARGING | 充电中 |
| COMPLETED | 已完成 |
| CANCELLED | 已取消 |
| FAULTED | 故障 |

## 2. 获取订单的详单列表

### 接口描述
获取指定订单ID的充电详单信息，包含该订单的所有时段充电记录。

### 请求信息
- **请求路径**：`/api/detailed-list/order`
- **请求方法**：GET

### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| orderId | String | 是 | 订单ID |

### 响应信息
```json
{
    "code": 200,
    "message": "success",
    "data": [
        {
            "detailId": 1,
            "orderId": "ORD202311250001",
            "periodSeq": 1,
            "periodType": "PEAK",
            "startTime": "2023-11-25T10:00:00",
            "endTime": "2023-11-25T11:00:00",
            "duration": 1.0,
            "kwh": 7.0,
            "chargeRate": 1.00,
            "serviceRate": 0.80,
            "chargeFee": 7.00,
            "serviceFee": 5.60,
            "subTotal": 12.60
        }
    ]
}
```

## 3. 获取订单信息

### 接口描述
获取指定订单ID的完整订单信息，包含订单基本信息和费用明细。

### 请求信息
- **请求路径**：`/api/orders/{orderId}`
- **请求方法**：GET

### 路径参数
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| orderId | String | 是 | 订单ID，格式：ORDyyyyMMddFxxxx |

### 响应信息
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "orderId": "ORD20231125F1001",
        "userId": 1,
        "carId": "京A12345",
        "requestId": 1001,
        "pileId": "F1",
        "orderDate": "2023-11-25",
        "orderStatus": "COMPLETED",
        "totalKwh": 50.00,
        "totalDuration": 5.00,
        "totalChargeFee": 75.00,
        "totalServiceFee": 40.00,
        "totalFee": 115.00,
        "chargingPower": 10.00,
        "startTime": "2023-11-25T10:00:00",
        "endTime": "2023-11-25T15:00:00",
        "createTime": "2023-11-25T09:55:00"
    }
}
``` 