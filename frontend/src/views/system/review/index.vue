<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="auto">
      <el-form-item label="申请学生ID" prop="studentId">
        <el-input
          v-model="queryParams.studentId"
          placeholder="请输入申请学生ID"
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
      <el-form-item label="查卷科目代码" prop="subject">
        <el-input
          v-model="queryParams.subject"
          placeholder="请输入查卷科目代码"
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
          v-hasPermi="['system:review:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['system:review:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['system:review:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['system:review:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="reviewList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="查卷申请ID" align="center" prop="id" />
      <el-table-column label="申请学生ID" align="center" prop="studentId" />
      <el-table-column label="考试ID" align="center" prop="examId" />
      <el-table-column label="查卷科目代码" align="center" prop="subject" />
      <el-table-column label="申请理由" align="center" prop="reason" />
      <el-table-column label="状态" align="center" prop="status" />
      <el-table-column label="班主任处理意见" align="center" prop="reply" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['system:review:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['system:review:remove']">删除</el-button>
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

    <!-- 添加或修改查卷申请对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="reviewRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="申请学生ID" prop="studentId">
              <el-input v-model="form.studentId" placeholder="请输入申请学生ID" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="考试ID" prop="examId">
              <el-input v-model="form.examId" placeholder="请输入考试ID" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="查卷科目代码" prop="subject">
              <el-input v-model="form.subject" placeholder="请输入查卷科目代码" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="申请理由" prop="reason">
              <el-input v-model="form.reason" type="textarea" placeholder="请输入内容" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="班主任处理意见" prop="reply">
              <el-input v-model="form.reply" type="textarea" placeholder="请输入内容" />
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

<script setup name="Review">
import { listReview, getReview, delReview, addReview, updateReview } from "@/api/system/review"

const { proxy } = getCurrentInstance()

const reviewList = ref([])
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
    subject: undefined,
    reason: undefined,
    status: undefined,
    reply: undefined,
  },
  rules: {
    studentId: [
      { required: true, message: "申请学生ID不能为空", trigger: "blur" }
    ],
    examId: [
      { required: true, message: "考试ID不能为空", trigger: "blur" }
    ],
    subject: [
      { required: true, message: "查卷科目代码不能为空", trigger: "blur" }
    ],
    reason: [
      { required: true, message: "申请理由不能为空", trigger: "blur" }
    ],
    status: [
      { required: true, message: "状态不能为空", trigger: "change" }
    ],
    createTime: [
      { required: true, message: "创建时间不能为空", trigger: "blur" }
    ],
    updateTime: [
      { required: true, message: "更新时间不能为空", trigger: "blur" }
    ]
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询查卷申请列表 */
function getList() {
  loading.value = true
  listReview(queryParams.value).then(response => {
    reviewList.value = response.rows
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
    subject: null,
    reason: null,
    status: null,
    reply: null,
    createTime: null,
    updateTime: null
  }
  proxy.resetForm("reviewRef")
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
  title.value = "添加查卷申请"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _id = row.id || ids.value
  getReview(_id).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改查卷申请"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["reviewRef"].validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        updateReview(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addReview(form.value).then(() => {
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
  proxy.$modal.confirm('是否确认删除查卷申请编号为"' + _ids + '"的数据项？').then(function() {
    return delReview(_ids)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('system/review/export', {
    ...queryParams.value
  }, `review_${new Date().getTime()}.xlsx`)
}

getList()
</script>
