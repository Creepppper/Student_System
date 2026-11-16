import { test } from 'node:test'
import assert from 'node:assert/strict'
import { classFilterOptions } from '../src/views/system/student/classOptions.js'

const grades = ['高一', '高二', '高三']
const classes = ['1班', '2班', '3班']
const assignments = [{ grade: '高一', className: '1班' }, { grade: '高二', className: '3班' }]

test('班主任只看到负责的年级和所选年级的班级', () => {
  assert.deepEqual(classFilterOptions(grades, classes, assignments, false, '高二'), { grades: ['高一', '高二'], classes: ['3班'] })
  assert.deepEqual(classFilterOptions(grades, classes, assignments, false, '高三').classes, [])
  assert.deepEqual(classFilterOptions(grades, classes, [], false, undefined), { grades: [], classes: [] })
})

test('管理员可筛选所有标准年级和班级', () => {
  assert.deepEqual(classFilterOptions(grades, classes, [], true, '高三'), { grades, classes })
})
