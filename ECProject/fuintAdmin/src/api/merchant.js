import request from '@/utils/request'

// 分页查询商户列表
export function getMerchantList(query) {
  return request({
     url: 'backendApi/merchant/list',
     method: 'get',
     params: query
  })
}

// 查询商户信息
export function getMerchantInfo(merchantId) {
  return request({
     url: 'backendApi/merchant/info/' + merchantId,
     method: 'get'
  })
}

// 查询商户列表
export function searchMerchant(param) {
  return request({
     url: 'backendApi/merchant/searchMerchant',
     method: 'get',
     params: param
  })
}

// 更新商户状态
export function updateMerchantStatus(merchantId, status) {
  const data = {
     merchantId,
     status
  }
  return request({
     url: 'backendApi/merchant/updateStatus',
     method: 'post',
     data: data
  })
}

// 保存商户信息
export function saveMerchant(data) {
  return request({
     url: 'backendApi/merchant/save',
     method: 'post',
     data: data
  })
}
