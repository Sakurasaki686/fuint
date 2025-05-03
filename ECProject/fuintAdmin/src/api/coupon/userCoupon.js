import request from '@/utils/request'

// 分页查询会员卡券列表
export function getUserCouponList(query) {
  return request({
      url: 'backendApi/userCoupon/list',
      method: 'get',
      params: query
  })
}

// 作废会员卡券
export function deleteUserCoupon(id) {
  return request({
      url: 'backendApi/userCoupon/delete/' + id,
      method: 'get',
  })
}

