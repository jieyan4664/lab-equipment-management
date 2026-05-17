import request from '../../utils/request'
import mock from '../../utils/mock'

const USE_MOCK = true

export const notificationApi = {
  // 获取未读消息数量
  getUnreadCount() {
    if (USE_MOCK) return mock.getUnreadCount()
    return request.get('/notification/unread-count')
  },

  // 获取消息列表
  getNotifications(params) {
    if (USE_MOCK) return mock.getNotifications(params)
    return request.get('/notification/list', { params })
  },

  // 标记消息已读
  markAsRead(id) {
    return request.put(`/notification/read/${id}`)
  },

  // 全部标记已读
  markAllAsRead() {
    return request.put('/notification/read-all')
  }
}
