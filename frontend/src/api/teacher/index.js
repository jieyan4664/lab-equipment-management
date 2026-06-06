import request from '../../utils/request'
import mock from '../../utils/mock'

const USE_MOCK = true

export const teacherApi = {
  // 获取仪表盘数据
  getDashboard() {
    if (USE_MOCK) return mock.getTeacherDashboard()
    return request.get('/teacher/dashboard')
  },

  // 获取设备列表
  getDevices(params) {
    return request.get('/teacher/devices', { params })
  },

  // 添加设备
  createDevice(data) {
    return request.post('/teacher/devices', data)
  },

  // 编辑设备
  updateDevice(id, data) {
    return request.put(`/teacher/devices/${id}`, data)
  },

  // 删除设备
  deleteDevice(id) {
    return request.delete(`/teacher/devices/${id}`)
  },

  // 修改设备状态
  updateDeviceStatus(id, data) {
    return request.put(`/teacher/devices/${id}/status`, data)
  },

  // 生成二维码
  generateQRCodes(deviceIds) {
    return request.post('/teacher/devices/qr-codes', { deviceIds })
  },

  // 获取预约列表
  getReservations(params) {
    return request.get('/teacher/reservations', { params })
  },

  // 审核预约
  auditReservation(id, data) {
    return request.put(`/teacher/reservations/${id}/audit`, data)
  },

  // 批量审核
  batchAudit(data) {
    return request.post('/teacher/reservations/batch-audit', data)
  },

  // 借用登记
  createBorrow(data) {
    return request.post('/teacher/borrows', data)
  },

  // 归还登记
  returnBorrow(data) {
    return request.post('/teacher/borrows/return', data)
  },

  // 获取当前借用列表
  getCurrentBorrows(params) {
    return request.get('/teacher/borrows/current', { params })
  },

  // 催还通知
  remindReturn(id) {
    return request.post(`/teacher/borrows/${id}/remind`)
  },

  // 获取学生列表
  getStudents(params) {
    return request.get('/teacher/students', { params })
  },

  // 禁用/启用学生权限
  updateStudentAccess(id, data) {
    return request.put(`/teacher/students/${id}/access`, data)
  },

  // 添加违规记录
  createViolation(data) {
    return request.post('/teacher/violations', data)
  },

  // 获取维修列表
  getRepairs(params) {
    return request.get('/teacher/repairs', { params })
  },

  // 登记维修
  createRepair(data) {
    return request.post('/teacher/repairs', data)
  },

  // 发布公告
  createAnnouncement(data) {
    return request.post('/teacher/announcements', data)
  },

  // 获取公告列表
  getAnnouncements(params) {
    return request.get('/teacher/announcements', { params })
  },

  // 删除公告
  deleteAnnouncement(id) {
    return request.delete(`/teacher/announcements/${id}`)
  },

  // 获取统计数据
  getStatistics(params) {
    if (USE_MOCK) return mock.getStatistics(params)
    return request.get('/teacher/statistics', { params })
  },

  // 生成报表
  generateReport(data) {
    // 报表功能不使用mock，直接调用后端
    return request.post('/teacher/reports/generate', data)
  },

  // 获取系统设置
  getSettings() {
    return request.get('/teacher/settings')
  },

  // 更新系统设置
  updateSettings(data) {
    return request.put('/teacher/settings', data)
  }
}
