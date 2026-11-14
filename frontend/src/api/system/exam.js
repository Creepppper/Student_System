import request from '@/utils/request'

// 查询考试信息列表
export function listExam(query) {
  return request({
    url: '/system/exam/list',
    method: 'get',
    params: query
  })
}

// 查询考试信息详细
export function getExam(id) {
  return request({
    url: '/system/exam/' + id,
    method: 'get'
  })
}

// 新增考试信息
export function addExam(data) {
  return request({
    url: '/system/exam',
    method: 'post',
    data: data
  })
}

// 修改考试信息
export function updateExam(data) {
  return request({
    url: '/system/exam',
    method: 'put',
    data: data
  })
}

// 删除考试信息
export function delExam(id) {
  return request({
    url: '/system/exam/' + id,
    method: 'delete'
  })
}
