<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="学生ID" prop="studentId">
        <el-input
          v-model="queryParams.studentId"
          placeholder="请输入学生ID"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="考试ID" prop="examId">
        <el-input
          v-model="queryParams.examId"
          placeholder="请输入考试ID"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="语文成绩" prop="chinese">
        <el-input
          v-model="queryParams.chinese"
          placeholder="请输入语文成绩"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="数学成绩" prop="math">
        <el-input
          v-model="queryParams.math"
          placeholder="请输入数学成绩"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="英语成绩" prop="english">
        <el-input
          v-model="queryParams.english"
          placeholder="请输入英语成绩"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="物理成绩" prop="physics">
        <el-input
          v-model="queryParams.physics"
          placeholder="请输入物理成绩"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="化学成绩" prop="chemistry">
        <el-input
          v-model="queryParams.chemistry"
          placeholder="请输入化学成绩"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="生物成绩" prop="biology">
        <el-input
          v-model="queryParams.biology"
          placeholder="请输入生物成绩"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="缺考科目代码" prop="absentSubjects">
        <el-input
          v-model="queryParams.absentSubjects"
          placeholder="请输入缺考科目代码"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-hasPermi="['system:score:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['system:score:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['system:score:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['system:score:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="scoreList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="成绩ID" align="center" prop="id" />
      <el-table-column label="学生ID" align="center" prop="studentId" />
      <el-table-column label="考试ID" align="center" prop="examId" />
      <el-table-column label="语文成绩" align="center" prop="chinese" />
      <el-table-column label="数学成绩" align="center" prop="math" />
      <el-table-column label="英语成绩" align="center" prop="english" />
      <el-table-column label="物理成绩" align="center" prop="physics" />
      <el-table-column label="化学成绩" align="center" prop="chemistry" />
      <el-table-column label="生物成绩" align="center" prop="biology" />
      <el-table-column label="缺考科目代码，多个用英文逗号分隔" align="center" prop="absentSubjects" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['system:score:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['system:score:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <pagination
      v-show="total>0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改学生成绩对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="scoreRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="学生ID" prop="studentId">
              <el-input v-model="form.studentId" placeholder="请输入学生ID" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="考试ID" prop="examId">
              <el-input v-model="form.examId" placeholder="请输入考试ID" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="语文成绩" prop="chinese">
              <el-input v-model="form.chinese" placeholder="请输入语文成绩" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="数学成绩" prop="math">
              <el-input v-model="form.math" placeholder="请输入数学成绩" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="英语成绩" prop="english">
              <el-input v-model="form.english" placeholder="请输入英语成绩" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="物理成绩" prop="physics">
              <el-input v-model="form.physics" placeholder="请输入物理成绩" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="化学成绩" prop="chemistry">
              <el-input v-model="form.chemistry" placeholder="请输入化学成绩" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="生物成绩" prop="biology">
              <el-input v-model="form.biology" placeholder="请输入生物成绩" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="缺考科目代码" prop="absentSubjects">
              <el-input v-model="form.absentSubjects" placeholder="请输入缺考科目代码" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Score">
import { listScore, getScore, delScore, addScore, updateScore } from "@/api/system/score"

const { proxy } = getCurrentInstance()

const scoreList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    studentId: undefined,
    examId: undefined,
    chinese: undefined,
    math: undefined,
    english: undefined,
    physics: undefined,
    chemistry: undefined,
    biology: undefined,
    absentSubjects: undefined
  },
  rules: {
    studentId: [
      { required: true, message: "学生ID不能为空", trigger: "blur" }
    ],
    examId: [
      { required: true, message: "考试ID不能为空", trigger: "blur" }
    ],
    absentSubjects: [
      { required: true, message: "缺考科目代码不能为空", trigger: "blur" }
    ]
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询学生成绩列表 */
function getList() {
  loading.value = true
  listScore(queryParams.value).then(response => {
    scoreList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

/** 取消按钮 */
function cancel() {
  open.value = false
  reset()
}

/** 表单重置 */
function reset() {
  form.value = {
    id: null,
    studentId: null,
    examId: null,
    chinese: null,
    math: null,
    english: null,
    physics: null,
    chemistry: null,
    biology: null,
    absentSubjects: null
  }
  proxy.resetForm("scoreRef")
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加学生成绩"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _id = row.id || ids.value
  getScore(_id).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改学生成绩"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["scoreRef"].validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        updateScore(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addScore(form.value).then(() => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

/** 删除按钮操作 */
function handleDelete(row) {
  const _ids = row.id || ids.value
  proxy.$modal.confirm('是否确认删除学生成绩编号为"' + _ids + '"的数据项？').then(function() {
    return delScore(_ids)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('system/score/export', {
    ...queryParams.value
  }, `score_${new Date().getTime()}.xlsx`)
}

getList()
</script>
