import request from '@/utils/request'
import { parseStrEmpty } from "@/utils/fuint";

// 查询用户列表
export function getAccountList(query) {
  return request({
    url: 'backendApi/account/list',
    method: 'get',
    params: query
  })
}

// 查询用户详细
export function getAccount(userId) {
  return request({
    url: 'backendApi/account/info/' + parseStrEmpty(userId),
    method: 'get'
  })
}

// 新增用户
export function addAccount(data) {
  return request({
    url: 'backendApi/account/doCreate',
    method: 'post',
    data: data
  })
}

// 修改用户
export function updateAccount(data) {
  return request({
    url: 'backendApi/account/update',
    method: 'post',
    data: data
  })
}

// 删除用户
export function delAccount(userId) {
  return request({
    url: 'backendApi/account/delete/' + userId,
    method: 'get'
  })
}

// 用户密码重置
export function resetAccountPwd(userId, password) {
  const data = {
    userId,
    password
  }
  return request({
    url: 'backendApi/account/resetPwd',
    method: 'post',
    data: data
  })
}

// 用户状态修改
export function changeAccountStatus(userId, status) {
  const data = {
    userId,
    status
  }
  return request({
    url: 'backendApi/account/updateStatus',
    method: 'post',
    data: data
  })
}

// 查询用户个人信息
export function getAccountProfile() {
  return request({
    url: 'backendApi/account/profile',
    method: 'get'
  })
}

// 修改用户个人信息
export function updateAccountProfile(data) {
  return request({
    url: 'backendApi/account/profile',
    method: 'post',
    data: data
  })
}

// 用户密码重置
export function updateAccountPwd(oldPassword, newPassword) {
  const data = {
    oldPassword,
    newPassword
  }
  return request({
    url: 'backendApi/account/profile/updatePwd',
    method: 'post',
    params: data
  })
}

// 用户头像上传
export function uploadAvatar(data) {
  return request({
    url: 'backendApi/account/profile/avatar',
    method: 'post',
    data: data
  })
}

// 查询授权角色
export function getAuthRole(userId) {
  return request({
    url: 'backendApi/account/authRole/' + userId,
    method: 'get'
  })
}

// 保存授权角色
export function updateAuthRole(data) {
  return request({
    url: 'backendApi/account/authRole',
    method: 'post',
    params: data
  })
}
