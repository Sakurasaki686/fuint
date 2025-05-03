import request from '@/utils/request'

// 分页查询转赠记录列表
export function getGiveLogList(query) {
  return request({
      url: 'backendApi/give/list',
      method: 'get',
      params: query
  })
}

// 获取转赠详情
export function getGiveItem(id) {
  return request({
      url: 'backendApi/give/giveItem?giveId=' + id,
      method: 'get',
  })
}
