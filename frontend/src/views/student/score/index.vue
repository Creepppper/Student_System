<template>
  <div class="app-container">
    <el-alert
      v-if="errorMessage"
      :title="errorMessage"
      type="warning"
      show-icon
      :closable="false"
    />
    <el-table v-else v-loading="loading" :data="transcriptList">
      <el-table-column label="考试" align="center" prop="examName" />
      <el-table-column label="考试日期" align="center" prop="examTime" width="120">
        <template #default="scope">
          {{ proxy.parseTime(scope.row.examTime, '{y}-{m}-{d}') }}
        </template>
      </el-table-column>
      <el-table-column label="语文" align="center" prop="chinese" />
      <el-table-column label="数学" align="center" prop="math" />
      <el-table-column label="英语" align="center" prop="english" />
      <el-table-column label="物理" align="center" prop="physics" />
      <el-table-column label="化学" align="center" prop="chemistry" />
      <el-table-column label="生物" align="center" prop="biology" />
      <el-table-column label="缺考科目" align="center">
        <template #default="scope">
          {{ formatAbsentSubjects(scope.row.absentSubjects) }}
        </template>
      </el-table-column>
      <el-table-column label="总分" align="center">
        <template #default="scope">
          {{ scope.row.complete ? scope.row.total : "未完整" }}
        </template>
      </el-table-column>
      <el-table-column label="年级名次" align="center">
        <template #default="scope">
          {{ scope.row.complete ? scope.row.gradeRank : "不参与排名" }}
        </template>
      </el-table-column>
      <el-table-column label="班级名次" align="center">
        <template #default="scope">
          {{ scope.row.complete ? scope.row.classRank : "不参与排名" }}
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup name="MyScore">
import { getMyScore } from "@/api/system/score"

const { proxy } = getCurrentInstance()

/** 六科代码与中文名，与后端 StuScoreSubject 保持一致 */
const SUBJECT_LABELS = { chinese: "语文", math: "数学", english: "英语", physics: "物理", chemistry: "化学", biology: "生物" }

const transcriptList = ref([])
const loading = ref(true)
const errorMessage = ref("")

/** 把缺考科目代码（如 "chinese,physics"）转换为中文（如 "语文、物理"） */
function formatAbsentSubjects(codes) {
  if (!codes) {
    return "无"
  }
  return codes.split(",").map(code => SUBJECT_LABELS[code] || code).join("、")
}

getMyScore().then(response => {
  transcriptList.value = response.data
  loading.value = false
}).catch(error => {
  errorMessage.value = error.message || "无法加载成绩"
  loading.value = false
})
</script>
