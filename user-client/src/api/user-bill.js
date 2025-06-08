import request from '@/utils/request'
/**
 * 获取用户订单列表
 * @param {number} userId - 用户ID
 * @param {object} params - 查询参数
 * @param {string} params.status - 订单状态筛选
 * @param {string} params.startDate - 开始日期，格式：yyyy-MM-dd
 * @param {string} params.endDate - 结束日期，格式：yyyy-MM-dd
 * @param {number} params.page - 页码，默认1
 * @param {number} params.size - 每页条数，默认10
 * @returns {Promise}
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
 * @returns {Promise}
 */
export function getOrderDetailList(orderId) {
  return request({
    url: '/api/detailed-list/order',
    method: 'get',
    params: {
      orderId
    }
  })
}

/**
 * 获取订单信息
 * @param {string} orderId - 订单ID，格式：ORDyyyyMMddFxxxx
 * @returns {Promise}
 */
export function getOrderInfo(orderId) {
  return request({
    url: `/api/orders/${orderId}`,
    method: 'get'
  })
}
