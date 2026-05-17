<template>
  <div class="borrows-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>借用归还管理</span>
          <div>
            <el-button type="primary" @click="showBorrowDialog">借用登记</el-button>
            <el-button type="success" @click="showReturnDialog">归还登记</el-button>
          </div>
        </div>
      </template>

      <!-- 筛选 -->
      <el-form :inline="true">
        <el-form-item label="关键词">
          <el-input v-model="keyword" placeholder="设备名/学生名" clearable />
        </el-form-item>
        <el-form-item label="超时">
          <el-select v-model="isOverdue" placeholder="全部" clearable>
            <el-option label="是" :value="true" />
            <el-option label="否" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadBorrows">搜索</el-button>
        </el-form-item>
      </el-form>

      <!-- 借用列表 -->
      <el-table :data="borrowList" border v-loading="loading">
        <el-table-column prop="deviceName" label="设备名称" width="150" />
        <el-table-column prop="deviceCode" label="设备编号" width="120" />
        <el-table-column prop="studentName" label="学生姓名" width="100" />
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="borrowTime" label="借用时间" width="180">
          <template #default="{ row }">{{ formatTime(row.borrowTime) }}</template>
        </el-table-column>
        <el-table-column prop="dueTime" label="应还时间" width="180">
          <template #default="{ row }">{{ formatTime(row.dueTime) }}</template>
        </el-table-column>
        <el-table-column prop="overdueDays" label="超时天数" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.overdueDays > 0" type="danger">{{ row.overdueDays }}天</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'overdue' ? 'danger' : 'warning'">
              {{ row.status === 'overdue' ? '超时' : '借用中' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button text type="warning" @click="handleRemind(row)">催还</el-button>
            <el-button text type="danger">标记丢失</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="borrowList.length === 0 && !loading" description="暂无借用记录" />
    </el-card>

    <!-- 借用登记对话框 -->
    <el-dialog v-model="borrowDialogVisible" title="借用登记" width="600px">
      <el-form :model="borrowForm" :rules="borrowRules" ref="borrowFormRef" label-width="120px">
        <el-form-item label="设备编号" prop="deviceCode">
          <el-input v-model="borrowForm.deviceCode" placeholder="扫码或手动输入" />
        </el-form-item>
        <el-form-item label="学生学号" prop="studentNo">
          <el-input v-model="borrowForm.studentNo" placeholder="请输入学号" />
        </el-form-item>
        <el-form-item label="应还时间" prop="dueTime">
          <el-date-picker
            v-model="borrowForm.dueTime"
            type="datetime"
            placeholder="选择应还时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="borrowForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="borrowDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitBorrow">确定</el-button>
      </template>
    </el-dialog>

    <!-- 归还登记对话框 -->
    <el-dialog v-model="returnDialogVisible" title="归还登记" width="600px">
      <el-form :model="returnForm" label-width="120px">
        <el-form-item label="设备编号" required>
          <el-input v-model="returnForm.deviceCode" placeholder="扫码或手动输入" />
        </el-form-item>
        <el-form-item label="设备状态" required>
          <el-select v-model="returnForm.equipmentCondition" placeholder="请选择">
            <el-option label="正常" value="good" />
            <el-option label="磨损" value="worn" />
            <el-option label="损坏" value="damaged" />
            <el-option label="需清洁" value="clean" />
          </el-select>
        </el-form-item>
        <el-form-item label="违规类型">
          <el-select v-model="returnForm.violationType" placeholder="请选择">
            <el-option label="无" value="none" />
            <el-option label="超时" value="overdue" />
            <el-option label="损坏" value="damage" />
          </el-select>
        </el-form-item>
        <el-form-item label="违规说明" v-if="returnForm.violationType !== 'none'">
          <el-input v-model="returnForm.violationDescription" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="returnDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReturn">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { teacherApi } from '../../api/teacher'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

const loading = ref(false)
const borrowList = ref([])
const keyword = ref('')
const isOverdue = ref(null)
const borrowDialogVisible = ref(false)
const returnDialogVisible = ref(false)
const borrowFormRef = ref(null)

const borrowForm = reactive({
  deviceCode: '',
  studentNo: '',
  dueTime: '',
  remark: ''
})

const returnForm = reactive({
  deviceCode: '',
  equipmentCondition: '',
  violationType: 'none',
  violationDescription: ''
})

const borrowRules = {
  deviceCode: [{ required: true, message: '请输入设备编号', trigger: 'blur' }],
  studentNo: [{ required: true, message: '请输入学生学号', trigger: 'blur' }],
  dueTime: [{ required: true, message: '请选择应还时间', trigger: 'change' }]
}

const loadBorrows = async () => {
  loading.value = true
  try {
    const params = {
      keyword: keyword.value,
      isOverdue: isOverdue.value
    }
    const res = await teacherApi.getCurrentBorrows(params)
    borrowList.value = res.list
  } catch (error) {
    ElMessage.error('加载借用列表失败')
  } finally {
    loading.value = false
  }
}

const showBorrowDialog = () => {
  borrowDialogVisible.value = true
}

const submitBorrow = async () => {
  if (!borrowFormRef.value) return
  
  await borrowFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await teacherApi.createBorrow(borrowForm)
        ElMessage.success('借用登记成功')
        borrowDialogVisible.value = false
        loadBorrows()
      } catch (error) {
        ElMessage.error('操作失败')
      }
    }
  })
}

const showReturnDialog = () => {
  returnDialogVisible.value = true
}

const submitReturn = async () => {
  try {
    await teacherApi.returnBorrow(returnForm)
    ElMessage.success('归还登记成功')
    returnDialogVisible.value = false
    loadBorrows()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleRemind = async (row) => {
  try {
    await teacherApi.remindReturn(row.id)
    ElMessage.success('催还通知已发送')
  } catch (error) {
    ElMessage.error('发送失败')
  }
}

const formatTime = (time) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

onMounted(() => {
  loadBorrows()
})
</script>

<style scoped>
.borrows-page {
  max-width: 1400px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
