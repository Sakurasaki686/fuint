import request from '@/utils/request'

// 分页查询卡券分组列表
export function getGroupList(query) {
  return request({
      url: 'backendApi/couponGroup/list',
      method: 'get',
      params: query
  })
}

// 查询卡券分组列表
export function getAllGroupList(query) {
  return request({
    url: 'backendApi/couponGroup/quickSearch',
    method: 'get',
    params: query
  })
}

// 查询分组信息
export function getGroupInfo(id) {
  return request({
    url: 'backendApi/couponGroup/info/' + id,
    method: 'get'
  })
}

// 更新状态
export function updateGroupStatus(id, status) {
  const data = {
    id,
    status
  }
  return request({
      url: 'backendApi/couponGroup/updateStatus',
      method: 'post',
      data: data
  })
}

// 删除卡券分组
export function deleteGroup(id) {
  return request({
    url: 'backendApi/couponGroup/delete/' + id,
    method: 'get'
  })
}

// 保存卡券分组数据
export function saveGroup(data) {
  return request({
    url: 'backendApi/couponGroup/save',
    method: 'post',
    data: data
  })
}
