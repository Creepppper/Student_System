<template>
  <div class="app-container">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="考试排名" name="ranking">
        <el-form :model="rankQuery" :inline="true" label-width="auto">
          <el-form-item label="考试" prop="examId">
            <el-select v-model="rankQuery.examId" placeholder="请选择考试" filterable style="width: 260px">
              <el-option
                v-for="item in examOptions"
                :key="item.id"
                :label="`${item.examName}（${proxy.parseTime(item.examTime, '{y}-{m}-{d}')}）`"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="年级" prop="grade">
            <el-select v-model="rankQuery.grade" placeholder="请选择年级" style="width: 140px" @change="rankQuery.className = undefined">
              <el-option v-for="grade in gradeOptions" :key="grade" :label="grade" :value="grade" />
            </el-select>
          </el-form-item>
          <el-form-item v-if="rankQuery.scope === 'CLASS'" label="班级" prop="className">
            <el-select v-model="rankQuery.className" placeholder="请选择班级" style="width: 140px">
              <el-option v-for="className in classOptions" :key="className" :label="className" :value="className" />
            </el-select>
          </el-form-item>
          <el-form-item label="排名范围" prop="scope">
            <el-radio-group v-model="rankQuery.scope">
              <el-radio-button label="GRADE">年级排名</el-radio-button>
              <el-radio-button label="CLASS">班级排名</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="排名科目" prop="subject">
            <el-select v-model="rankQuery.subject" style="width: 140px">
              <el-option label="总分" value="TOTAL" />
              <el-option
                v-for="subject in SUBJECT_FIELDS"
                :key="subject.key"
                :label="subject.label"
                :value="subject.key"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="loadRanking">查询</el-button>
          </el-form-item>
        </el-form>

        <el-table v-loading="rankLoading" :data="rankList">
          <el-table-column label="名次" align="center" prop="rank" width="80" />
          <el-table-column label="学号" align="center" prop="studentNo" />
          <el-table-column label="姓名" align="center" prop="studentName" />
          <el-table-column label="年级" align="center" prop="grade" />
          <el-table-column label="班级" align="center" prop="className" />
          <el-table-column label="语文" align="center" prop="chinese" />
          <el-table-column label="数学" align="center" prop="math" />
          <el-table-column label="英语" align="center" prop="english" />
          <el-table-column label="物理" align="center" prop="physics" />
          <el-table-column label="化学" align="center" prop="chemistry" />
          <el-table-column label="生物" align="center" prop="biology" />
          <el-table-column label="总分" align="center" prop="total" />
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="班级统计" name="classStat">
        <el-form :model="statQuery" :inline="true" label-width="auto">
          <el-form-item label="考试" prop="examId">
            <el-select v-model="statQuery.examId" placeholder="请选择考试" filterable style="width: 260px" @change="loadClassStat">
              <el-option
                v-for="item in examOptions"
                :key="item.id"
                :label="`${item.examName}（${proxy.parseTime(item.examTime, '{y}-{m}-{d}')}）`"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="年级" prop="grade">
            <el-select v-model="statQuery.grade" placeholder="全部年级" clearable style="width: 140px">
              <el-option v-for="grade in classGradeOptions" :key="grade" :label="grade" :value="grade" />
            </el-select>
          </el-form-item>
        </el-form>

        <div ref="classChartRef" class="class-chart" v-show="filteredClassStatRows.length > 0"></div>
        <el-empty v-if="!classStatLoading && filteredClassStatRows.length === 0" :description="statQuery.examId ? '所选条件暂无班级统计数据' : '请选择考试查看班级平均分'" />

        <el-table v-loading="classStatLoading" :data="filteredClassStatRows" class="mt8">
          <el-table-column label="班级" align="center">
            <template #default="scope">{{ scope.row.grade }}{{ scope.row.className }}</template>
          </el-table-column>
          <el-table-column label="统计人数" align="center" prop="count" />
          <el-table-column label="平均总分" align="center" prop="avgTotal" />
          <el-table-column label="语文" align="center" prop="avgChinese" />
          <el-table-column label="数学" align="center" prop="avgMath" />
          <el-table-column label="英语" align="center" prop="avgEnglish" />
          <el-table-column label="物理" align="center" prop="avgPhysics" />
          <el-table-column label="化学" align="center" prop="avgChemistry" />
          <el-table-column label="生物" align="center" prop="avgBiology" />
        </el-table>
        <p class="stat-note">说明：只统计六科均已录入的学生，缺考科目按 0 分计入平均。</p>
      </el-tab-pane>

      <el-tab-pane label="学生成绩单" name="transcript">
        <el-form :model="transcriptQuery" :inline="true" label-width="auto">
          <el-form-item label="学生" prop="studentId">
            <el-select
              v-model="transcriptQuery.studentId"
              placeholder="请选择学生"
              filterable
              style="width: 320px"
              @change="loadTranscript"
            >
              <el-option
                v-for="item in studentOptions"
                :key="item.id"
                :label="`${item.studentNo} ${item.studentName}（${item.grade}${item.className}）`"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
        </el-form>

        <el-table v-loading="transcriptLoading" :data="transcriptList">
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
          <el-table-column label="总分" align="center" prop="total">
            <template #default="scope">
              {{ scope.row.complete ? scope.row.total : "未完整" }}
            </template>
          </el-table-column>
          <el-table-column label="年级名次" align="center" prop="gradeRank">
            <template #default="scope">
              {{ scope.row.complete ? scope.row.gradeRank : "不参与排名" }}
            </template>
          </el-table-column>
          <el-table-column label="班级名次" align="center" prop="classRank">
            <template #default="scope">
              {{ scope.row.complete ? scope.row.classRank : "不参与排名" }}
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup name="ScoreStat">
import { listStudent, listStudentClassOptions } from "@/api/system/student"
import { listExam } from "@/api/system/exam"
import { getScoreRanking, getScoreTranscript, getClassStat } from "@/api/system/score"
import auth from "@/plugins/auth"
import * as echarts from "echarts"

const { proxy } = getCurrentInstance()

/** 六科代码与中文名，与后端 StuScoreSubject 保持一致 */
const SUBJECT_FIELDS = [
  { key: "chinese", label: "语文" },
  { key: "math", label: "数学" },
  { key: "english", label: "英语" },
  { key: "physics", label: "物理" },
  { key: "chemistry", label: "化学" },
  { key: "biology", label: "生物" }
]

const activeTab = ref("ranking")

/** 班级统计：六科各用一个固定的分类色（dataviz 参考调色板，槽位顺序不可打乱） */
const SUBJECT_COLORS_LIGHT = ["#2a78d6", "#eb6834", "#1baf7a", "#eda100", "#e87ba4", "#008300"]
const SUBJECT_COLORS_DARK = ["#3987e5", "#d95926", "#199e70", "#c98500", "#d55181", "#0ca30c"]
const statQuery = reactive({ examId: undefined, grade: undefined })
const classStatRows = ref([])
const classStatLoading = ref(false)
const classChartRef = ref(null)
let classChart = null
const studentOptions = ref([])
const examOptions = ref([])
const assignedClasses = ref([])
const isAdmin = auth.hasRole("admin")

const rankQuery = reactive({ examId: undefined, grade: undefined, className: undefined, scope: "GRADE", subject: "TOTAL" })
const rankList = ref([])
const rankLoading = ref(false)
let rankingRequest = 0

const transcriptQuery = reactive({ studentId: undefined })
const transcriptList = ref([])
const transcriptLoading = ref(false)

/** 班主任按分配的班级确定年级，管理员按学生数据确定选项。 */
const rankingClassSource = computed(() => isAdmin ? studentOptions.value : assignedClasses.value)
const gradeOptions = computed(() => [...new Set(rankingClassSource.value.map(item => item.grade).filter(Boolean))])

/** 班级下拉选项：取所选年级下出现过的班级 */
const classOptions = computed(() => [...new Set(
  rankingClassSource.value
    .filter(item => item.grade === rankQuery.grade && item.className)
    .map(item => item.className)
)])

/** 把缺考科目代码（如 "chinese,physics"）转换为中文（如 "语文、物理"） */
function formatAbsentSubjects(codes) {
  if (!codes) {
    return "无"
  }
  return codes
    .split(",")
    .map(code => SUBJECT_FIELDS.find(item => item.key === code)?.label || code)
    .join("、")
}

/** 查询考试排名 */
function loadRanking() {
  if (!rankQuery.examId) {
    proxy.$modal.msgWarning("请先选择考试")
    return
  }
  if (!rankQuery.grade) {
    proxy.$modal.msgWarning("请先选择年级")
    return
  }
  if (rankQuery.scope === "CLASS" && !rankQuery.className) {
    proxy.$modal.msgWarning("请先选择班级")
    return
  }
  rankLoading.value = true
  const requestId = ++rankingRequest
  // 年级排名不需要班级参数
  const query = { ...rankQuery, className: rankQuery.scope === "CLASS" ? rankQuery.className : undefined }
  getScoreRanking(query).then(response => {
    if (requestId === rankingRequest) rankList.value = response.data
  }).finally(() => {
    if (requestId === rankingRequest) rankLoading.value = false
  })
}

watch(() => [rankQuery.examId, rankQuery.grade, rankQuery.className, rankQuery.scope, rankQuery.subject], () => {
  rankingRequest++
  rankList.value = []
  rankLoading.value = false
})

/** 班级统计中的年级选项（取自已加载的班级数据） */
const classGradeOptions = computed(() => [...new Set(classStatRows.value.map(item => item.grade))])
const filteredClassStatRows = computed(() => statQuery.grade
  ? classStatRows.value.filter(item => item.grade === statQuery.grade)
  : classStatRows.value)

/** 加载班级平均分并绘制分组柱状图 */
function loadClassStat() {
  if (!statQuery.examId) {
    classStatRows.value = []
    return
  }
  classStatLoading.value = true
  getClassStat(statQuery.examId).then(response => {
    classStatRows.value = response.data
    nextTick(drawClassChart)
  }).finally(() => {
    classStatLoading.value = false
  })
}

/** 绘制：横轴为班级，每个班级六根柱子（分组柱状图），顶部图例说明颜色 */
function drawClassChart() {
  if (!classChartRef.value || filteredClassStatRows.value.length === 0) {
    return
  }
  if (!classChart) {
    classChart = echarts.init(classChartRef.value)
    window.addEventListener("resize", () => classChart && classChart.resize())
  }
  classChart.resize()
  const rows = filteredClassStatRows.value
  const dark = document.documentElement.classList.contains("dark")
  const colors = dark ? SUBJECT_COLORS_DARK : SUBJECT_COLORS_LIGHT
  const subjects = [
    { name: "语文", key: "avgChinese" },
    { name: "数学", key: "avgMath" },
    { name: "英语", key: "avgEnglish" },
    { name: "物理", key: "avgPhysics" },
    { name: "化学", key: "avgChemistry" },
    { name: "生物", key: "avgBiology" }
  ]
  classChart.setOption({
    color: colors,
    tooltip: { trigger: "axis", axisPointer: { type: "shadow" } },
    legend: { top: 0, data: subjects.map(item => item.name) },
    grid: { left: 48, right: 16, top: 48, bottom: 40 },
    xAxis: { type: "category", data: rows.map(item => `${item.grade}${item.className}`), axisTick: { show: false } },
    yAxis: { type: "value", name: "平均分", splitLine: { lineStyle: { opacity: 0.4 } } },
    series: subjects.map(item => ({
      name: item.name,
      type: "bar",
      barMaxWidth: 18,
      barGap: "20%",
      itemStyle: { borderRadius: [4, 4, 0, 0] },
      data: rows.map(row => Number(row[item.key]))
    }))
  }, true)
}

/** 查询学生成绩单 */
function loadTranscript() {
  if (!transcriptQuery.studentId) {
    transcriptList.value = []
    return
  }
  transcriptLoading.value = true
  getScoreTranscript(transcriptQuery.studentId).then(response => {
    transcriptList.value = response.data
  }).finally(() => {
    transcriptLoading.value = false
  })
}

watch(() => statQuery.grade, () => nextTick(drawClassChart))

onBeforeUnmount(() => {
  if (classChart) {
    classChart.dispose()
    classChart = null
  }
})

/** 加载学生、考试下拉选项 */
function loadOptions() {
  if (!isAdmin) {
    listStudentClassOptions().then(response => { assignedClasses.value = response.data || [] })
  }
  listStudent({ pageNum: 1, pageSize: 1000 }).then(response => {
    studentOptions.value = response.rows
  })
  listExam({ pageNum: 1, pageSize: 1000 }).then(response => {
    examOptions.value = response.rows
  })
}

loadOptions()
</script>

<style scoped>
.class-chart {
  width: 100%;
  height: 380px;
}
.stat-note {
  margin-top: 12px;
  color: #909399;
  font-size: 13px;
}
</style>
