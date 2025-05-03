import request from '@/utils/request'

// 分页查询开卡赠礼列表
export function getOpenGiftList(query) {
  return request({
      url: 'backendApi/openGift/list',
      method: 'get',
      params: query
  })
}

// 查询信息
export function getOpenGiftInfo(id) {
  return request({
    url: 'backendApi/openGift/info/' + id,
    method: 'get'
  })
}

// 更新状态
export function updateOpenGiftStatus(id, status) {
  const data = {
    id,
    status
  }
  return request({
      url: 'backendApi/openGift/updateStatus',
      method: 'post',
      data: data
  })
}

// 删除开卡赠礼
export function deleteOpenGift(id) {
  return request({
    url: 'backendApi/openGift/delete/' + id,
    method: 'get'
  })
}

// 保存数据
export function saveOpenGift(data) {
  return request({
    url: 'backendApi/openGift/save',
    method: 'post',
    data: data
  })
}
