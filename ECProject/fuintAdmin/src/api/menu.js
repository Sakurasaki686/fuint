import request from '@/utils/request'

// 获取路由
export const getRouters = () => {
  return request({
    url: 'backendApi/login/getRouters',
    method: 'get'
  })
}
