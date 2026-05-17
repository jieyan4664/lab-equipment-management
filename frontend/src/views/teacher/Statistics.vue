<template>
  <div class="statistics-page">
    <el-row :gutter="20">
      <!-- 设备借用统计 -->
      <el-col :span="24">
        <el-card>
          <template #header>
            <span>设备借用统计</span>
          </template>
          <el-row :gutter="20">
            <el-col :span="12">
              <h4>借用TOP设备</h4>
              <div ref="deviceRankChart" style="height: 300px"></div>
            </el-col>
            <el-col :span="12">
              <h4>类别占比</h4>
              <div ref="categoryPieChart" style="height: 300px"></div>
            </el-col>
          </el-row>
          <div style="margin-top: 20px">
            <h4>月度借用趋势</h4>
            <div ref="monthlyTrendChart" style="height: 300px"></div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <!-- 学生活跃度 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>学生活跃度</span>
          </template>
          <div ref="studentActivityChart" style="height: 300px"></div>
        </el-card>
      </el-col>

      <!-- 违规统计 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>违规统计</span>
          </template>
          <div ref="violationChart" style="height: 300px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row style="margin-top: 20px">
      <el-col :span="24">
        <el-card>
          <template #header>
            <span>生成报表</span>
          </template>
          <el-form :inline="true" :model="reportForm">
            <el-form-item label="报表类型">
              <el-select v-model="reportForm.reportType" placeholder="请选择">
                <el-option label="月报" value="monthly" />
                <el-option label="学期报" value="semester" />
                <el-option label="年报" value="yearly" />
              </el-select>
            </el-form-item>
            <el-form-item label="导出格式">
              <el-select v-model="reportForm.format" placeholder="请选择">
                <el-option label="Excel" value="excel" />
                <el-option label="PDF" value="pdf" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="generateReport">生成报表</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { teacherApi } from '../../api/teacher'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'

const statisticsData = ref({})
const deviceRankChart = ref(null)
const categoryPieChart = ref(null)
const monthlyTrendChart = ref(null)
const studentActivityChart = ref(null)
const violationChart = ref(null)

const reportForm = reactive({
  reportType: 'monthly',
  format: 'excel'
})

const loadStatistics = async () => {
  try {
    statisticsData.value = await teacherApi.getStatistics({})
    await nextTick()
    initCharts()
  } catch (error) {
    ElMessage.error('加载统计数据失败')
  }
}

const initCharts = () => {
  const data = statisticsData.value
  
  // 设备排行图表
  if (deviceRankChart.value && data.deviceStats?.rankings) {
    const chart = echarts.init(deviceRankChart.value)
    chart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: {
        type: 'category',
        data: data.deviceStats.rankings.map(item => item.name)
      },
      yAxis: { type: 'value' },
      series: [{
        data: data.deviceStats.rankings.map(item => item.count),
        type: 'bar',
        itemStyle: { color: '#409EFF' }
      }]
    })
  }

  // 类别占比饼图
  if (categoryPieChart.value && data.deviceStats?.categoryRatio) {
    const chart = echarts.init(categoryPieChart.value)
    const ratio = data.deviceStats.categoryRatio
    chart.setOption({
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie',
        radius: '50%',
        data: [
          { value: ratio.bio * 100, name: '生物设备' },
          { value: ratio.chem * 100, name: '化学设备' }
        ]
      }]
    })
  }

  // 月度趋势
  if (monthlyTrendChart.value && data.deviceStats?.monthlyTrend) {
    const chart = echarts.init(monthlyTrendChart.value)
    chart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: {
        type: 'category',
        data: data.deviceStats.monthlyTrend.map(item => item.month)
      },
      yAxis: { type: 'value' },
      series: [{
        data: data.deviceStats.monthlyTrend.map(item => item.count),
        type: 'line',
        smooth: true,
        itemStyle: { color: '#67C23A' }
      }]
    })
  }

  // 学生活跃度
  if (studentActivityChart.value && data.studentStats?.topStudents) {
    const chart = echarts.init(studentActivityChart.value)
    chart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: {
        type: 'category',
        data: data.studentStats.topStudents.map(item => item.name)
      },
      yAxis: { type: 'value' },
      series: [{
        data: data.studentStats.topStudents.map(item => item.count),
        type: 'bar',
        itemStyle: { color: '#E6A23C' }
      }]
    })
  }

  // 违规统计
  if (violationChart.value && data.violationStats?.typeRatio) {
    const chart = echarts.init(violationChart.value)
    const ratio = data.violationStats.typeRatio
    chart.setOption({
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie',
        radius: '50%',
        data: [
          { value: ratio.overdue * 100, name: '超时' },
          { value: ratio.damage * 100, name: '损坏' },
          { value: ratio.other * 100, name: '其他' }
        ]
      }]
    })
  }
}

const generateReport = async () => {
  try {
    const res = await teacherApi.generateReport(reportForm)
    ElMessage.success('报表生成成功')
    // 可以下载文件
  } catch (error) {
    ElMessage.error('生成失败')
  }
}

onMounted(() => {
  loadStatistics()
})
</script>

<style scoped>
.statistics-page {
  max-width: 1400px;
  margin: 0 auto;
}

h4 {
  margin: 0 0 15px 0;
  color: #333;
}
</style>
