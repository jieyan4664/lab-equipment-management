<template>
  <div class="announcements-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>公告管理</span>
          <el-button type="primary" @click="showAddDialog">发布公告</el-button>
        </div>
      </template>

      <!-- 公告列表 -->
      <el-table :data="announcementList" border v-loading="loading">
        <el-table-column prop="title" label="标题" width="200" />
        <el-table-column label="发布范围" width="120">
          <template #default="{ row }">
            {{ getTargetTypeText(row.targetType) }}
          </template>
        </el-table-column>
        <el-table-column prop="isPinned" label="置顶" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.isPinned" type="danger">是</el-tag>
            <span v-else>否</span>
          </template>
        </el-table-column>
        <el-table-column prop="publishTime" label="发布时间" width="180" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '正常' : '已删除' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="handleView(row)">查看</el-button>
            <el-button text type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="announcementList.length === 0 && !loading" description="暂无公告" />
    </el-card>

    <!-- 发布公告对话框 -->
    <el-dialog v-model="dialogVisible" title="发布公告" width="700px">
      <el-form :model="announcementForm" label-width="100px">
        <el-form-item label="标题" required>
          <el-input v-model="announcementForm.title" />
        </el-form-item>
        <el-form-item label="内容" required>
          <el-input v-model="announcementForm.content" type="textarea" :rows="5" />
        </el-form-item>
        <el-form-item label="发布范围" required>
          <el-select v-model="announcementForm.targetType" placeholder="请选择">
            <el-option label="全部" value="all" />
            <el-option label="生物实验室" value="bio" />
            <el-option label="化学实验室" value="chem" />
          </el-select>
        </el-form-item>
        <el-form-item label="置顶">
          <el-switch v-model="announcementForm.isPinned" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAnnouncement">发布</el-button>
      </template>
    </el-dialog>

    <!-- 查看公告对话框 -->
    <el-dialog v-model="viewDialogVisible" title="查看公告" width="700px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="标题">{{ currentAnnouncement.title }}</el-descriptions-item>
        <el-descriptions-item label="发布范围">
          {{ getTargetTypeText(currentAnnouncement.targetType) }}
        </el-descriptions-item>
        <el-descriptions-item label="置顶">
          <el-tag v-if="currentAnnouncement.isPinned" type="danger">是</el-tag>
          <span v-else>否</span>
        </el-descriptions-item>
        <el-descriptions-item label="发布时间">
          {{ currentAnnouncement.publishTime }}
        </el-descriptions-item>
        <el-descriptions-item label="内容">
          <div v-html="currentAnnouncement.content" class="announcement-content" />
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { teacherApi } from '../../api/teacher'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const announcementList = ref([])
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const currentAnnouncement = reactive({
  title: '',
  content: '',
  targetType: '',
  isPinned: false,
  publishTime: ''
})

const announcementForm = reactive({
  title: '',
  content: '',
  targetType: 'all',
  isPinned: false
})

// 获取发布范围文本
const getTargetTypeText = (type) => {
  const typeMap = {
    'all': '全部',
    'bio': '生物实验室',
    'chem': '化学实验室',
    'class': '指定班级',
    'student': '指定学生'
  }
  return typeMap[type] || type
}

const loadAnnouncements = async () => {
  loading.value = true
  try {
    const res = await teacherApi.getAnnouncements({})
    announcementList.value = res.list || []
  } catch (error) {
    ElMessage.error('加载公告列表失败')
  } finally {
    loading.value = false
  }
}

const showAddDialog = () => {
  dialogVisible.value = true
}

const submitAnnouncement = async () => {
  try {
    await teacherApi.createAnnouncement(announcementForm)
    ElMessage.success('发布成功')
    dialogVisible.value = false
    loadAnnouncements()
  } catch (error) {
    ElMessage.error('发布失败')
  }
}

const handleView = (row) => {
  currentAnnouncement.title = row.title
  currentAnnouncement.content = row.content
  currentAnnouncement.targetType = row.targetType
  currentAnnouncement.isPinned = row.isPinned
  currentAnnouncement.publishTime = row.publishTime
  viewDialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该公告吗?', '提示', { type: 'warning' })
    await teacherApi.deleteAnnouncement(row.id)
    ElMessage.success('删除成功')
    loadAnnouncements()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadAnnouncements()
})
</script>

<style scoped>
.announcements-page {
  max-width: 1400px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 公告内容样式 */
.announcement-content {
  line-height: 1.8;
  word-wrap: break-word;
}

.announcement-content :deep(p) {
  margin: 0.5em 0;
  line-height: 1.8;
}

.announcement-content :deep(ul),
.announcement-content :deep(ol) {
  margin: 0.5em 0;
  padding-left: 2em;
}

.announcement-content :deep(li) {
  margin: 0.3em 0;
  line-height: 1.6;
}

.announcement-content :deep(strong),
.announcement-content :deep(b) {
  font-weight: 600;
}

.announcement-content :deep(em),
.announcement-content :deep(i) {
  font-style: italic;
}
</style>
