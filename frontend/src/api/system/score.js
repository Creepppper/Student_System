import request from '@/utils/request'

// 查询学生成绩列表
export function listScore(query) {
  return request({
    url: '/system/score/list',
    method: 'get',
    params: query
  })
}

// 查询学生成绩详细
export function getScore(id) {
  return request({
    url: '/system/score/' + id,
    method: 'get'
  })
}

// 新增学生成绩
export function addScore(data) {
  return request({
    url: '/system/score',
    method: 'post',
    data: data
  })
}

// 修改学生成绩
export function updateScore(data) {
  return request({
    url: '/system/score',
    method: 'put',
    data: data
  })
}

// 删除学生成绩
export function delScore(id) {
  return request({
    url: '/system/score/' + id,
    method: 'delete'
  })
}

// 查询考试排名（scope: GRADE 年级 / CLASS 班级；subject: TOTAL 或六科代码）
export function getScoreRanking(query) {
  return request({
    url: '/system/score/ranking',
    method: 'get',
    params: query
  })
}

// 查询学生成绩单
export function getScoreTranscript(studentId) {
  return request({
    url: '/system/score/transcript/' + studentId,
    method: 'get'
  })
}

// 学生端：我的成绩单
export function getMyScore() {
  return request({
    url: '/system/score/my',
    method: 'get'
  })
}

// 班级平均分统计
export function getClassStat(examId) {
  return request({
    url: '/system/score/classStat',
    method: 'get',
    params: { examId }
  })
}
