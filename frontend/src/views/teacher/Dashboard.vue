<template>
  <div class="teacher-dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6" v-for="(stat, index) in statsList" :key="index">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" :style="{ backgroundColor: stat.color }">
              <el-icon :size="30" color="#fff"><component :is="stat.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stat.value }}</div>
              <div class="stat-label">{{ stat.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <!-- 待办事项 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>待办事项</span>
          </template>
          <el-empty v-if="!dashboardData.todos?.length" description="暂无待办" />
          <div v-else class="todo-list">
            <div v-for="todo in dashboardData.todos" :key="todo.id" class="todo-item">
              <el-tag :type="getPriorityType(todo.priority)">
                {{ getPriorityText(todo.priority) }}
              </el-tag>
              <span class="todo-content">{{ todo.deviceName }} - {{ todo.studentName }}</span>
              <span class="todo-time">{{ todo.time }}</span>
              <el-button text type="primary">处理</el-button>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 借用TOP5设备 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>借用TOP5设备</span>
          </template>
          <div ref="topDevicesChart" style="height: 300px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <!-- 月度借用趋势 -->
      <el-col :span="24">
        <el-card>
          <template #header>
            <span>月度借用趋势</span>
          </template>
          <div ref="monthlyTrendChart" style="height: 300px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { teacherApi } from '../../api/teacher'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { Monitor, Calendar, Box, Warning } from '@element-plus/icons-vue'

const dashboardData = ref({})
const topDevicesChart = ref(null)
const monthlyTrendChart = ref(null)

const statsList = ref([])

const loadDashboard = async () => {
  try {
    dashboardData.value = await teacherApi.getDashboard()
    
    // 构建统计列表
    const stats = dashboardData.value.stats
    statsList.value = [
      { label: '设备总数', value: stats.deviceCount, icon: Monitor, color: '#409EFF' },
      { label: '可借用数', value: stats.availableCount, icon: Monitor, color: '#67C23A' },
      { label: '今日预约', value: stats.todayReservationCount, icon: Calendar, color: '#E6A23C' },
      { label: '待审核数', value: stats.pendingAuditCount, icon: Calendar, color: '#F56C6C' },
      { label: '借用中数', value: stats.borrowedCount, icon: Box, color: '#409EFF' },
      { label: '超时数', value: stats.overdueCount, icon: Warning, color: '#F56C6C' },
      { label: '活跃学生', value: stats.activeStudentCount, icon: Monitor, color: '#67C23A' },
      { label: '违规学生', value: stats.violationStudentCount, icon: Warning, color: '#F56C6C' }
    ]
    
    await nextTick()
    initCharts()
  } catch (error) {
    ElMessage.error('加载仪表盘数据失败')
  }
}

const initCharts = () => {
  // TOP设备图表
  if (topDevicesChart.value && dashboardData.value.charts?.topDevices) {
    const chart = echarts.init(topDevicesChart.value)
    const data = dashboardData.value.charts.topDevices
    chart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: {
        type: 'category',
        data: data.map(item => item.name)
      },
      yAxis: { type: 'value' },
      series: [{
        data: data.map(item => item.count),
        type: 'bar',
        itemStyle: { color: '#409EFF' }
      }]
    })
  }

  // 月度趋势图表
  if (monthlyTrendChart.value && dashboardData.value.charts?.monthlyTrend) {
    const chart = echarts.init(monthlyTrendChart.value)
    const data = dashboardData.value.charts.monthlyTrend
    chart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: {
        type: 'category',
        data: data.map(item => item.month)
      },
      yAxis: { type: 'value' },
      series: [{
        data: data.map(item => item.count),
        type: 'line',
        smooth: true,
        itemStyle: { color: '#67C23A' },
        areaStyle: { opacity: 0.3 }
      }]
    })
  }
}

const getPriorityType = (priority) => {
  const map = { high: 'danger', medium: 'warning', low: 'info' }
  return map[priority] || 'info'
}

const getPriorityText = (priority) => {
  const map = { high: '高优先级', medium: '中优先级', low: '低优先级' }
  return map[priority] || priority
}

onMounted(() => {
  loadDashboard()
})
</script>

<style scoped>
.teacher-dashboard {
  max-width: 1400px;
  margin: 0 auto;
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
  gap: 15px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #999;
  margin-top: 5px;
}

.todo-list {
  max-height: 300px;
  overflow-y: auto;
}

.todo-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.todo-content {
  flex: 1;
  color: #333;
}

.todo-time {
  font-size: 12px;
  color: #999;
}
</style>
