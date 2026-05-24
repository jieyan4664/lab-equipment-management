<template>
  <div class="reservations-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的预约</span>
          <el-radio-group v-model="activeTab" @change="loadReservations">
            <el-radio-button label="current">当前预约</el-radio-button>
            <el-radio-button label="history">历史预约</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <!-- 筛选 -->
      <el-form :inline="true" v-if="activeTab === 'current'">
        <el-form-item label="状态">
          <el-select v-model="filterStatus" placeholder="全部" clearable @change="loadReservations">
            <el-option label="待审核" value="pending" />
            <el-option label="已通过" value="approved" />
            <el-option label="被拒绝" value="rejected" />
            <el-option label="已取消" value="cancelled" />
          </el-select>
        </el-form-item>
      </el-form>

      <!-- 预约列表 -->
      <el-table :data="reservationList" border v-loading="loading">
        <el-table-column prop="deviceName" label="设备名称" width="150" />
        <el-table-column prop="deviceCode" label="设备编号" width="120" />
        <el-table-column prop="startTime" label="预约时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="endTime" label="归还时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="purpose" label="用途说明" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="申请时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'pending'"
              text
              type="danger"
              @click="handleCancel(row)"
            >
              取消预约
            </el-button>
            <el-button
              v-if="row.status === 'rejected'"
              text
              type="primary"
            >
              查看详情
            </el-button>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { studentApi } from '../../api/student'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'

const loading = ref(false)
const activeTab = ref('current')
const filterStatus = ref('')
const reservationList = ref([])
const total = ref(0)

const pagination = reactive({
  page: 1,
  size: 10
})

const loadReservations = async () => {
  loading.value = true
  try {
    const params = {
      type: activeTab.value,
      status: filterStatus.value,
      page: pagination.page,
      size: pagination.size
    }
    const res = await studentApi.getReservations(params)
    reservationList.value = res.records
    total.value = res.total
  } catch (error) {
    ElMessage.error('加载预约列表失败')
  } finally {
    loading.value = false
  }
}

const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm('确定要取消该预约吗?', '提示', {
      type: 'warning'
    })
    await studentApi.cancelReservation(row.id)
    ElMessage.success('取消成功')
    loadReservations()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消失败')
    }
  }
}

const getStatusType = (status) => {
  const map = {
    pending: 'warning',
    approved: 'success',
    rejected: 'danger',
    cancelled: 'info'
  }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = {
    pending: '待审核',
    approved: '已通过',
    rejected: '被拒绝',
    cancelled: '已取消'
  }
  return map[status] || status
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
