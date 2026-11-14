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

// 处理查卷申请（action: ACCEPT 受理 / REJECT 驳回 / COMPLETE 完成）
export function handleReview(id, data) {
  return request({
    url: '/system/review/' + id + '/handle',
    method: 'put',
    data: data
  })
}

// 学生端：我的查卷申请
export function listMyReview(query) {
  return request({
    url: '/system/review/my',
    method: 'get',
    params: query
  })
}

// 学生端：提交查卷申请（学生由登录账号决定，无需传学生ID）
export function applyReview(data) {
  return request({
    url: '/system/review/apply',
    method: 'post',
    data: data
  })
}
