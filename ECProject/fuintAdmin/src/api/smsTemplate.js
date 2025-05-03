import request from '@/utils/request'

// 分页查询短信模板列表
export function getSmsTemplateList(query) {
  return request({
      url: 'backendApi/smsTemplate/list',
      method: 'get',
      params: query
  })
}

// 查询信息
export function getSmsTemplateInfo(id) {
  return request({
    url: 'backendApi/smsTemplate/info/' + id,
    method: 'get'
  })
}

// 更新状态
export function updateSmsTemplateStatus(id, status) {
  const data = {
    id,
    status
  }
  return request({
      url: 'backendApi/smsTemplate/updateStatus',
      method: 'post',
      data: data
  })
}

// 删除短信模板
export function deleteSmsTemplate(id) {
  return request({
    url: 'backendApi/smsTemplate/delete/' + id,
    method: 'get'
  })
}

// 保存数据
export function saveSmsTemplate(data) {
  return request({
    url: 'backendApi/smsTemplate/save',
    method: 'post',
    data: data
  })
}
