<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" :disabled="!canApply" @click="handleApply">提交查卷</el-button>
      </el-col>
      <right-toolbar @queryTable="getList"></right-toolbar>
    </el-row>

    <el-alert v-if="!canApply && hint" :title="hint" type="info" show-icon :closable="false" class="mb8" />

    <el-table v-loading="loading" :data="reviewList">
      <el-table-column label="考试" align="center" prop="examId">
        <template #default="scope">
          {{ examName(scope.row.examId) }}
        </template>
      </el-table-column>
      <el-table-column label="科目" align="center" prop="subject">
        <template #default="scope">
          {{ SUBJECT_LABELS[scope.row.subject] || scope.row.subject }}
        </template>
      </el-table-column>
      <el-table-column label="申请理由" align="center" prop="reason" show-overflow-tooltip />
      <el-table-column label="状态" align="center" prop="status">
        <template #default="scope">
          <dict-tag :options="stu_review_status" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="班主任处理意见" align="center" prop="reply" show-overflow-tooltip />
      <el-table-column label="提交时间" align="center" prop="createTime" width="170">
        <template #default="scope">
          {{ proxy.parseTime(scope.row.createTime) }}
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

    <el-dialog title="提交查卷申请" v-model="open" width="500px" append-to-body>
      <el-form ref="applyRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="考试" prop="examId">
          <el-select v-model="form.examId" placeholder="请选择考试" style="width: 100%">
            <el-option v-for="item in examOptions" :key="item.examId" :label="item.examName" :value="item.examId" />
          </el-select>
        </el-form-item>
        <el-form-item label="科目" prop="subject">
          <el-select v-model="form.subject" placeholder="请选择科目" style="width: 100%">
            <el-option v-for="(label, key) in SUBJECT_LABELS" :key="key" :label="label" :value="key" />
          </el-select>
        </el-form-item>
        <el-form-item label="申请理由" prop="reason">
          <el-input v-model="form.reason" type="textarea" :rows="4" maxlength="500" show-word-limit placeholder="请说明需要复核的原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" :loading="submitting" @click="submitForm">确 定</el-button>
          <el-button @click="open = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="MyReview">
import { listMyReview, applyReview } from "@/api/system/review"
import { getMyScore } from "@/api/system/score"

const { proxy } = getCurrentInstance()
const { stu_review_status } = proxy.useDict("stu_review_status")

/** 六科代码与中文名，与后端 StuScoreSubject 保持一致 */
const SUBJECT_LABELS = { chinese: "语文", math: "数学", english: "英语", physics: "物理", chemistry: "化学", biology: "生物" }

const reviewList = ref([])
const total = ref(0)
const loading = ref(true)
const queryParams = reactive({ pageNum: 1, pageSize: 10 })

/** 可申请的考试：本人有成绩记录的考试 */
const examOptions = ref([])
const canApply = ref(false)
const hint = ref("")

const open = ref(false)
const submitting = ref(false)
const form = reactive({ examId: undefined, subject: undefined, reason: "" })
const rules = {
  examId: [{ required: true, message: "请选择考试", trigger: "change" }],
  subject: [{ required: true, message: "请选择科目", trigger: "change" }],
  reason: [{ required: true, message: "请填写申请理由", trigger: "blur" }]
}

function examName(examId) {
  return examOptions.value.find(item => item.examId === examId)?.examName || examId
}

function getList() {
  loading.value = true
  listMyReview(queryParams).then(response => {
    reviewList.value = response.rows
    total.value = response.total
    loading.value = false
  }).catch(() => {
    loading.value = false
  })
}

function loadExams() {
  getMyScore().then(response => {
    examOptions.value = response.data.map(item => ({ examId: item.examId, examName: item.examName }))
    canApply.value = examOptions.value.length > 0
    hint.value = canApply.value ? "" : "暂无考试成绩，无法提交查卷申请。"
  }).catch(error => {
    canApply.value = false
    hint.value = error.message || "无法加载考试列表"
  })
}

function handleApply() {
  form.examId = undefined
  form.subject = undefined
  form.reason = ""
  open.value = true
  nextTick(() => proxy.resetForm("applyRef"))
}

function submitForm() {
  proxy.$refs["applyRef"].validate(valid => {
    if (!valid) {
      return
    }
    submitting.value = true
    applyReview({ examId: form.examId, subject: form.subject, reason: form.reason }).then(() => {
      proxy.$modal.msgSuccess("提交成功，请等待班主任处理")
      open.value = false
      getList()
    }).finally(() => {
      submitting.value = false
    })
  })
}

getList()
loadExams()
</script>
