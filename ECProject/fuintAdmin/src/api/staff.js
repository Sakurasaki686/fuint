import request from '@/utils/request'

// 查询员工列表
export function getStaffList(query) {
  return request({
      url: 'backendApi/staff/list',
      method: 'get',
      params: query
  })
}

// 查询员工信息
export function getStaffInfo(staffId) {
  return request({
    url: 'backendApi/staff/info/' + staffId,
    method: 'get'
  })
}

// 删除员工信息
export function deleteStaff(staffId) {
  return request({
    url: 'backendApi/staff/delete/' + staffId,
    method: 'get'
  })
}

// 获取店铺员工列表
export function getStoreStaffList(storeId) {
  return request({
    url: 'backendApi/staff/storeStaffList/' + storeId,
    method: 'get'
  })
}

// 更新员工状态
export function updateStaffStatus(id, status) {
  const data = {
    id,
    status
  }
  return request({
      url: 'backendApi/staff/updateStatus',
      method: 'post',
      data: data
  })
}

// 保存员工信息
export function saveStaff(data) {
  return request({
    url: 'backendApi/staff/save',
    method: 'post',
    data: data
  })
}
