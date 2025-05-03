import request from '@/utils/request'

// 分页查询分销提成规则列表
export function getCommissionRuleList(query) {
  return request({
      url: 'backendApi/commissionRule/list',
      method: 'get',
      params: query
  })
}

// 查询分销提成规则详情
export function getCommissionRuleInfo(ruleId) {
  return request({
     url: 'backendApi/commissionRule/info/' + ruleId,
     method: 'get'
  })
}

// 更新分销提成规则状态
export function updateCommissionRuleStatus(ruleId, status) {
  const data = { ruleId, status }
  return request({
     url: 'backendApi/commissionRule/updateStatus',
     method: 'post',
     data: data
  })
}

// 保存分销提成规则
export function saveCommissionRule(data) {
  return request({
     url: 'backendApi/commissionRule/save',
     method: 'post',
     data: data
  })
}

// 删除信息
export function removeCommissionRule(data) {
  return request({
    url: 'backendApi/commissionRule/updateStatus',
    method: 'post',
    data: data
  })
}
