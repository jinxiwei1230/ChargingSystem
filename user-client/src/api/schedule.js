import request from '@/utils/request'

// 获取等候区队列，已实现
export function getWaitingQueue() {
  return request({
    url: '/request/waiting-queue',
    method: 'get'
  })
}

// 提交充电请求，已实现
export function submitChargingRequest(data) {
  return request({
    url: '/request/submit',
    method: 'post',
    params: data
  })
}

// 查看本车排队号码，已实现
export function getQueueNumber(userId) {
  return request({
    url: '/request/getQueueNumber',
    method: 'get',
    params: { userId }
  })
}

// 查看本充电模式下前车等待数量，已实现
export function getAheadNumber(userId) {
  return request({
    url: '/request/getAheadNumber',
    method: 'get',
    params: { userId }
  })
}

// 修改充电模式，已实现
export function modifyChargingMode(data) {
  return request({
    url: '/request/modifyMode',
    method: 'post',
    params: data
  })
}

// 修改充电量，已实现
export function modifyChargingAmount(data) {
  return request({
    url: '/request/modifyAmount',
    method: 'post',
    params: data
  })
}

// 取消充电并回到等候区重新排队，已实现
export function cancelAndRequeue(userId) {
  return request({
    url: '/request/cancelAndRequeue',
    method: 'post',
    params: { userId }
  })
}

// 取消充电并离开，已实现
export function cancelAndLeave(userId) {
  return request({
    url: '/request/cancelAndLeave',
    method: 'post',
    params: { userId }
  })
}

// 结束充电
export function finishCharging(userId) {
  return request({
    url: '/request/finish',
    method: 'post',
    params: { userId }
  })
}

// 处理充电桩故障
export function handlePileFault(data) {
  return request({
    url: '/request/fault/handle',
    method: 'post',
    params: data
  })
}

// 处理充电桩故障恢复
export function handlePileRecovery(pileId) {
  return request({
    url: '/request/fault/recovery',
    method: 'post',
    params: { pileId }
  })
}

// 获取用户充电请求列表，已实现
export function getUserRequests(userId) {
  return request({
    url: '/user/requests',
    method: 'get',
    params: { userId }
  })
}

