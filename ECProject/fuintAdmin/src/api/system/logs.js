import request from '@/utils/request'

// 查询操作日志列表
export function list(query) {
  return request({
    url: 'backendApi/actlog/list',
    method: 'get',
    params: query
  })
}

// 删除操作日志
export function delOperlog(logId) {
  return request({
    url: 'backendApi/actlog/delete/' + logId,
    method: 'get'
  })
}
