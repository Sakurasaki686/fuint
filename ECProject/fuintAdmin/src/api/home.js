import request from '@/utils/request'

// 获取首页数据
export function getHomeData() {
  return request({
    url: 'backendApi/home/index',
    method: 'get'
  })
}

// 获取统计数据
export function getStatisticData(query) {
  return request({
    url: 'backendApi/home/statistic',
    method: 'get',
    params: query
  })
}
