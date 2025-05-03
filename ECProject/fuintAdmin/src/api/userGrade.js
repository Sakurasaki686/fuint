import request from '@/utils/request'

// 分页查询会员等级列表
export function getUserGradeList(query) {
  return request({
      url: 'backendApi/userGrade/list',
      method: 'get',
      params: query
  })
}

// 查询等级信息
export function getUserGradeInfo(memberId) {
  return request({
    url: 'backendApi/userGrade/info/' + memberId,
    method: 'get'
  })
}

// 更新状态
export function updateUserGradeStatus(userGradeId, status) {
  const data = {
    userGradeId,
    status
  }
  return request({
      url: 'backendApi/userGrade/updateStatus',
      method: 'post',
      data: data
  })
}

// 删除会员等级信息
export function deleteUserGrade(gradeId) {
  return request({
    url: 'backendApi/userGrade/delete/' + gradeId,
    method: 'get'
  })
}

// 保存数据
export function saveUserGrade(data) {
  return request({
    url: 'backendApi/userGrade/save',
    method: 'post',
    data: data
  })
}
