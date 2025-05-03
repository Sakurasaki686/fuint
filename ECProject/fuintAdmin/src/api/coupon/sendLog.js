import request from '@/utils/request'

// 分页查询发券记录列表
export function getSendLogList(query) {
  return request({
      url: 'backendApi/sendLog/list',
      method: 'get',
      params: query
  })
}

// 撤销已发放的卡券
export function removeUserCoupon(id) {
  return request({
    url: 'backendApi/sendLog/removeUserCoupon/' + id,
    method: 'get',
  })
}
