<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="auto">
      <el-form-item label="年级" prop="grade">
        <el-select v-model="queryParams.grade" placeholder="请选择年级" clearable style="width: 140px" @change="handleQuery">
          <el-option v-for="grade in gradeOptions" :key="grade" :label="grade" :value="grade" />
        </el-select>
      </el-form-item>
      <el-form-item label="班级" prop="className">
        <el-select v-model="queryParams.className" placeholder="请选择班级" clearable style="width: 140px" @change="handleQuery">
          <el-option v-for="className in classOptions" :key="className" :label="className" :value="className" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['system:classteacher:add']">分配班级</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['system:classteacher:remove']">取消分配</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="classTeacherList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="班主任账号" align="center" prop="userName" />
      <el-table-column label="昵称" align="center" prop="nickName" />
      <el-table-column label="年级" align="center" prop="grade" />
      <el-table-column label="班级" align="center" prop="className" />
      <el-table-column label="分配时间" align="center" prop="createTime" width="180">
        <template #default="scope">
          {{ proxy.parseTime(scope.row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['system:classteacher:remove']">取消分配</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <el-dialog title="分配班级" v-model="open" width="500px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="班主任账号" prop="userId">
          <el-select v-model="form.userId" placeholder="请选择班主任账号" filterable style="width: 100%">
            <el-option
              v-for="item in userOptions"
              :key="item.userId"
              :label="`${item.userName}（${item.nickName}）`"
              :value="item.userId"
            />
          </el-select>
          <div class="form-tip">账号需拥有“班主任”角色，否则保存会被拒绝。</div>
        </el-form-item>
        <el-form-item label="年级" prop="grade">
          <el-select v-model="form.grade" placeholder="请选择年级" style="width: 100%" @change="form.className = undefined">
            <el-option v-for="grade in gradeOptions" :key="grade" :label="grade" :value="grade" />
          </el-select>
        </el-form-item>
        <el-form-item label="班级" prop="className">
          <el-select v-model="form.className" placeholder="请选择班级" style="width: 100%">
            <el-option v-for="className in formClassOptions" :key="className" :label="className" :value="className" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="open = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="ClassTeacher">
import { listClassTeacher, addClassTeacher, delClassTeacher } from "@/api/system/classteacher"
import { listUser } from "@/api/system/user"
import { listStudent } from "@/api/system/student"

const { proxy } = getCurrentInstance()

const classTeacherList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const ids = ref([])
const multiple = ref(true)
const open = ref(false)
const userOptions = ref([])
const studentList = ref([])

const queryParams = reactive({ pageNum: 1, pageSize: 10, grade: undefined, className: undefined })
const form = reactive({ userId: undefined, grade: undefined, className: undefined })
const rules = {
  userId: [{ required: true, message: "请选择班主任账号", trigger: "change" }],
  grade: [{ required: true, message: "请选择年级", trigger: "change" }],
  className: [{ required: true, message: "请选择班级", trigger: "change" }]
}

/** 年级与班级选项，取自学生列表 */
const gradeOptions = computed(() => [...new Set(studentList.value.map(item => item.grade).filter(Boolean))])
const classOptions = computed(() => [...new Set(
  studentList.value.filter(item => !queryParams.grade || item.grade === queryParams.grade)
    .map(item => item.className).filter(Boolean)
)])
const formClassOptions = computed(() => [...new Set(
  studentList.value.filter(item => item.grade === form.grade).map(item => item.className).filter(Boolean)
)])

function getList() {
  loading.value = true
  listClassTeacher(queryParams).then(response => {
    classTeacherList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.grade = undefined
  queryParams.className = undefined
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id)
  multiple.value = !selection.length
}

function handleAdd() {
  form.userId = undefined
  form.grade = undefined
  form.className = undefined
  open.value = true
  nextTick(() => proxy.resetForm("formRef"))
}

function submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (!valid) {
      return
    }
    addClassTeacher({ userId: form.userId, grade: form.grade, className: form.className }).then(() => {
      proxy.$modal.msgSuccess("分配成功")
      open.value = false
      getList()
    })
  })
}

function handleDelete(row) {
  const _ids = row.id || ids.value
  proxy.$modal.confirm('是否确认取消该班主任对班级的负责关系？').then(() => {
    return delClassTeacher(_ids)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("已取消")
  }).catch(() => {})
}

function loadOptions() {
  listUser({ pageNum: 1, pageSize: 1000 }).then(response => {
    userOptions.value = response.rows
  })
  listStudent({ pageNum: 1, pageSize: 1000 }).then(response => {
    studentList.value = response.rows
  })
}

getList()
loadOptions()
</script>

<style scoped>
.form-tip {
  color: #909399;
  font-size: 12px;
  line-height: 1.5;
}
</style>
