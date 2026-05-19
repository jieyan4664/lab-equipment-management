<template>
  <el-container class="teacher-layout">
    <el-header class="header">
      <div class="header-content">
        <h1 class="logo">实验室设备管理系统 - 管理端</h1>
        <div class="header-right">
          <el-badge :value="notificationStore.unreadCount" :hidden="notificationStore.unreadCount === 0">
            <el-button text @click="showNotifications">
              <el-icon><Bell /></el-icon>
            </el-button>
          </el-badge>
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              {{ userStore.userName }}
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </el-header>
    
    <el-container>
      <el-aside width="200px" class="aside">
        <el-menu
          :default-active="activeMenu"
          router
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
        >
          <el-menu-item index="/teacher/dashboard">
            <el-icon><DataAnalysis /></el-icon>
            <span>管理仪表盘</span>
          </el-menu-item>
          <el-menu-item index="/teacher/devices">
            <el-icon><Monitor /></el-icon>
            <span>设备管理</span>
          </el-menu-item>
          <el-menu-item index="/teacher/reservations">
            <el-icon><Calendar /></el-icon>
            <span>预约审核</span>
          </el-menu-item>
          <el-menu-item index="/teacher/borrows">
            <el-icon><Box /></el-icon>
            <span>借用归还</span>
          </el-menu-item>
          <el-menu-item index="/teacher/students">
            <el-icon><User /></el-icon>
            <span>学生管理</span>
          </el-menu-item>
          <el-menu-item index="/teacher/repairs">
            <el-icon><Tools /></el-icon>
            <span>维修报废</span>
          </el-menu-item>
          <el-menu-item index="/teacher/announcements">
            <el-icon><Bell /></el-icon>
            <span>公告管理</span>
          </el-menu-item>
          <el-menu-item index="/teacher/statistics">
            <el-icon><TrendCharts /></el-icon>
            <span>数据统计</span>
          </el-menu-item>
          <el-menu-item index="/teacher/settings">
            <el-icon><Setting /></el-icon>
            <span>系统设置</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>

    <!-- 消息通知抽屉 -->
    <el-drawer v-model="notificationDrawer" title="消息通知" size="400px">
      <NotificationList />
    </el-drawer>
  </el-container>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../stores/modules/user'
import { useNotificationStore } from '../stores/modules/notification'
import NotificationList from '../components/NotificationList.vue'
import { Bell, ArrowDown, DataAnalysis, Monitor, Calendar, Box, User, Tools, TrendCharts, Setting } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const notificationStore = useNotificationStore()

const notificationDrawer = ref(false)
const activeMenu = computed(() => route.path)

const showNotifications = () => {
  notificationDrawer.value = true
}

const handleCommand = (command) => {
  if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.teacher-layout {
  height: 100vh;
}

.header {
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  padding: 0 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
}

.logo {
  font-size: 20px;
  color: #409EFF;
  margin: 0;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-info {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 5px;
}

.aside {
  background-color: #304156;
}

.main {
  background-color: #f0f2f5;
  padding: 20px;
  overflow: hidden;
  display: block;
}
</style>
