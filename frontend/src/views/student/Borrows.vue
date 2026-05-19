<template>
  <div class="borrows-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的借用记录</span>
          <el-radio-group v-model="activeTab" @change="loadBorrows">
            <el-radio-button label="current">当前借用</el-radio-button>
            <el-radio-button label="history">借用历史</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <!-- 借用列表 -->
      <el-table :data="borrowList" border v-loading="loading" :default-sort="{prop: 'borrowTime', order: 'descending'}" width="100%">
        <el-table-column prop="deviceName" label="设备名称" width="150" />
        <el-table-column prop="deviceCode" label="设备编号" width="120" />
        <el-table-column prop="borrowTime" label="借用时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.borrowTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="dueTime" label="应还时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.dueTime) }}
          </template>
        </el-table-column>
        <el-table-column label="归还状态" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'returned'" type="success" size="small">已归还</el-tag>
            <el-tag v-else-if="row.isOverdue" type="danger" size="small">超时{{ row.overdueDays }}天</el-tag>
            <el-tag v-else type="warning" size="small">剩余{{ row.remainingDays }}天</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="returnCode" label="归还凭证码" width="150" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'borrowed' && row.returnCode"
              link
              type="primary"
              size="small"
            >
              归还凭证
            </el-button>
            <el-button
              v-if="row.violation"
              link
              type="danger"
              size="small"
              @click="handleAppeal(row)"
            >
              申诉
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="borrowList.length === 0 && !loading" description="暂无借用记录" />

      <!-- 分页 -->
      <el-pagination
        v-if="total > 0"
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="loadBorrows"
        class="pagination"
      />
    </el-card>

    <!-- 申诉对话框 -->
    <el-dialog v-model="appealDialogVisible" title="违规申诉" width="500px">
      <el-form :model="appealForm" label-width="100px">
        <el-form-item label="申诉理由">
          <el-input
            v-model="appealForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入申诉理由"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="appealDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAppeal">提交申诉</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { studentApi } from '../../api/student'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

const loading = ref(false)
const activeTab = ref('current')
const borrowList = ref([])
const total = ref(0)
const appealDialogVisible = ref(false)
const currentViolationId = ref(null)

const pagination = reactive({
  page: 1,
  size: 10
})

const appealForm = reactive({
  reason: ''
})

const loadBorrows = async () => {
  loading.value = true
  try {
    const params = {
      type: activeTab.value,
      page: pagination.page,
      size: pagination.size
    }
    const res = await studentApi.getBorrows(params)
    borrowList.value = res.list
    total.value = res.total
  } catch (error) {
    ElMessage.error('加载借用记录失败')
  } finally {
    loading.value = false
  }
}

const handleAppeal = (row) => {
  if (row.violation) {
    currentViolationId.value = row.violation.id
    appealDialogVisible.value = true
  }
}

const submitAppeal = async () => {
  if (!appealForm.reason) {
    ElMessage.warning('请输入申诉理由')
    return
  }
  
  try {
    await studentApi.appealViolation(currentViolationId.value, appealForm)
    ElMessage.success('申诉提交成功')
    appealDialogVisible.value = false
    appealForm.reason = ''
  } catch (error) {
    ElMessage.error('申诉提交失败')
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

.pagination {
  margin-top: 20px;
  justify-content: center;
}
</style>
