<template>
  <div class="announcement-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>公告列表</span>
          <el-radio-group v-model="filterType" size="small">
            <el-radio-button label="all">全部</el-radio-button>
            <el-radio-button label="unread">未读</el-radio-button>
            <el-radio-button label="read">已读</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <el-table :data="announcementList" border v-loading="loading">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="title" label="标题" min-width="300">
          <template #default="{ row }">
            <div class="title-cell">
              <el-icon v-if="!row.isRead" color="#409EFF" style="margin-right: 8px"><Bell /></el-icon>
              <el-link type="primary" @click="viewDetail(row.id)">{{ row.title }}</el-link>
              <el-tag v-if="row.isPinned" size="small" type="danger" style="margin-left: 8px">置顶</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="publishTime" label="发布时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.publishTime) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isRead ? 'info' : 'success'" size="small">
              {{ row.isRead ? '已读' : '未读' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="total > 0"
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 20px; justify-content: flex-end"
        @current-change="loadAnnouncements"
        @size-change="loadAnnouncements"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { studentApi } from '../../api/student'
import { ElMessage } from 'element-plus'
import { Bell } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const announcementList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const filterType = ref('all')

const loadAnnouncements = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      filterType: filterType.value
    }
    const res = await studentApi.getAnnouncements(params)
    announcementList.value = res.records || []
    total.value = res.total || 0
  } catch (error) {
    ElMessage.error('加载公告列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const viewDetail = (id) => {
  router.push(`/student/announcements/${id}`)
}

const formatTime = (time) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

// 页面首次加载
onMounted(() => {
  loadAnnouncements()
})

// 监听筛选类型变化
watch(filterType, () => {
  currentPage.value = 1  // 重置到第一页
  loadAnnouncements()
})

// 监听路由query参数变化（从详情页返回时会带上refresh参数）
watch(() => route.query.refresh, () => {
  if (route.query.refresh) {
    // 重新加载数据
    loadAnnouncements()
  }
})
</script>

<style scoped>
.announcement-page {
  max-width: 1200px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title-cell {
  display: flex;
  align-items: center;
}

.title-cell .el-link {
  font-weight: 500;
}
</style>
