import request from '@/utils/request'

// 分页查询订阅消息列表
export function getSubMessageList(query) {
  return request({
      url: 'backendApi/subMessage/list',
      method: 'get',
      params: query
  })
}

// 查询信息
export function getSubMessageInfo(id) {
  return request({
    url: 'backendApi/subMessage/info/' + id,
    method: 'get'
  })
}

// 更新状态
export function updateSubMessageStatus(id, status) {
  const data = {
    id,
    status
  }
  return request({
      url: 'backendApi/subMessage/updateStatus',
      method: 'post',
      data: data
  })
}

// 删除订阅消息
export function deleteSubMessage(id) {
  return request({
    url: 'backendApi/subMessage/delete/' + id,
    method: 'get'
  })
}

// 保存数据
export function saveSubMessage(data) {
  return request({
    url: 'backendApi/subMessage/save',
    method: 'post',
    data: data
  })
}
