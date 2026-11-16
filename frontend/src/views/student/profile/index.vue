<template>
  <div class="app-container" v-loading="loading">
    <el-alert v-if="!loading && !profile && !error" title="当前账号尚未绑定学生信息，请联系管理员。" type="info" show-icon :closable="false" />
    <el-alert v-if="error" :title="error" type="warning" show-icon :closable="false" />
    <el-descriptions v-if="profile" :column="1" border class="profile-table">
      <el-descriptions-item label="姓名">{{ profile.studentName }}</el-descriptions-item>
      <el-descriptions-item label="学号">{{ profile.studentNo }}</el-descriptions-item>
      <el-descriptions-item label="年级">{{ profile.grade }}</el-descriptions-item>
      <el-descriptions-item label="班级">{{ profile.className }}</el-descriptions-item>
      <el-descriptions-item label="班主任">{{ profile.teachers?.length ? profile.teachers.join('、') : '暂无' }}</el-descriptions-item>
    </el-descriptions>
  </div>
</template>

<script setup name="MyProfile">
import request from '@/utils/request'

const profile = ref(null)
const loading = ref(true)
const error = ref('')
request({ url: '/student/profile/my', method: 'get' }).then(response => {
  profile.value = response.data || null
}).catch(() => {
  error.value = '基本信息加载失败，请稍后重试。'
}).finally(() => { loading.value = false })
</script>

<style scoped>
.profile-table { max-width: 720px; }
</style>
