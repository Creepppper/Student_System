import request from '@/utils/request'

// 查询查卷申请列表
export function listReview(query) {
  return request({
    url: '/system/review/list',
    method: 'get',
    params: query
  })
}

// 查询查卷申请详细
export function getReview(id) {
  return request({
    url: '/system/review/' + id,
    method: 'get'
  })
}

// 新增查卷申请
export function addReview(data) {
  return request({
    url: '/system/review',
    method: 'post',
    data: data
  })
}

// 修改查卷申请
export function updateReview(data) {
  return request({
    url: '/system/review',
    method: 'put',
    data: data
  })
}

// 删除查卷申请
export function delReview(id) {
  return request({
    url: '/system/review/' + id,
    method: 'delete'
  })
}
