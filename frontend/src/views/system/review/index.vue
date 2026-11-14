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
      <el-form-item label="查卷科目" prop="subject">
        <el-select v-model="queryParams.subject" placeholder="请选择查卷科目" clearable @change="handleQuery">
          <el-option
            v-for="subject in SUBJECT_FIELDS"
            :key="subject.key"
            :label="subject.label"
            :value="subject.key"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable @change="handleQuery">
          <el-option
            v-for="dict in stu_review_status"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
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
      <el-table-column label="查卷科目" align="center" prop="subject">
        <template #default="scope">
          {{ formatSubject(scope.row.subject) }}
        </template>
      </el-table-column>
      <el-table-column label="申请理由" align="center" prop="reason" />
      <el-table-column label="状态" align="center" prop="status">
        <template #default="scope">
          <dict-tag :options="stu_review_status" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="班主任处理意见" align="center" prop="reply" show-overflow-tooltip />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="260">
        <template #default="scope">
          <el-button
            v-if="scope.row.status === '0'"
            link
            type="success"
            icon="Check"
            @click="openHandle(scope.row, 'ACCEPT')"
            v-hasPermi="['system:review:handle']"
          >受理</el-button>
          <el-button
            v-if="scope.row.status === '0' || scope.row.status === '1'"
            link
            type="danger"
            icon="Close"
            @click="openHandle(scope.row, 'REJECT')"
            v-hasPermi="['system:review:handle']"
          >驳回</el-button>
          <el-button
            v-if="scope.row.status === '1'"
            link
            type="primary"
            icon="CircleCheck"
            @click="openHandle(scope.row, 'COMPLETE')"
            v-hasPermi="['system:review:handle']"
          >完成</el-button>
          <el-button
            v-if="scope.row.status === '0'"
            link
            type="primary"
            icon="Edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['system:review:edit']"
          >修改</el-button>
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
            <el-form-item label="申请学生" prop="studentId">
              <el-select v-model="form.studentId" placeholder="请选择申请学生" filterable clearable style="width: 100%">
                <el-option
                  v-for="item in studentOptions"
                  :key="item.id"
                  :label="`${item.studentNo} ${item.studentName}（${item.grade}${item.className}）`"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="考试" prop="examId">
              <el-select v-model="form.examId" placeholder="请选择考试" filterable clearable style="width: 100%">
                <el-option
                  v-for="item in examOptions"
                  :key="item.id"
                  :label="`${item.examName}（${proxy.parseTime(item.examTime, '{y}-{m}-{d}')}）`"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="查卷科目" prop="subject">
              <el-select v-model="form.subject" placeholder="请选择查卷科目" style="width: 100%">
                <el-option
                  v-for="subject in SUBJECT_FIELDS"
                  :key="subject.key"
                  :label="subject.label"
                  :value="subject.key"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="申请理由" prop="reason">
              <el-input v-model="form.reason" type="textarea" placeholder="请输入内容" />
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

    <!-- 班主任处理查卷申请对话框（受理 / 驳回 / 完成） -->
    <el-dialog :title="handleTitle" v-model="handleOpen" width="500px" append-to-body>
      <el-form ref="handleRef" :model="handleForm" :rules="handleRules" label-width="100px">
        <el-form-item label="处理意见" prop="reply">
          <el-input
            v-model="handleForm.reply"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
            :placeholder="handleReplyPlaceholder"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" :loading="handleSubmitting" @click="submitHandle">确 定</el-button>
          <el-button @click="handleOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Review">
import { listReview, getReview, delReview, addReview, updateReview, handleReview } from "@/api/system/review"
import { listStudent } from "@/api/system/student"
import { listExam } from "@/api/system/exam"

const { proxy } = getCurrentInstance()
const { stu_review_status } = proxy.useDict("stu_review_status")

/** 六科代码与中文名，与后端 StuScoreSubject 保持一致 */
const SUBJECT_FIELDS = [
  { key: "chinese", label: "语文" },
  { key: "math", label: "数学" },
  { key: "english", label: "英语" },
  { key: "physics", label: "物理" },
  { key: "chemistry", label: "化学" },
  { key: "biology", label: "生物" }
]

const studentOptions = ref([])
const examOptions = ref([])

/** 把科目代码转换为中文，如 "physics" -> "物理" */
function formatSubject(code) {
  return SUBJECT_FIELDS.find(item => item.key === code)?.label || code || ""
}

/** 加载学生、考试下拉选项 */
function loadOptions() {
  listStudent({ pageNum: 1, pageSize: 1000 }).then(response => {
    studentOptions.value = response.rows
  })
  listExam({ pageNum: 1, pageSize: 1000 }).then(response => {
    examOptions.value = response.rows
  })
}

const reviewList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")

/** 处理对话框：动作与后端 StuReviewAction 枚举一致 */
const HANDLE_ACTIONS = {
  ACCEPT: { label: "受理", replyRequired: false },
  REJECT: { label: "驳回", replyRequired: true },
  COMPLETE: { label: "完成", replyRequired: true }
}
const handleOpen = ref(false)
const handleSubmitting = ref(false)
const handleTitle = ref("")
const handleReplyPlaceholder = ref("")
const handleCurrent = ref({ id: null, action: null })
const handleForm = ref({ reply: "" })
const handleRules = {
  reply: [{ validator: validateHandleReply, trigger: "blur" }]
}

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
      { required: true, message: "请选择申请学生", trigger: "change" }
    ],
    examId: [
      { required: true, message: "请选择考试", trigger: "change" }
    ],
    subject: [
      { required: true, message: "请选择查卷科目", trigger: "change" }
    ],
    reason: [
      { required: true, message: "申请理由不能为空", trigger: "blur" }
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

/** 驳回、完成必须填写处理意见（与后端 StuReviewAction 保持一致） */
function validateHandleReply(rule, value, callback) {
  const action = HANDLE_ACTIONS[handleCurrent.value.action]
  if (action && action.replyRequired && !(value || "").trim()) {
    callback(new Error("请填写处理意见"))
  } else {
    callback()
  }
}

/** 打开处理对话框 */
function openHandle(row, action) {
  handleCurrent.value = { id: row.id, action }
  handleForm.value = { reply: "" }
  handleTitle.value = `${HANDLE_ACTIONS[action].label}查卷申请`
  handleReplyPlaceholder.value = HANDLE_ACTIONS[action].replyRequired
    ? "请填写处理意见（必填）"
    : "可选填写处理意见"
  handleOpen.value = true
  nextTick(() => proxy.resetForm("handleRef"))
}

/** 提交处理结果 */
function submitHandle() {
  proxy.$refs["handleRef"].validate(valid => {
    if (!valid) {
      return
    }
    handleSubmitting.value = true
    handleReview(handleCurrent.value.id, {
      action: handleCurrent.value.action,
      reply: handleForm.value.reply
    }).then(() => {
      proxy.$modal.msgSuccess(`${HANDLE_ACTIONS[handleCurrent.value.action].label}成功`)
      handleOpen.value = false
      getList()
    }).finally(() => {
      handleSubmitting.value = false
    })
  })
}


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
loadOptions()
</script>
