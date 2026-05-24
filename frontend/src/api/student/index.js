import request from '../../utils/request'
import mock from '../../utils/mock'

const USE_MOCK = false  // 关闭Mock，使用真实API

export const studentApi = {
  // 获取首页数据
  getDashboard() {
    if (USE_MOCK) return mock.getStudentDashboard()
    return request.get('/student/dashboard')
  },

  // 获取设备分类列表
  getCategories() {
    return request.get('/student/categories')
  },

  // 获取设备列表
  getDevices(params) {
    if (USE_MOCK) return mock.getStudentDevices(params)
    return request.get('/student/devices', { params })
  },

  // 获取设备详情
  getDeviceDetail(id) {
    if (USE_MOCK) return mock.getDeviceDetail(id)
    return request.get(`/student/devices/${id}`)
  },

  // 收藏/取消收藏
  toggleFavorite(data) {
    if (USE_MOCK) return Promise.resolve({ success: true })
    return request.post('/student/favorites', data)
  },

  // 获取收藏列表
  getFavorites(params) {
    return request.get('/student/favorites', { params })
  },

  // 提交预约
  createReservation(data) {
    if (USE_MOCK) return mock.createReservation(data)
    return request.post('/student/reservations', data)
  },

  // 获取我的预约
  getReservations(params) {
    if (USE_MOCK) return mock.getStudentReservations(params)
    return request.get('/student/reservations', { params })
  },

  // 取消预约
  cancelReservation(id) {
    return request.put(`/student/reservations/${id}/cancel`)
  },

  // 申请延期
  extendReservation(id, data) {
    return request.post(`/student/reservations/${id}/extend`, data)
  },

  // 获取我的借用记录
  getBorrows(params) {
    if (USE_MOCK) return mock.getStudentBorrows(params)
    return request.get('/student/borrows', { params })
  },

  // 提交违规申诉
  appealViolation(id, data) {
    return request.post(`/student/violations/${id}/appeal`, data)
  },

  // 获取个人中心
  getProfile() {
    if (USE_MOCK) return mock.getStudentProfile()
    return request.get('/student/profile')
  },

  // 更新个人资料
  updateProfile(data) {
    return request.put('/student/profile', data)
  }
}
