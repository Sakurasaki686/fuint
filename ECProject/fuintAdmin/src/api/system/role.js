import request from '@/utils/request'

// 查询角色列表
export function listRole(query) {
  return request({
    url: 'backendApi/duty/list',
    method: 'get',
    params: query
  })
}

// 查询角色详细
export function getRole(roleId) {
  return request({
    url: 'backendApi/duty/info/' + roleId,
    method: 'get'
  })
}

// 新增角色
export function addRole(data) {
  return request({
    url: 'backendApi/duty/add',
    method: 'post',
    data: data
  })
}

// 修改角色
export function updateRole(data) {
  return request({
    url: 'backendApi/duty/update',
    method: 'post',
    data: data
  })
}

// 角色数据权限
export function dataScope(data) {
  return request({
    url: 'backendApi/duty/dataScope',
    method: 'post',
    data: data
  })
}

// 角色状态修改
export function changeRoleStatus(roleId, status) {
  const data = {
    roleId,
    status
  }
  return request({
    url: 'backendApi/duty/changeStatus',
    method: 'post',
    data: data
  })
}

// 删除角色
export function delRole(roleId) {
  return request({
    url: 'backendApi/duty/delete/' + roleId,
    method: 'post'
  })
}

// 查询角色已授权用户列表
export function getRoleUserList(query) {
  return request({
    url: 'backendApi/duty/getRoleUserList',
    method: 'get',
    params: query
  })
}

// 查询角色未授权用户列表
export function unallocatedUserList(query) {
  return request({
    url: 'backendApi/duty/authUser/unallocatedList',
    method: 'get',
    params: query
  })
}

// 取消用户授权角色
export function authUserCancel(data) {
  return request({
    url: 'backendApi/duty/authUser/cancel',
    method: 'post',
    data: data
  })
}

// 批量取消用户授权角色
export function authUserCancelAll(data) {
  return request({
    url: 'backendApi/duty/authUser/cancelAll',
    method: 'post',
    params: data
  })
}

// 授权用户选择
export function authUserSelectAll(data) {
  return request({
    url: 'backendApi/duty/authUser/selectAll',
    method: 'post',
    params: data
  })
}
