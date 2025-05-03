import request from '@/utils/request'

// 分页查询核销流水列表
export function getConfirmLogList(query) {
  return request({
      url: 'backendApi/confirmLog/list',
      method: 'get',
      params: query
  })
}

// 撤销已使用的卡券
export function rollbackUserCoupon(id, userCouponId) {
  return request({
    url: 'backendApi/confirmLog/rollbackUserCoupon/' + id + '?userCouponId=' + userCouponId,
    method: 'get',
  })
}
