import request from '@/utils/request'

// 查询班主任负责班级列表
export function listClassTeacher(query) {
  return request({
    url: '/system/classteacher/list',
    method: 'get',
    params: query
  })
}

// 分配班主任负责的班级
export function addClassTeacher(data) {
  return request({
    url: '/system/classteacher',
    method: 'post',
    data: data
  })
}

// 取消班主任负责的班级
export function delClassTeacher(ids) {
  return request({
    url: '/system/classteacher/' + ids,
    method: 'delete'
  })
}
