import request from '@/utils/request'

// 获取看板数据
export function getMainData(params) {
  return request({
    url: 'backendApi/statistic/main',
    method: 'post',
    data: params
  })
}

// 获取排行榜数据
export function getTopData(params) {
  return request({
    url: 'backendApi/statistic/top',
    method: 'post',
    data: params
  })
}

// 获取总会员数量
export function getTotalMember() {
  return request({
    url: 'backendApi/statistic/totalMember',
    method: 'get'
  })
}
