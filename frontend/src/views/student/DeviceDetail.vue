<template>
  <div class="device-detail">
    <el-card v-if="device">
      <el-page-header @back="$router.back()" title="返回" />
      
      <el-row :gutter="20" class="detail-content">
        <!-- 左侧图片 -->
        <el-col :span="10">
          <el-carousel height="400px" indicator-position="outside">
            <el-carousel-item v-for="(img, index) in device.images" :key="index">
              <img :src="img" class="carousel-image" alt="设备图片" />
            </el-carousel-item>
          </el-carousel>
        </el-col>

        <!-- 右侧信息 -->
        <el-col :span="14">
          <h2 class="device-title">{{ device.name }}</h2>
          
          <el-descriptions :column="2" border>
            <el-descriptions-item label="设备编号">{{ device.code }}</el-descriptions-item>
            <el-descriptions-item label="设备类别">{{ device.category }}</el-descriptions-item>
            <el-descriptions-item label="品牌">{{ device.brand || '-' }}</el-descriptions-item>
            <el-descriptions-item label="型号">{{ device.model || '-' }}</el-descriptions-item>
            <el-descriptions-item label="存放位置" :span="2">{{ device.location }}</el-descriptions-item>
            <el-descriptions-item label="购入日期">{{ device.purchaseDate || '-' }}</el-descriptions-item>
            <el-descriptions-item label="当前状态">
              <el-tag :type="getStatusType(device.status)">
                {{ getStatusText(device.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="规格参数" :span="2">
              {{ device.spec || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="技术参数" :span="2">
              {{ device.technicalParams || '-' }}
            </el-descriptions-item>
          </el-descriptions>

          <!-- 借用信息 -->
          <div v-if="device.status === 'borrowed'" class="borrow-info">
            <el-alert
              title="设备正在借用中"
              type="warning"
              :closable="false"
              show-icon
            >
              <p>借用人: {{ device.currentBorrower }}</p>
              <p>预计归还: {{ device.expectedReturnTime }}</p>
            </el-alert>
          </div>

          <!-- 预约按钮 -->
          <div class="action-buttons" v-if="device.status === 'available'">
            <el-button type="primary" size="large" @click="showReservationDialog">
              立即预约
            </el-button>
          </div>
        </el-col>
      </el-row>

      <!-- 可用时段 -->
      <el-divider />
      <h3>可用时段</h3>
      <el-table :data="device.availableSlots || []" border>
        <el-table-column prop="date" label="日期" width="150" />
        <el-table-column label="时段">
          <template #default="{ row }">
            <el-tag
              v-for="(slot, index) in row.slots"
              :key="index"
              :type="slot.status === 'available' ? 'success' : 'info'"
              style="margin-right: 10px; margin-bottom: 5px"
            >
              {{ slot.start }}-{{ slot.end }}
              ({{ slot.status === 'available' ? '可预约' : '已约满' }})
            </el-tag>
          </template>
        </el-table-column>
      </el-table>

      <!-- 评论区 -->
      <el-divider />
      <h3>用户评价</h3>
      <el-empty v-if="!device.comments?.length" description="暂无评价" />
      <div v-else class="comment-list">
        <div v-for="(comment, index) in device.comments" :key="index" class="comment-item">
          <div class="comment-header">
            <span class="comment-user">{{ comment.userName }}</span>
            <el-rate v-model="comment.rating" disabled />
            <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
          </div>
          <div class="comment-content">{{ comment.content }}</div>
          <div class="comment-footer">
            <el-button text>
              <el-icon><Star /></el-icon>
              {{ comment.likeCount }}
            </el-button>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 预约对话框 -->
    <el-dialog v-model="reservationDialogVisible" title="提交预约" width="600px">
      <el-form :model="reservationForm" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="预约日期" prop="startTime">
          <el-date-picker
            v-model="reservationForm.startTime"
            type="datetime"
            placeholder="选择开始时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="预计归还" prop="endTime">
          <el-date-picker
            v-model="reservationForm.endTime"
            type="datetime"
            placeholder="选择归还时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="用途说明" prop="purpose">
          <el-input
            v-model="reservationForm.purpose"
            type="textarea"
            :rows="3"
            placeholder="请说明使用用途"
          />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="reservationForm.agreeRules">
            我已阅读并同意《实验室设备使用须知》
          </el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reservationDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReservation" :loading="submitting">
          提交预约
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { studentApi } from '../../api/student'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import { Star } from '@element-plus/icons-vue'

const route = useRoute()
const device = ref(null)
const reservationDialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref(null)

const reservationForm = reactive({
  startTime: '',
  endTime: '',
  purpose: '',
  agreeRules: false
})

const rules = {
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择归还时间', trigger: 'change' }],
  purpose: [{ required: true, message: '请填写用途说明', trigger: 'blur' }]
}

const loadDeviceDetail = async () => {
  try {
    device.value = await studentApi.getDeviceDetail(route.params.id)
  } catch (error) {
    ElMessage.error('加载设备详情失败')
  }
}

const showReservationDialog = () => {
  reservationDialogVisible.value = true
}

const submitReservation = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      if (!reservationForm.agreeRules) {
        ElMessage.warning('请先同意使用须知')
        return
      }
      
      submitting.value = true
      try {
        await studentApi.createReservation({
          deviceId: device.value.id,
          startTime: dayjs(reservationForm.startTime).format('YYYY-MM-DDTHH:mm:ss'),
          endTime: dayjs(reservationForm.endTime).format('YYYY-MM-DDTHH:mm:ss'),
          purpose: reservationForm.purpose
        })
        ElMessage.success('预约提交成功')
        reservationDialogVisible.value = false
        // 重置表单
        reservationForm.startTime = ''
        reservationForm.endTime = ''
        reservationForm.purpose = ''
        reservationForm.agreeRules = false
      } catch (error) {
        ElMessage.error('预约提交失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

const getStatusType = (status) => {
  const map = {
    available: 'success',
    borrowed: 'warning',
    repair: 'danger'
  }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = {
    available: '可借用',
    borrowed: '已借出',
    repair: '维修中'
  }
  return map[status] || status
}

const formatTime = (time) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

onMounted(() => {
  loadDeviceDetail()
})
</script>

<style scoped>
.device-detail {
  max-width: 1200px;
  margin: 0 auto;
}

.detail-content {
  margin-top: 20px;
}

.carousel-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.device-title {
  margin: 0 0 20px 0;
  font-size: 24px;
  color: #333;
}

.borrow-info {
  margin-top: 20px;
}

.action-buttons {
  margin-top: 20px;
  text-align: center;
}

.comment-list {
  margin-top: 20px;
}

.comment-item {
  padding: 15px;
  border-bottom: 1px solid #f0f0f0;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 10px;
}

.comment-user {
  font-weight: bold;
  color: #333;
}

.comment-time {
  font-size: 12px;
  color: #999;
}

.comment-content {
  color: #666;
  line-height: 1.6;
}

.comment-footer {
  margin-top: 10px;
}
</style>
