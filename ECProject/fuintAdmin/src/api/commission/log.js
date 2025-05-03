import request from '@/utils/request'

// 查询员工提成统计列表
export function getStaffStatistics(query) {
  return request({
    url: 'backendApi/commissionLog/list',
    method: 'get',
    params: query
  })
}

// 查询员工提成统计列表
export function getStaffStatisticsDetail(query) {
  return request({
    url: 'backendApi/commissionLog/list',
    method: 'get',
    params: query
  })
}
