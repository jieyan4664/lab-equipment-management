<template>
  <div class="student-dashboard">
    <!-- 欢迎横幅 -->
    <el-card class="welcome-card">
      <div class="welcome-content">
        <h2>欢迎, {{ dashboardData.student?.name }}</h2>
        <p>学号: {{ dashboardData.student?.studentNo }} | 实验室: {{ labTypeText }}</p>
        <p v-if="dashboardData.student?.accessExpire">
          准入有效期: {{ dashboardData.student.accessExpire }}
        </p>
      </div>
    </el-card>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="12">
        <el-card class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#409EFF"><Box /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ dashboardData.stats?.currentBorrowCount || 0 }}</div>
              <div class="stat-label">当前借用设备数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#67C23A"><Calendar /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ dashboardData.stats?.pendingReservationCount || 0 }}</div>
              <div class="stat-label">待处理预约数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 公告栏 -->
    <el-card class="announcement-card">
      <template #header>
        <div class="card-header">
          <span>最新公告</span>
          <el-button text @click="$router.push('/student/announcements')">查看全部</el-button>
        </div>
      </template>
      <el-empty v-if="!dashboardData.announcements?.length" description="暂无公告" />
      <div v-else class="announcement-list">
        <div
          v-for="item in dashboardData.announcements"
          :key="item.id"
          class="announcement-item"
          :class="{ 'unread': !item.isRead }"
        >
          <el-icon v-if="!item.isRead" color="#409EFF"><Bell /></el-icon>
          <span class="announcement-title">{{ item.title }}</span>
          <span class="announcement-time">{{ formatTime(item.publishTime) }}</span>
        </div>
      </div>
    </el-card>

    <!-- 快速入口 -->
    <el-card class="quick-access-card">
      <template #header>
        <span>快速入口</span>
      </template>
      <el-row :gutter="20">
        <el-col :span="8">
          <div class="quick-item" @click="$router.push('/student/devices')">
            <el-icon size="40" color="#409EFF"><Monitor /></el-icon>
            <div>设备查询</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="quick-item" @click="$router.push('/student/reservations')">
            <el-icon size="40" color="#67C23A"><Calendar /></el-icon>
            <div>我的预约</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="quick-item" @click="$router.push('/student/borrows')">
            <el-icon size="40" color="#E6A23C"><Box /></el-icon>
            <div>我的借用</div>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { studentApi } from '../../api/student'
import dayjs from 'dayjs'
import { Bell, Box, Calendar, Monitor } from '@element-plus/icons-vue'

const dashboardData = ref({})

const labTypeText = computed(() => {
  const type = dashboardData.value.student?.labType
  return type === 'bio' ? '生物实验室' : type === 'chem' ? '化学实验室' : ''
})

const loadDashboard = async () => {
  try {
    dashboardData.value = await studentApi.getDashboard()
  } catch (error) {
    console.error('加载首页数据失败:', error)
  }
}

const formatTime = (time) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

onMounted(() => {
  loadDashboard()
})
</script>

<style scoped>
.student-dashboard {
  max-width: 1200px;
  margin: 0 auto;
}

.welcome-card {
  margin-bottom: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.welcome-content h2 {
  margin: 0 0 10px 0;
}

.welcome-content p {
  margin: 5px 0;
  opacity: 0.9;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  cursor: pointer;
  transition: transform 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-icon {
  font-size: 48px;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #999;
  margin-top: 5px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.announcement-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.3s;
}

.announcement-item:hover {
  background-color: #f5f7fa;
}

.announcement-item.unread {
  background-color: #ecf5ff;
}

.announcement-title {
  flex: 1;
  color: #333;
}

.announcement-time {
  font-size: 12px;
  color: #999;
}

.quick-access-card {
  margin-top: 20px;
}

.quick-item {
  text-align: center;
  padding: 30px 20px;
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.3s;
}

.quick-item:hover {
  background-color: #f5f7fa;
  transform: translateY(-3px);
}

.quick-item div {
  margin-top: 10px;
  font-size: 16px;
  color: #333;
}
</style>
