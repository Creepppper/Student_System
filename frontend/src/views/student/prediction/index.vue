<template>
  <div class="app-container">
    <el-alert title="按历次考试各科平均分预估，排名为校内年级和班级排名，仅供参考。缺考及空成绩按 0 分计算。" type="info" show-icon :closable="false" />
    <el-alert v-if="errorMessage" class="prediction-content" :title="errorMessage" type="warning" show-icon :closable="false" />
    <div v-else v-loading="loading" class="prediction-content">
      <el-empty v-if="!loading && !prediction" description="暂无考试成绩，无法预估" />
      <template v-else-if="prediction">
        <p>已参考 {{ prediction.examCount }} 场考试</p>
        <el-table :data="[prediction]">
          <el-table-column v-for="item in subjects" :key="item.key" :label="item.label" :prop="item.key" align="center" />
          <el-table-column label="预估总分" prop="total" align="center" />
          <el-table-column label="年级预估名次" prop="gradeRank" align="center" />
          <el-table-column label="班级预估名次" prop="classRank" align="center" />
        </el-table>
      </template>
    </div>
  </div>
</template>

<script setup name="StudentPrediction">
import { getMyPrediction } from "@/api/system/score"

const subjects = [
  { key: "chinese", label: "语文" }, { key: "math", label: "数学" },
  { key: "english", label: "英语" }, { key: "physics", label: "物理" },
  { key: "chemistry", label: "化学" }, { key: "biology", label: "生物" }
]
const prediction = ref(null)
const loading = ref(true)
const errorMessage = ref("")

getMyPrediction().then(response => {
  prediction.value = response.data || null
}).catch(error => {
  errorMessage.value = error.message || "无法加载高考预估"
}).finally(() => { loading.value = false })
</script>

<style scoped>
.prediction-content { margin-top: 20px; }
</style>
