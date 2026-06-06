<template>
  <div class="students-page">
    <el-card>
      <template #header>
        <span>学生管理</span>
      </template>

      <!-- 筛选 -->
      <el-form :inline="true">
        <el-form-item label="班级">
          <el-input v-model="filterForm.class" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="filterForm.keyword" placeholder="姓名/学号" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadStudents">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 学生列表 -->
      <el-table :data="studentList" border v-loading="loading" width="100%">
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="class" label="班级" width="150" />
        <el-table-column prop="phone" label="联系电话" width="120" />
        <el-table-column prop="accessStatus" label="准入状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.accessStatus === 'normal' ? 'success' : 'danger'">
              {{ row.accessStatus === 'normal' ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="currentBorrowCount" label="当前借用" width="100" />
        <el-table-column prop="totalBorrowCount" label="累计借用" width="100" />
        <el-table-column prop="violationCount" label="违规次数" width="100" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="showEditDialog(row)">编辑</el-button>
            <el-button
              text
              :type="row.accessStatus === 'normal' ? 'danger' : 'success'"
              @click="handleToggleAccess(row)"
            >
              {{ row.accessStatus === 'normal' ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-if="total > 0"
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="loadStudents"
        class="pagination"
      />
    </el-card>

    <!-- 编辑对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑学生信息" width="600px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="班级">
          <el-input v-model="editForm.class" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="editForm.phone" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { teacherApi } from '../../api/teacher'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const studentList = ref([])
const total = ref(0)
const editDialogVisible = ref(false)
const currentStudent = ref(null)

const filterForm = reactive({
  class: '',
  keyword: ''
})

const pagination = reactive({
  page: 1,
  size: 10
})

const editForm = reactive({
  class: '',
  phone: '',
  email: ''
})

const loadStudents = async () => {
  loading.value = true
  try {
    const params = {
      ...filterForm,
      page: pagination.page,
      size: pagination.size
    }
    const res = await teacherApi.getStudents(params)
    studentList.value = res.list
    total.value = res.total
  } catch (error) {
    ElMessage.error('加载学生列表失败')
  } finally {
    loading.value = false
  }
}

const showEditDialog = (row) => {
  currentStudent.value = row
  Object.assign(editForm, row)
  editDialogVisible.value = true
}

const submitEdit = async () => {
  try {
    ElMessage.success('更新成功')
    editDialogVisible.value = false
    loadStudents()
  } catch (error) {
    ElMessage.error('更新失败')
  }
}

const handleToggleAccess = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要${row.accessStatus === 'normal' ? '禁用' : '启用'}该学生吗?`,
      '提示',
      { type: 'warning' }
    )
    await teacherApi.updateStudentAccess(row.id, {
      status: row.accessStatus === 'normal' ? 2 : 1
    })
    ElMessage.success('操作成功')
    loadStudents()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const handleReset = () => {
  filterForm.class = ''
  filterForm.keyword = ''
  pagination.page = 1
  loadStudents()
}

onMounted(() => {
  loadStudents()
})
</script>

<style scoped>
.students-page {
  max-width: 1400px;
  margin: 0 auto;
}

.pagination {
  margin-top: 20px;
  justify-content: center;
}
</style>
