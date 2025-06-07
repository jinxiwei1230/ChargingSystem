import request from '@/utils/request'

/**
 * 获取所有充电桩信息
 * @returns {Promise} 返回充电桩信息列表
 */
export function getAllChargingPiles() {
  return request({
    url: '/api/charging-pile/all',
    method: 'get'
  })
}

/**
 * 开启充电桩电源
 * @param {string} pileId - 充电桩ID
 * @returns {Promise} 返回操作结果
 */
export function powerOnChargingPile(pileId) {
  return request({
    url: '/api/charging-pile/power-on',
    method: 'post',
    params: {
      pileId
    }
  })
}

/**
 * 关闭充电桩电源
 * @param {string} pileId - 充电桩ID
 * @returns {Promise} 返回操作结果
 */
export function powerOffChargingPile(pileId) {
  return request({
    url: '/api/charging-pile/power-off',
    method: 'post',
    params: {
      pileId
    }
  })
}

/**
 * 设置充电桩参数
 * @param {Object} params - 参数对象
 * @param {string} params.pileId - 充电桩ID
 * @param {number} params.chargingPower - 充电功率
 * @returns {Promise} 返回操作结果
 */
export function setChargingPileParameters(params) {
  return request({
    url: '/api/charging-pile/set-parameters',
    method: 'post',
    data: params
  })
}

/**
 * 处理充电桩故障发生
 * @param {string} pileId - 故障充电桩ID
 * @param {string} strategy - 调度策略（PRIORITY-优先级调度，TIME_ORDER-时间顺序调度）
 * @returns {Promise} 返回操作结果
 */
export function handleChargingPileFault(pileId, strategy) {
  return request({
    url: '/request/fault/handle',
    method: 'post',
    params: {
      pileId,
      strategy
    }
  })
}

/**
 * 处理充电桩故障恢复
 * @param {string} pileId - 恢复的充电桩ID
 * @returns {Promise} 返回操作结果
 */
export function handleChargingPileRecovery(pileId) {
  return request({
    url: '/request/fault/recovery',
    method: 'post',
    params: {
      pileId
    }
  })
}

/**
 * 获取充电桩等候队列详细信息
 * @param {string} pileId - 充电桩ID
 * @returns {Promise} 返回等候队列详细信息
 */
export function getChargingQueueDetails(pileId) {
  return request({
    url: '/api/charging-pile/queue-details',
    method: 'get',
    params: {
      pileId
    }
  })
}

/**
 * 获取所有充电桩的等候队列信息
 * @returns {Promise} 返回所有充电桩的等候队列信息
 */
export function getAllPileQueueInfo() {
  return request({
    url: '/api/charging-pile/queue-info',
    method: 'get'
  })
}
