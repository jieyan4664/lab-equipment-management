<template>
  <div class="reservations-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>预约审核</span>
          <el-radio-group v-model="activeTab" @change="loadReservations">
            <el-radio-button label="pending">待审核</el-radio-button>
            <el-radio-button label="approved">已通过</el-radio-button>
            <el-radio-button label="rejected">已拒绝</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <!-- 筛选 -->
      <el-form :inline="true">
        <el-form-item label="学生姓名">
          <el-input v-model="filterForm.studentName" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item label="设备名称">
          <el-input v-model="filterForm.deviceName" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadReservations">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 预约列表 -->
      <el-table :data="reservationList" border v-loading="loading">
        <el-table-column prop="studentName" label="学生姓名" width="100" />
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="deviceName" label="设备名称" width="150" />
        <el-table-column prop="deviceCode" label="设备编号" width="120" />
        <el-table-column prop="startTime" label="预约时间" width="180">
          <template #default="{ row }">{{ formatTime(row.startTime) }}</template>
        </el-table-column>
        <el-table-column prop="endTime" label="归还时间" width="180">
          <template #default="{ row }">{{ formatTime(row.endTime) }}</template>
        </el-table-column>
        <el-table-column prop="purpose" label="用途说明" show-overflow-tooltip />
        <el-table-column prop="waitingHours" label="等待时长" width="100">
          <template #default="{ row }">{{ row.waitingHours }}小时</template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right" v-if="activeTab === 'pending'">
          <template #default="{ row }">
            <el-button text type="success" @click="handleAudit(row, 'approve')">通过</el-button>
            <el-button text type="danger" @click="showRejectDialog(row)">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="reservationList.length === 0 && !loading" description="暂无预约记录" />

      <!-- 分页 -->
      <el-pagination
        v-if="total > 0"
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="loadReservations"
        class="pagination"
      />
    </el-card>

    <!-- 拒绝对话框 -->
    <el-dialog v-model="rejectDialogVisible" title="拒绝预约" width="500px">
      <el-form :model="rejectForm" label-width="100px">
        <el-form-item label="拒绝理由" required>
          <el-input v-model="rejectForm.reason" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReject">确定</el-button>
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
const activeTab = ref('pending')
const reservationList = ref([])
const total = ref(0)
const rejectDialogVisible = ref(false)
const currentReservation = ref(null)

const filterForm = reactive({
  studentName: '',
  deviceName: ''
})

const pagination = reactive({
  page: 1,
  size: 10
})

const rejectForm = reactive({
  reason: ''
})

const loadReservations = async () => {
  loading.value = true
  try {
    const params = {
      status: activeTab.value,
      ...filterForm,
      page: pagination.page,
      size: pagination.size
    }
    const res = await teacherApi.getReservations(params)
    reservationList.value = res.list
    total.value = res.total
  } catch (error) {
    ElMessage.error('加载预约列表失败')
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  filterForm.studentName = ''
  filterForm.deviceName = ''
  pagination.page = 1
  loadReservations()
}

const handleAudit = async (row, result) => {
  try {
    await teacherApi.auditReservation(row.id, { result })
    ElMessage.success(result === 'approve' ? '审核通过' : '已拒绝')
    loadReservations()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const showRejectDialog = (row) => {
  currentReservation.value = row
  rejectForm.reason = ''
  rejectDialogVisible.value = true
}

const submitReject = async () => {
  if (!rejectForm.reason) {
    ElMessage.warning('请填写拒绝理由')
    return
  }
  
  try {
    await teacherApi.auditReservation(currentReservation.value.id, {
      result: 'reject',
      reason: rejectForm.reason
    })
    ElMessage.success('已拒绝')
    rejectDialogVisible.value = false
    loadReservations()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const formatTime = (time) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

onMounted(() => {
  loadReservations()
})
</script>

<style scoped>
.reservations-page {
  max-width: 1400px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination {
  margin-top: 20px;
  justify-content: center;
}
</style>
