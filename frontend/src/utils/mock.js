// Mock数据 - 模拟后端API响应

// 模拟延迟
const delay = (ms) => new Promise(resolve => setTimeout(resolve, ms))

// Mock用户数据
const mockUsers = {
  student: {
    id: 1,
    name: '张三',
    account: '20240001',
    role: 'student',
    labType: 'bio'
  },
  teacher: {
    id: 1,
    name: '李老师',
    account: 'T001',
    role: 'teacher'
  }
}

// Mock设备数据
const mockDevices = Array.from({ length: 50 }, (_, i) => ({
  id: i + 1,
  name: ['显微镜', '离心机', '分光光度计', 'pH计', '电子天平'][i % 5] + (i + 1),
  code: `DEV-${String(i + 1).padStart(3, '0')}`,
  category: i % 2 === 0 ? '生物设备' : '化学设备',
  brand: ['奥林巴斯', '赛默飞', '梅特勒', '哈希'][i % 4],
  model: `Model-${i + 1}`,
  spec: '规格参数示例',
  technicalParams: '技术参数示例',
  location: `A栋-${200 + i}- ${i + 1}号柜`,
  purchaseDate: '2024-03-15',
  status: ['available', 'borrowed', 'repair'][i % 3],
  thumbnail: '/images/device/default.jpg'
}))

// Mock预约数据
const mockReservations = Array.from({ length: 20 }, (_, i) => ({
  id: i + 1,
  studentName: '张三',
  studentNo: '20240001',
  deviceName: mockDevices[i % mockDevices.length].name,
  deviceCode: mockDevices[i % mockDevices.length].code,
  startTime: '2026-01-20 08:00:00',
  endTime: '2026-01-20 12:00:00',
  purpose: '细胞观察实验',
  status: ['pending', 'approved', 'rejected', 'cancelled'][i % 4],
  reason: i % 4 === 2 ? '设备维护中' : null,
  waitingHours: i * 2,
  createdAt: '2026-01-15 10:30:00'
}))

// Mock借用记录
const mockBorrows = Array.from({ length: 15 }, (_, i) => ({
  id: i + 1,
  deviceName: mockDevices[i % mockDevices.length].name,
  deviceCode: mockDevices[i % mockDevices.length].code,
  studentName: '张三',
  studentNo: '20240001',
  borrowTime: '2026-01-10 09:00:00',
  dueTime: '2026-01-13 09:00:00',
  returnTime: i < 5 ? '2026-01-12 15:00:00' : null,
  remainingDays: i < 5 ? 0 : 2,
  isOverdue: i >= 10,
  overdueDays: i >= 10 ? 2 : 0,
  status: i < 5 ? 'returned' : 'borrowed',
  returnCode: `RET-001-${i + 1}`,
  equipmentCondition: ['good', 'worn', 'damaged'][i % 3]
}))

export default {
  // 登录
  async login(data) {
    await delay(500)
    if (data.account === '20240001' && data.password === '123456') {
      return {
        token: 'mock-token-student-' + Date.now(),
        userId: 1,
        name: '张三',
        role: 'student'
      }
    } else if (data.account === 'T001' && data.password === '123456') {
      return {
        token: 'mock-token-teacher-' + Date.now(),
        userId: 1,
        name: '李老师',
        role: 'teacher'
      }
    }
    throw new Error('账号或密码错误')
  },

  // 获取当前用户信息
  async getCurrentUser() {
    await delay(300)
    const role = localStorage.getItem('userRole') || 'student'
    return mockUsers[role]
  },

  // 学生端 - 获取首页数据
  async getStudentDashboard() {
    await delay(300)
    return {
      student: {
        id: 1,
        name: '张三',
        studentNo: '20240001',
        labType: 'bio',
        accessExpire: '2026-12-31'
      },
      stats: {
        currentBorrowCount: 2,
        pendingReservationCount: 1
      },
      announcements: [
        {
          id: 1,
          title: '实验室开放时间调整通知',
          publishTime: '2026-01-10 09:00:00',
          isRead: false
        },
        {
          id: 2,
          title: '新设备入库通知',
          publishTime: '2026-01-08 14:00:00',
          isRead: true
        }
      ]
    }
  },

  // 学生端 - 获取设备列表
  async getStudentDevices(params) {
    await delay(300)
    let filtered = [...mockDevices]
    
    if (params.keyword) {
      filtered = filtered.filter(d => 
        d.name.includes(params.keyword) || d.code.includes(params.keyword)
      )
    }
    
    if (params.status) {
      filtered = filtered.filter(d => d.status === params.status)
    }
    
    const page = params.page || 1
    const size = params.size || 12
    const start = (page - 1) * size
    const end = start + size
    
    return {
      total: filtered.length,
      list: filtered.slice(start, end)
    }
  },

  // 学生端 - 获取设备详情
  async getDeviceDetail(id) {
    await delay(300)
    const device = mockDevices.find(d => d.id === parseInt(id))
    if (!device) throw new Error('设备不存在')
    
    return {
      ...device,
      images: ['/images/device/1_1.jpg', '/images/device/1_2.jpg'],
      availableSlots: [
        {
          date: '2026-01-16',
          slots: [
            { start: '08:00', end: '10:00', status: 'available' },
            { start: '10:00', end: '12:00', status: 'available' }
          ]
        }
      ],
      comments: [
        {
          userName: '李四',
          rating: 5,
          content: '设备很好用',
          createTime: '2026-01-10 14:30:00',
          likeCount: 3
        }
      ]
    }
  },

  // 学生端 - 提交预约
  async createReservation(data) {
    await delay(500)
    return {
      reservationId: Math.floor(Math.random() * 1000)
    }
  },

  // 学生端 - 获取我的预约
  async getStudentReservations(params) {
    await delay(300)
    const page = params.page || 1
    const size = params.size || 10
    
    return {
      total: mockReservations.length,
      list: mockReservations.slice((page - 1) * size, page * size)
    }
  },

  // 学生端 - 获取我的借用记录
  async getStudentBorrows(params) {
    await delay(300)
    const page = params.page || 1
    const size = params.size || 10
    
    return {
      total: mockBorrows.length,
      list: mockBorrows.slice((page - 1) * size, page * size)
    }
  },

  // 学生端 - 获取个人中心
  async getStudentProfile() {
    await delay(300)
    return {
      student: {
        name: '张三',
        studentNo: '20240001',
        class: '生物技术1班',
        phone: '13800138000',
        email: 'zhangsan@example.com',
        accessStatus: 'normal',
        accessExpire: '2026-12-31'
      },
      violations: [
        {
          id: 1,
          time: '2025-12-20',
          deviceName: '离心机',
          type: 'overdue',
          punishment: 'warning',
          teacherName: '李老师'
        }
      ]
    }
  },

  // 老师端 - 获取仪表盘数据
  async getTeacherDashboard() {
    await delay(300)
    return {
      stats: {
        deviceCount: 156,
        availableCount: 120,
        repairCount: 5,
        todayReservationCount: 8,
        pendingAuditCount: 3,
        borrowedCount: 25,
        overdueCount: 2,
        activeStudentCount: 45,
        violationStudentCount: 8
      },
      todos: [
        {
          id: 1,
          type: 'overdue',
          deviceName: '显微镜',
          studentName: '张三',
          time: '2天',
          priority: 'high'
        }
      ],
      charts: {
        topDevices: [
          { name: '显微镜', count: 45 },
          { name: '离心机', count: 32 }
        ],
        monthlyTrend: [
          { month: '2025-12', count: 120 },
          { month: '2026-01', count: 85 }
        ]
      }
    }
  },

  // 老师端 - 获取设备列表
  async getTeacherDevices(params) {
    await delay(300)
    const page = params.page || 1
    const size = params.size || 10
    
    return {
      total: mockDevices.length,
      list: mockDevices.slice((page - 1) * size, page * size)
    }
  },

  // 老师端 - 获取预约列表
  async getTeacherReservations(params) {
    await delay(300)
    const page = params.page || 1
    const size = params.size || 10
    
    return {
      total: mockReservations.length,
      list: mockReservations.slice((page - 1) * size, page * size)
    }
  },

  // 老师端 - 审核预约
  async auditReservation(id, data) {
    await delay(500)
    return { success: true }
  },

  // 老师端 - 借用登记
  async createBorrow(data) {
    await delay(500)
    return {
      borrowId: Math.floor(Math.random() * 1000),
      returnCode: `RET-001-${Math.floor(Math.random() * 1000)}`
    }
  },

  // 老师端 - 归还登记
  async returnBorrow(data) {
    await delay(500)
    return { success: true }
  },

  // 老师端 - 获取当前借用列表
  async getCurrentBorrows(params) {
    await delay(300)
    return {
      list: mockBorrows.filter(b => b.status === 'borrowed')
    }
  },

  // 老师端 - 获取学生列表
  async getStudents(params) {
    await delay(300)
    const students = Array.from({ length: 50 }, (_, i) => ({
      id: i + 1,
      name: `学生${i + 1}`,
      studentNo: `2024${String(i + 1).padStart(4, '0')}`,
      class: '生物技术1班',
      phone: '13800138000',
      accessStatus: 'normal',
      currentBorrowCount: i % 3,
      totalBorrowCount: i * 2,
      violationCount: i % 5 === 0 ? 1 : 0
    }))
    
    const page = params.page || 1
    const size = params.size || 10
    
    return {
      total: students.length,
      list: students.slice((page - 1) * size, page * size)
    }
  },

  // 老师端 - 获取统计数据
  async getStatistics(params) {
    await delay(300)
    return {
      deviceStats: {
        rankings: [
          { name: '显微镜', count: 45 },
          { name: '离心机', count: 32 }
        ],
        categoryRatio: { bio: 0.6, chem: 0.4 },
        monthlyTrend: [
          { month: '2026-01', count: 85 },
          { month: '2026-02', count: 92 }
        ]
      },
      studentStats: {
        topStudents: [
          { name: '张三', count: 25 },
          { name: '李四', count: 18 }
        ],
        classActivity: [
          { class: '生物技术1班', avgCount: 3.2 }
        ]
      },
      violationStats: {
        typeRatio: { overdue: 0.7, damage: 0.2, other: 0.1 },
        monthlyTrend: [
          { month: '2026-01', count: 5 }
        ]
      }
    }
  },

  // 获取未读消息数量
  async getUnreadCount() {
    await delay(200)
    return { count: 3 }
  },

  // 获取消息列表
  async getNotifications(params) {
    await delay(300)
    const notifications = Array.from({ length: 20 }, (_, i) => ({
      id: i + 1,
      title: '预约审核结果',
      content: '您的显微镜预约已通过审核',
      type: 'reservation',
      isRead: i % 2 === 0,
      link: '/reservation/123',
      createdAt: '2026-01-15 10:30:00'
    }))
    
    const page = params.page || 1
    const size = params.size || 20
    
    return {
      total: notifications.length,
      list: notifications.slice((page - 1) * size, page * size)
    }
  }
}
