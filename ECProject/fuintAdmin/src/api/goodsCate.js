import request from '@/utils/request'

// 分页查询商品分类列表
export function getGoodsCateList(query) {
  return request({
      url: 'backendApi/goods/cate/list',
      method: 'get',
      params: query
  })
}

// 查询商品分类详情
export function getGoodsCateInfo(cateId) {
  return request({
    url: 'backendApi/goods/cate/info/' + cateId,
    method: 'get'
  })
}

// 更新状态
export function updateGoodsCateStatus(id, status) {
  const data = {
    id,
    status
  }
  return request({
    url: 'backendApi/goods/cate/updateStatus',
    method: 'post',
    data: data
  })
}

// 保存分类数据
export function saveGoodsCate(data) {
  return request({
    url: 'backendApi/goods/cate/save',
    method: 'post',
    data: data
  })
}
