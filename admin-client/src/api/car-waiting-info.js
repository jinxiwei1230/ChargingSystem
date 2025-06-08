import request from '@/utils/request'

/**
 * 获取所有充电桩等候队列信息
 * 按充电桩ID分组返回所有充电桩的等候队列信息
 * @returns {Promise} 返回所有充电桩的等候队列信息
 */
export function getAllChargingPileQueues() {
  return request({
    url: '/api/charging-pile/all-queues',
    method: 'get'
  })
}

/**
 * 获取指定充电桩等候队列信息
 * 获取指定充电桩的等候队列详细信息，包括每个用户的排队情况、预计等待时间等
 * @param {string} pileId - 充电桩ID (如：A、B、C、D、E)
 * @returns {Promise} 返回指定充电桩的等候队列信息
 */
export function getChargingPileQueueDetails(pileId) {
  return request({
    url: '/api/charging-pile/queue-details',
    method: 'get',
    params: {
      pileId
    }
  })
}
