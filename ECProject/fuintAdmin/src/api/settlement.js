import request from '@/utils/request'

// 分页查询结算列表
export function getSettlementList(query) {
  return request({
      url: 'backendApi/settlement/list',
      method: 'get',
      params: query
  })
}

// 查询结算详情
export function getSettlementInfo(query) {
  return request({
    url: 'backendApi/settlement/info/',
    method: 'get',
    params: query
  })
}

// 提交结算
export function doSubmit(data) {
  return request({
    url: 'backendApi/settlement/doSubmit',
    method: 'post',
    data: data
  })
}

// 提交确认
export function doConfirm(data) {
  return request({
    url: 'backendApi/settlement/doConfirm',
    method: 'post',
    data: data
  })
}


