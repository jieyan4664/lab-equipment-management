import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useNotificationStore = defineStore('notification', () => {
  const unreadCount = ref(0)
  const notifications = ref([])
  
  function setUnreadCount(count) {
    unreadCount.value = count
  }
  
  function addNotification(notification) {
    notifications.value.unshift(notification)
    if (!notification.isRead) {
      unreadCount.value++
    }
  }
  
  function markAsRead(id) {
    const notification = notifications.value.find(n => n.id === id)
    if (notification && !notification.isRead) {
      notification.isRead = true
      unreadCount.value--
    }
  }
  
  function markAllAsRead() {
    notifications.value.forEach(n => {
      if (!n.isRead) {
        n.isRead = true
      }
    })
    unreadCount.value = 0
  }
  
  return {
    unreadCount,
    notifications,
    setUnreadCount,
    addNotification,
    markAsRead,
    markAllAsRead
  }
})
