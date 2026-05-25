<template>
  <div class="announcement-detail-page">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <el-button text @click="goBack">
            <el-icon><ArrowLeft /></el-icon>
            返回
          </el-button>
          <el-tag v-if="announcement.isPinned" type="danger">置顶</el-tag>
        </div>
      </template>

      <div v-if="announcement.id" class="detail-content">
        <h1 class="title">{{ announcement.title }}</h1>
        
        <div class="meta-info">
          <span>发布时间：{{ formatTime(announcement.publishTime) }}</span>
          <span>发布人：{{ announcement.teacherName || '管理员' }}</span>
        </div>

        <el-divider />

        <div class="content" v-html="announcement.content"></div>

        <div v-if="announcement.attachments" class="attachments">
          <h3>附件：</h3>
          <el-tag
            v-for="(file, index) in parseAttachments(announcement.attachments)"
            :key="index"
            style="margin-right: 10px; margin-bottom: 10px"
          >
            <el-icon><Document /></el-icon>
            {{ file }}
          </el-tag>
        </div>
      </div>

      <el-empty v-else description="公告不存在" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { studentApi } from '../../api/student'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Document } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const announcement = ref({})

const loadAnnouncementDetail = async () => {
  loading.value = true
  try {
    const id = route.params.id
    announcement.value = await studentApi.getAnnouncementDetail(id)
    
    // 标记为已读
    if (!announcement.value.isRead) {
      await studentApi.markAnnouncementRead(id)
    }
  } catch (error) {
    ElMessage.error('加载公告详情失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  // 返回列表页并带上刷新标志
  router.push({
    path: '/student/announcements',
    query: { 
      refresh: Date.now() // 使用时间戳强制刷新
    }
  })
}

const formatTime = (time) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

const parseAttachments = (attachments) => {
  try {
    return JSON.parse(attachments)
  } catch {
    return []
  }
}

onMounted(() => {
  loadAnnouncementDetail()
})
</script>

<style scoped>
.announcement-detail-page {
  max-width: 900px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.detail-content {
  padding: 20px 0;
}

.title {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin: 0 0 15px 0;
  line-height: 1.5;
}

.meta-info {
  display: flex;
  gap: 20px;
  font-size: 14px;
  color: #999;
}

.content {
  font-size: 16px;
  line-height: 1.8;
  color: #333;
  min-height: 200px;
}

.content :deep(p) {
  margin: 10px 0;
}

.content :deep(img) {
  max-width: 100%;
  height: auto;
}

.attachments {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.attachments h3 {
  font-size: 16px;
  color: #333;
  margin-bottom: 15px;
}
</style>
