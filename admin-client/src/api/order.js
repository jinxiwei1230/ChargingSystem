import request from '@/utils/request'

/**
 * 获取用户订单列表
 * @param {number} userId - 用户ID
 * @param {Object} params - 查询参数
 * @param {string} [params.status] - 订单状态
 * @param {string} [params.startDate] - 开始日期
 * @param {string} [params.endDate] - 结束日期
 * @param {number} [params.page=1] - 页码
 * @param {number} [params.size=10] - 每页条数
 * @returns {Promise} 返回订单列表数据
 */
export function getUserOrders(userId, params) {
  return request({
    url: `/api/orders/user/${userId}`,
    method: 'get',
    params
  })
}

/**
 * 获取订单的详单列表
 * @param {string} orderId - 订单ID
 * @returns {Promise} 返回订单详单列表数据
 */
export function getOrderDetailList(orderId) {
  return request({
    url: '/api/detailed-list/order',
    method: 'get',
    params: { orderId }
  })
}

/**
 * 获取订单信息
 * @param {string} orderId - 订单ID
 * @returns {Promise} 返回订单详细信息
 */
export function getOrderInfo(orderId) {
  return request({
    url: `/api/orders/${orderId}`,
    method: 'get'
  })
}

// 订单状态枚举
export const OrderStatus = {
  CREATED: 'CREATED',
  CHARGING: 'CHARGING',
  COMPLETED: 'COMPLETED',
  CANCELLED: 'CANCELLED',
  FAULTED: 'FAULTED'
}

// 订单状态映射
export const OrderStatusMap = {
  [OrderStatus.CREATED]: '订单已创建',
  [OrderStatus.CHARGING]: '充电中',
  [OrderStatus.COMPLETED]: '已完成',
  [OrderStatus.CANCELLED]: '已取消',
  [OrderStatus.FAULTED]: '故障'
} 