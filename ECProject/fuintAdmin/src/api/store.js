import request from '@/utils/request'

// 分页查询店铺列表
export function getStoreList(query) {
  return request({
      url: 'backendApi/store/list',
      method: 'get',
      params: query
  })
}

// 查询店铺信息
export function getStoreInfo(storeId) {
  return request({
    url: 'backendApi/store/info/' + storeId,
    method: 'get'
  })
}

// 查询店铺列表
export function searchStore(param) {
  return request({
    url: 'backendApi/store/searchStore',
    method: 'get',
    params: param
  })
}

// 更新店铺状态
export function updateStoreStatus(storeId, status) {
  const data = {
    storeId,
    status
  }
  return request({
      url: 'backendApi/store/updateStatus',
      method: 'post',
      data: data
  })
}

// 保存店铺
export function saveStore(data) {
  return request({
    url: 'backendApi/store/save',
    method: 'post',
    data: data
  })
}
