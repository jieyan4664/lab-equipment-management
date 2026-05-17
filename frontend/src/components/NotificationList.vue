<template>
  <div class="notification-list">
    <el-empty v-if="notifications.length === 0" description="暂无消息" />
    <div v-else>
      <div
        v-for="item in notifications"
        :key="item.id"
        class="notification-item"
        :class="{ 'unread': !item.isRead }"
        @click="handleClick(item)"
      >
        <div class="notification-header">
          <span class="notification-title">{{ item.title }}</span>
          <span class="notification-time">{{ formatTime(item.createdAt) }}</span>
        </div>
        <div class="notification-content">{{ item.content }}</div>
      </div>
      
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="loadNotifications"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { notificationApi } from '../api/common/notification'
import { useNotificationStore } from '../stores/modules/notification'
import dayjs from 'dayjs'

const notificationStore = useNotificationStore()
const notifications = ref([])
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

const loadNotifications = async () => {
  try {
    const res = await notificationApi.getNotifications({
      page: currentPage.value,
      size: pageSize.value
    })
    notifications.value = res.list
    total.value = res.total
  } catch (error) {
    console.error('加载消息失败:', error)
  }
}

const handleClick = async (item) => {
  if (!item.isRead) {
    await notificationApi.markAsRead(item.id)
    notificationStore.markAsRead(item.id)
  }
}

const formatTime = (time) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

onMounted(() => {
  loadNotifications()
})
</script>

<style scoped>
.notification-list {
  padding: 10px;
}

.notification-item {
  padding: 15px;
  border-bottom: 1px solid #eee;
  cursor: pointer;
  transition: background-color 0.3s;
}

.notification-item:hover {
  background-color: #f5f7fa;
}

.notification-item.unread {
  background-color: #ecf5ff;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.notification-title {
  font-weight: bold;
  color: #333;
}

.notification-time {
  font-size: 12px;
  color: #999;
}

.notification-content {
  font-size: 14px;
  color: #666;
}
</style>
