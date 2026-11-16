import { readFileSync } from 'node:fs'
import { runInNewContext } from 'node:vm'
import { test } from 'node:test'
import assert from 'node:assert/strict'
import { parse } from '@vue/compiler-sfc'

const file = process.env.STUDENT_VIEW_FIXTURE || new URL('../src/views/system/student/index.vue', import.meta.url)
const source = parse(readFileSync(file, 'utf8')).descriptor.scriptSetup.content
const initialization = source.slice(source.lastIndexOf('\ngetList()'))

for (const permitted of [false, true]) {
  test(`student page initializes with user-list permission ${permitted}`, () => {
    let studentRequests = 0
    let userRequests = 0
    runInNewContext(initialization, {
      getList: () => studentRequests++,
      loadUserOptions: () => userRequests++,
      isAdmin: true,
      auth: { hasPermi: permission => permission === 'system:user:list' && permitted }
    })
    assert.equal(studentRequests, 1)
    assert.equal(userRequests, permitted ? 1 : 0)
  })
}
