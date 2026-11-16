<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="auto">
      <el-form-item label="学号" prop="studentNo">
        <el-input
          v-model="queryParams.studentNo"
          placeholder="请输入学号"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="学生姓名" prop="studentName">
        <el-input
          v-model="queryParams.studentName"
          placeholder="请输入学生姓名"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="年级" prop="grade">
        <el-select v-model="queryParams.grade" placeholder="请选择年级" clearable style="width: 180px" @change="queryParams.className = undefined">
          <el-option v-for="grade in filterGrades" :key="grade" :label="grade" :value="grade" />
        </el-select>
      </el-form-item>
      <el-form-item label="班级名称" prop="className">
        <el-select v-model="queryParams.className" placeholder="请选择班级" clearable style="width: 180px">
          <el-option v-for="className in filterClasses" :key="className" :label="className" :value="className" />
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
          v-hasPermi="['system:student:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['system:student:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['system:student:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="info"
          plain
          icon="Upload"
          @click="handleImport"
          v-hasPermi="['system:student:import']"
        >导入</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['system:student:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="studentList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="学生ID" align="center" prop="id" />
      <el-table-column label="学号" align="center" prop="studentNo" />
      <el-table-column label="学生姓名" align="center" prop="studentName" />
      <el-table-column label="年级" align="center" prop="grade" />
      <el-table-column label="班级名称" align="center" prop="className" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['system:student:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['system:student:remove']">删除</el-button>
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

    <!-- 添加或修改学生信息对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="studentRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="学号" prop="studentNo">
              <el-input v-model="form.studentNo" placeholder="请输入学号" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="学生姓名" prop="studentName">
              <el-input v-model="form.studentName" placeholder="请输入学生姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="年级" prop="grade">
              <el-select v-model="form.grade" placeholder="请选择年级" filterable style="width: 100%" no-match-text="年级不存在">
                <el-option v-for="grade in grades" :key="grade" :label="grade" :value="grade" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="班级名称" prop="className">
              <el-select v-model="form.className" placeholder="请选择班级" filterable style="width: 100%" no-match-text="班级不存在">
                <el-option v-for="className in classes" :key="className" :label="className" :value="className" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="绑定账号" prop="userId">
              <el-select v-model="form.userId" placeholder="请选择学生账号（可留空）" filterable clearable style="width: 100%">
                <el-option
                  v-for="item in userOptions"
                  :key="item.userId"
                  :label="`${item.userName}（${item.nickName}）`"
                  :value="item.userId"
                />
              </el-select>
              <div class="form-tip">账号需拥有“学生”角色；一个账号只能绑定一个学生；清空即解除绑定。</div>
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
    <excel-import-dialog ref="importStudentRef" title="学生导入" action="/system/student/importData" template-action="/system/student/importTemplate" template-file-name="student_template" update-support-label="学号已存在时是否更新该学生信息" @success="getList" />
  </div>
</template>

<script setup name="Student">
import { listStudent, listStudentClassOptions, getStudent, delStudent, addStudent, updateStudent } from "@/api/system/student"
import { listUser } from "@/api/system/user"
import ExcelImportDialog from "@/components/ExcelImportDialog"
import auth from "@/plugins/auth"
import { classFilterOptions } from "./classOptions"

const { proxy } = getCurrentInstance()
const grades = ['高一', '高二', '高三']
const classes = Array.from({ length: 12 }, (_, index) => `${index + 1}班`)
const isAdmin = auth.hasRole('admin')
const assignedClasses = ref([])
const filterOptions = computed(() => classFilterOptions(grades, classes, assignedClasses.value, isAdmin, queryParams.value.grade))
const filterGrades = computed(() => filterOptions.value.grades)
const filterClasses = computed(() => filterOptions.value.classes)

/** 可绑定的系统账号（后端会校验角色和唯一性） */
const userOptions = ref([])

function loadUserOptions() {
  listUser({ pageNum: 1, pageSize: 1000 }).then(response => {
    userOptions.value = response.rows
  })
}

const studentList = ref([])
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
    studentNo: undefined,
    studentName: undefined,
    grade: undefined,
    className: undefined
  },
  rules: {
    studentNo: [
      { required: true, message: "学号不能为空", trigger: "blur" }
    ],
    studentName: [
      { required: true, message: "学生姓名不能为空", trigger: "blur" }
    ],
    grade: [
      { required: true, message: "请选择年级", trigger: "change" }
    ],
    className: [
      { required: true, message: "请选择班级", trigger: "change" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询学生信息列表 */
function getList() {
  loading.value = true
  listStudent(queryParams.value).then(response => {
    studentList.value = response.rows
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
    studentNo: null,
    studentName: null,
    grade: null,
    className: null,
    userId: null
  }
  proxy.resetForm("studentRef")
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
  title.value = "添加学生信息"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _id = row.id || ids.value
  getStudent(_id).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改学生信息"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["studentRef"].validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        updateStudent(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addStudent(form.value).then(() => {
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
  proxy.$modal.confirm('是否确认删除学生信息编号为"' + _ids + '"的数据项？').then(function() {
    return delStudent(_ids)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
/** 导入按钮操作 */
function handleImport() {
  proxy.$refs["importStudentRef"].open()
}

function handleExport() {
  proxy.download('system/student/export', {
    ...queryParams.value
  }, `student_${new Date().getTime()}.xlsx`)
}

getList()
if (!isAdmin) {
  listStudentClassOptions().then(response => { assignedClasses.value = response.data })
}
if (auth.hasPermi("system:user:list")) {
  loadUserOptions()
}
</script>

<style scoped>
.form-tip {
  color: #909399;
  font-size: 12px;
  line-height: 1.5;
}
</style>
