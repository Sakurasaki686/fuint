import request from '@/utils/request'

// 分页查询文章列表
export function getArticleList(query) {
  return request({
      url: 'backendApi/article/list',
      method: 'get',
      params: query
  })
}

// 查询文章信息
export function getArticle(articleId) {
  return request({
    url: 'backendApi/article/info/' + articleId,
    method: 'get'
  })
}

// 更新文章状态
export function updateStatus(id, status) {
  const data = {
    id,
    status
  }
  return request({
      url: 'backendApi/article/updateStatus',
      method: 'post',
      data: data
  })
}

// 保存数据
export function saveArticle(data) {
  return request({
    url: 'backendApi/article/save',
    method: 'post',
    data: data
  })
}
