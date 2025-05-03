import request from '@/utils/request'

// 分页查询会员分组列表
export function getGroupList(query) {
  return request({
      url: 'backendApi/memberGroup/list',
      method: 'get',
      params: query
  })
}

// 查询会员分组列表
export function getAllGroupList(query) {
  return request({
    url: 'backendApi/memberGroup/quickSearch',
    method: 'get',
    params: query
  })
}

// 查询会员分组信息
export function getGroupInfo(id) {
  return request({
    url: 'backendApi/memberGroup/info/' + id,
    method: 'get'
  })
}

// 更新会员分组状态
export function updateGroupStatus(id, status) {
  const data = {
    id,
    status
  }
  return request({
      url: 'backendApi/memberGroup/updateStatus',
      method: 'post',
      data: data
  })
}

// 删除会员分组
export function deleteGroup(id) {
  return request({
    url: 'backendApi/memberGroup/delete/' + id,
    method: 'get'
  })
}

// 保存会员分组数据
export function saveGroup(data) {
  return request({
    url: 'backendApi/memberGroup/save',
    method: 'post',
    data: data
  })
}
