<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryRef" :model="queryParams" :inline="true" label-width="80px">
      <el-form-item label="用户昵称" prop="nickName">
        <el-input v-model="queryParams.nickName" placeholder="请输入用户昵称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="用户名称" prop="userName">
        <el-input v-model="queryParams.userName" placeholder="请输入用户名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 180px">
          <el-option v-for="dict in sys_normal_disable" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="角色" prop="roleId">
        <el-select v-model="queryParams.roleId" placeholder="请选择角色" clearable style="width: 180px">
          <el-option v-for="role in searchRoles" :key="role.roleId" :label="role.roleName" :value="role.roleId" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['system:user:add']">新增</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['system:user:edit']">修改</el-button></el-col>
      <el-col :span="1.5"><el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['system:user:remove']">删除</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="userList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="用户昵称" prop="nickName" align="center" show-overflow-tooltip />
      <el-table-column label="用户名称" prop="userName" align="center" show-overflow-tooltip />
      <el-table-column label="角色" prop="roleNames" align="center" show-overflow-tooltip />
      <el-table-column label="状态" align="center">
        <template #default="{ row }">
          <el-switch v-model="row.status" active-value="0" inactive-value="1" :disabled="row.userId === 1" @change="handleStatusChange(row)" v-hasPermi="['system:user:edit']" />
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="150">
        <template #default="{ row }">
          <template v-if="row.userId !== 1">
            <el-tooltip content="修改"><el-button link type="primary" icon="Edit" @click="handleUpdate(row)" v-hasPermi="['system:user:edit']" /></el-tooltip>
            <el-tooltip content="删除"><el-button link type="primary" icon="Delete" @click="handleDelete(row)" v-hasPermi="['system:user:remove']" /></el-tooltip>
            <el-tooltip content="重置密码"><el-button link type="primary" icon="Key" @click="handleResetPwd(row)" v-hasPermi="['system:user:resetPwd']" /></el-tooltip>
          </template>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="userRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="用户昵称" prop="nickName"><el-input v-model="form.nickName" placeholder="请输入用户昵称" maxlength="30" /></el-form-item>
        <el-form-item label="用户名称" prop="userName"><el-input v-model="form.userName" placeholder="请输入用户名称" maxlength="30" :disabled="form.userId != null" /></el-form-item>
        <el-form-item v-if="form.userId == null" label="用户密码" prop="password" :rules="pwdValidator"><el-input v-model="form.password" placeholder="请输入用户密码" type="password" maxlength="20" show-password /></el-form-item>
        <el-form-item label="状态" prop="status"><el-radio-group v-model="form.status"><el-radio v-for="dict in sys_normal_disable" :key="dict.value" :value="dict.value">{{ dict.label }}</el-radio></el-radio-group></el-form-item>
        <el-form-item label="角色" prop="roleIds"><el-select v-model="form.roleIds" multiple placeholder="请选择角色"><el-option v-for="role in roleOptions" :key="role.roleId" :label="role.roleName" :value="role.roleId" :disabled="role.status === '1'" /></el-select></el-form-item>
      </el-form>
      <template #footer><el-button type="primary" @click="submitForm">确 定</el-button><el-button @click="open = false">取 消</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup name="User">
import { usePasswordRule } from '@/utils/passwordRule'
import { changeUserStatus, listUser, resetUserPwd, delUser, getUser, updateUser, addUser } from '@/api/system/user'
import { listRole } from '@/api/system/role'

const { proxy } = getCurrentInstance()
const { pwdValidator, pwdPromptValidator } = usePasswordRule()
const { sys_normal_disable } = useDict('sys_normal_disable')
const userList = ref([]), searchRoles = ref([]), roleOptions = ref([])
const open = ref(false), loading = ref(false), showSearch = ref(true)
const ids = ref([]), single = ref(true), multiple = ref(true), total = ref(0), title = ref('')
const queryParams = ref({ pageNum: 1, pageSize: 10, userName: undefined, nickName: undefined, status: undefined, roleId: undefined })
const form = ref({})
const rules = {
  userName: [{ required: true, message: '用户名称不能为空', trigger: 'blur' }, { min: 2, max: 20, message: '用户名称长度必须介于 2 和 20 之间', trigger: 'blur' }],
  nickName: [{ required: true, message: '用户昵称不能为空', trigger: 'blur' }],
  roleIds: [{ required: true, type: 'array', min: 1, message: '请选择角色', trigger: 'change' }]
}
function getList() {
  loading.value = true
  return listUser(queryParams.value).then(res => { userList.value = res.rows; total.value = res.total }).finally(() => { loading.value = false })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm('queryRef'); handleQuery() }
function handleSelectionChange(selection) { ids.value = selection.map(item => item.userId); single.value = selection.length !== 1; multiple.value = !selection.length }
function handleDelete(row) {
  const userIds = row.userId || ids.value
  proxy.$modal.confirm('是否确认删除所选用户？').then(() => delUser(userIds)).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') }).catch(() => {})
}
function handleStatusChange(row) {
  const text = row.status === '0' ? '启用' : '停用'
  proxy.$modal.confirm(`确认要${text}用户「${row.userName}」吗？`).then(() => changeUserStatus(row.userId, row.status)).then(() => proxy.$modal.msgSuccess(text + '成功')).catch(() => { row.status = row.status === '0' ? '1' : '0' })
}
function handleResetPwd(row) {
  proxy.$prompt(`请输入「${row.userName}」的新密码`, '重置密码', { confirmButtonText: '确定', cancelButtonText: '取消', closeOnClickModal: false, inputValidator: pwdPromptValidator })
    .then(({ value }) => resetUserPwd(row.userId, value).then(() => proxy.$modal.msgSuccess('密码重置成功'))).catch(() => {})
}
function reset() { form.value = { userName: undefined, nickName: undefined, password: undefined, status: '0', roleIds: [], postIds: [] }; proxy.resetForm('userRef') }
function handleAdd() {
  reset()
  getUser().then(response => { roleOptions.value = response.roles; title.value = '添加用户'; open.value = true })
}
function handleUpdate(row) {
  reset()
  getUser(row.userId || ids.value[0]).then(response => {
    form.value = { ...response.data, roleIds: response.roleIds, postIds: response.postIds }
    roleOptions.value = response.roles; title.value = '修改用户'; open.value = true
  })
}
function submitForm() {
  proxy.$refs.userRef.validate(valid => {
    if (!valid) return
    const save = form.value.userId != null ? updateUser : addUser
    save(form.value).then(() => { proxy.$modal.msgSuccess('保存成功'); open.value = false; getList() })
  })
}
onMounted(() => {
  getList()
  listRole({ pageNum: 1, pageSize: 100 }).then(res => { searchRoles.value = res.rows })
})
</script>
