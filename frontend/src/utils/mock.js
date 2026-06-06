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

// Mock设备数据 - 更真实的设备信息
const mockDevices = [
  {
    id: 1,
    name: '光学显微镜',
    code: 'DEV-BIO-001',
    category: '显微镜',
    brand: '奥林巴斯',
    model: 'CX23',
    spec: '40x-1000x',
    technicalParams: 'LED光源，双目镜筒，无限远光学系统',
    location: 'A栋-201-1号柜',
    purchaseDate: '2024-03-15',
    status: 'available',
    thumbnail: '/images/devices/microscope_1.jpg',
    isFavorited: false
  },
  {
    id: 2,
    name: '电子显微镜',
    code: 'DEV-BIO-002',
    category: '显微镜',
    brand: '日立',
    model: 'SU3500',
    spec: '放大倍数：200000x',
    technicalParams: '扫描电镜，配备能谱仪',
    location: 'A栋-202-1号柜',
    purchaseDate: '2023-06-20',
    status: 'available',
    thumbnail: '/images/devices/emicroscope_1.jpg',
    isFavorited: true
  },
  {
    id: 3,
    name: '高速离心机',
    code: 'DEV-BIO-003',
    category: '离心机',
    brand: 'Eppendorf',
    model: '5430R',
    spec: '最高转速：30000rpm',
    technicalParams: '制冷型，最大容量：6×50ml',
    location: 'A栋-201-2号柜',
    purchaseDate: '2024-01-10',
    status: 'borrowed',
    thumbnail: '/images/devices/centrifuge_high_1.jpg',
    isFavorited: false
  },
  {
    id: 4,
    name: '低速离心机',
    code: 'DEV-BIO-004',
    category: '离心机',
    brand: '湘仪',
    model: 'TD5A',
    spec: '最高转速：5000rpm',
    technicalParams: '常温型，最大容量：6×500ml',
    location: 'A栋-201-3号柜',
    purchaseDate: '2023-09-05',
    status: 'available',
    thumbnail: '/images/devices/centrifuge_low_1.jpg',
    isFavorited: false
  },
  {
    id: 5,
    name: 'CO2培养箱',
    code: 'DEV-BIO-005',
    category: '培养箱',
    brand: 'Thermo',
    model: '3111',
    spec: '温度范围：室温+5~60℃',
    technicalParams: 'CO2浓度控制：0-20%，湿度控制',
    location: 'A栋-203-1号柜',
    purchaseDate: '2024-05-20',
    status: 'available',
    thumbnail: '/images/devices/incubator_co2_1.jpg',
    isFavorited: false
  },
  {
    id: 6,
    name: '恒温培养箱',
    code: 'DEV-BIO-006',
    category: '培养箱',
    brand: '上海一恒',
    model: 'DHP-9162',
    spec: '温度范围：室温+5~65℃',
    technicalParams: '容积：160L，微电脑控温',
    location: 'A栋-203-2号柜',
    purchaseDate: '2023-11-15',
    status: 'repair',
    thumbnail: '/images/devices/incubator_const_1.jpg',
    isFavorited: false
  },
  {
    id: 7,
    name: '紫外可见分光光度计',
    code: 'DEV-BIO-007',
    category: '分光光度计',
    brand: '岛津',
    model: 'UV-2600',
    spec: '波长范围：190-1100nm',
    technicalParams: '双光束，自动波长扫描',
    location: 'A栋-204-1号柜',
    purchaseDate: '2024-02-28',
    status: 'available',
    thumbnail: '/images/devices/spectrophotometer_1.jpg',
    isFavorited: false
  },
  {
    id: 8,
    name: '高压反应釜',
    code: 'DEV-CHEM-001',
    category: '反应釜',
    brand: 'Parr',
    model: '4560',
    spec: '容积：1L，压力：20MPa',
    technicalParams: '不锈钢材质，带搅拌装置',
    location: 'B栋-301-1号柜',
    purchaseDate: '2023-08-10',
    status: 'available',
    thumbnail: '/images/devices/reactor_high_1.jpg',
    isFavorited: false
  },
  {
    id: 9,
    name: '微型反应釜',
    code: 'DEV-CHEM-002',
    category: '反应釜',
    brand: '天津欧诺',
    model: 'KCF-0.1',
    spec: '容积：0.1L，压力：10MPa',
    technicalParams: '小型实验用，聚四氟乙烯内衬',
    location: 'B栋-301-2号柜',
    purchaseDate: '2024-04-15',
    status: 'available',
    thumbnail: '/images/devices/reactor_micro_1.jpg',
    isFavorited: false
  },
  {
    id: 10,
    name: '自动电位滴定仪',
    code: 'DEV-CHEM-003',
    category: '滴定仪',
    brand: '梅特勒',
    model: 'T50',
    spec: '精度：0.001ml',
    technicalParams: '自动终点判断，多通道',
    location: 'B栋-302-1号柜',
    purchaseDate: '2024-06-01',
    status: 'borrowed',
    thumbnail: '/images/devices/titrator_auto_1.jpg',
    isFavorited: false
  },
  {
    id: 11,
    name: '手动滴定仪',
    code: 'DEV-CHEM-004',
    category: '滴定仪',
    brand: '上海雷磁',
    model: 'ZDJ-4A',
    spec: '精度：0.01ml',
    technicalParams: '数字显示，手动控制',
    location: 'B栋-302-2号柜',
    purchaseDate: '2023-10-20',
    status: 'available',
    thumbnail: '/images/devices/titrator_manual_1.jpg',
    isFavorited: false
  },
  {
    id: 12,
    name: '气相色谱仪',
    code: 'DEV-CHEM-005',
    category: '色谱仪',
    brand: '安捷伦',
    model: '7890B',
    spec: '检测器：FID/TCD/ECD',
    technicalParams: '毛细管柱，自动进样',
    location: 'B栋-303-1号柜',
    purchaseDate: '2023-07-15',
    status: 'available',
    thumbnail: '/images/devices/gc_1.jpg',
    isFavorited: false
  },
  {
    id: 13,
    name: '液相色谱仪',
    code: 'DEV-CHEM-006',
    category: '色谱仪',
    brand: '沃特世',
    model: 'e2695',
    spec: '检测器：UV/FLD',
    technicalParams: '二元梯度泵，自动进样器',
    location: 'B栋-303-2号柜',
    purchaseDate: '2024-03-10',
    status: 'available',
    thumbnail: '/images/devices/hplc_1.jpg',
    isFavorited: false
  },
  {
    id: 14,
    name: '精密pH计',
    code: 'DEV-CHEM-007',
    category: 'pH计',
    brand: '梅特勒',
    model: 'FE28',
    spec: '精度：±0.01pH',
    technicalParams: '自动温度补偿，校准提醒',
    location: 'B栋-304-1号柜',
    purchaseDate: '2024-01-25',
    status: 'available',
    thumbnail: '/images/devices/ph_meter_precision_1.jpg',
    isFavorited: false
  },
  {
    id: 15,
    name: '便携式pH计',
    code: 'DEV-CHEM-008',
    category: 'pH计',
    brand: '哈希',
    model: 'HQ11d',
    spec: '精度：±0.1pH',
    technicalParams: '电池供电，防水设计',
    location: 'B栋-304-2号柜',
    purchaseDate: '2023-12-05',
    status: 'available',
    thumbnail: '/images/devices/ph_meter_portable_1.jpg',
    isFavorited: false
  }
]

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
    
    // 关键词搜索
    if (params.keyword) {
      const keyword = params.keyword.toLowerCase()
      filtered = filtered.filter(d => 
        d.name.toLowerCase().includes(keyword) || 
        d.code.toLowerCase().includes(keyword) ||
        d.brand.toLowerCase().includes(keyword) ||
        d.model.toLowerCase().includes(keyword)
      )
    }
    
    // 状态筛选
    if (params.status) {
      filtered = filtered.filter(d => d.status === params.status)
    }
    
    // 分类筛选（模拟）
    if (params.categoryId) {
      // 这里简化处理，实际应该根据category_id过滤
      filtered = filtered.filter(d => {
        const categoryMap = {
          '1': '显微镜',
          '2': '离心机',
          '3': '培养箱',
          '4': '分光光度计',
          '5': '反应釜',
          '6': '滴定仪',
          '7': '色谱仪',
          '8': 'pH计'
        }
        return d.category === categoryMap[params.categoryId]
      })
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
    
    // 筛选数据
    let filteredDevices = mockDevices
    
    // 关键词筛选（设备名称或编号）
    if (params.keyword && params.keyword.trim()) {
      const keyword = params.keyword.toLowerCase()
      filteredDevices = filteredDevices.filter(device => 
        device.name.toLowerCase().includes(keyword) || 
        device.code.toLowerCase().includes(keyword)
      )
    }
    
    // 状态筛选
    if (params.status && params.status.trim()) {
      filteredDevices = filteredDevices.filter(device => 
        device.status === params.status
      )
    }
    
    return {
      total: filteredDevices.length,
      list: filteredDevices.slice((page - 1) * size, page * size)
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
