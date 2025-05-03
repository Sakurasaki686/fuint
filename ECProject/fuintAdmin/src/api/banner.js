import request from '@/utils/request'

// 分页查询banner列表
export function getBannerList(query) {
  return request({
      url: 'backendApi/banner/list',
      method: 'get',
      params: query
  })
}

// 查询banner信息
export function getBannerInfo(bannerId) {
  return request({
    url: 'backendApi/banner/info/' + bannerId,
    method: 'get'
  })
}

// 更新banner状态
export function updateBannerStatus(id, status) {
  const data = {
    id,
    status
  }
  return request({
      url: 'backendApi/banner/updateStatus',
      method: 'post',
      data: data
  })
}

// 保存数据
export function saveBanner(data) {
  return request({
    url: 'backendApi/banner/save',
    method: 'post',
    data: data
  })
}
