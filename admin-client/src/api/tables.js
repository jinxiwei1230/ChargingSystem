import request from '@/utils/request'

/**
 * 获取充电桩报表数据
 * @param {Object} params 请求参数
 * @param {String} params.pileId 充电桩编号，不传则查询所有充电桩
 * @param {String} params.timeType 时间类型，可选值：day（今日）、week（本周）、month（本月）
 * @returns {Promise} 返回报表数据
 */
export function getChargingPileReport(params) {
  return request({
    url: '/api/charging-pile/report',
    method: 'get',
    params
  })
}

/**
 * 获取系统总体报表数据
 * @param {Object} params 请求参数
 * @param {String} params.timeType 时间类型，可选值：day（今日）、week（本周）、month（本月）
 * @returns {Promise} 返回系统总体报表数据
 */
export function getChargingSystemSummaryReport(params) {
  return request({
    url: '/api/charging-pile/report/summary',
    method: 'get',
    params
  })
}
