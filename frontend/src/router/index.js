import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/common/Login.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/',
    redirect: '/student/dashboard'
  },
  // 学生端路由
  {
    path: '/student',
    component: () => import('../layouts/StudentLayout.vue'),
    meta: { requiresAuth: true, role: 'student' },
    children: [
      {
        path: 'dashboard',
        name: 'StudentDashboard',
        component: () => import('../views/student/Dashboard.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'devices',
        name: 'StudentDevices',
        component: () => import('../views/student/Devices.vue'),
        meta: { title: '设备查询' }
      },
      {
        path: 'devices/:id',
        name: 'DeviceDetail',
        component: () => import('../views/student/DeviceDetail.vue'),
        meta: { title: '设备详情' }
      },
      {
        path: 'reservations',
        name: 'StudentReservations',
        component: () => import('../views/student/Reservations.vue'),
        meta: { title: '我的预约' }
      },
      {
        path: 'borrows',
        name: 'StudentBorrows',
        component: () => import('../views/student/Borrows.vue'),
        meta: { title: '我的借用' }
      },
      {
        path: 'profile',
        name: 'StudentProfile',
        component: () => import('../views/student/Profile.vue'),
        meta: { title: '个人中心' }
      }
    ]
  },
  // 老师端路由
  {
    path: '/teacher',
    component: () => import('../layouts/TeacherLayout.vue'),
    meta: { requiresAuth: true, role: 'teacher' },
    children: [
      {
        path: 'dashboard',
        name: 'TeacherDashboard',
        component: () => import('../views/teacher/Dashboard.vue'),
        meta: { title: '管理仪表盘' }
      },
      {
        path: 'devices',
        name: 'TeacherDevices',
        component: () => import('../views/teacher/Devices.vue'),
        meta: { title: '设备管理' }
      },
      {
        path: 'reservations',
        name: 'TeacherReservations',
        component: () => import('../views/teacher/Reservations.vue'),
        meta: { title: '预约审核' }
      },
      {
        path: 'borrows',
        name: 'TeacherBorrows',
        component: () => import('../views/teacher/Borrows.vue'),
        meta: { title: '借用归还管理' }
      },
      {
        path: 'students',
        name: 'TeacherStudents',
        component: () => import('../views/teacher/Students.vue'),
        meta: { title: '学生管理' }
      },
      {
        path: 'repairs',
        name: 'TeacherRepairs',
        component: () => import('../views/teacher/Repairs.vue'),
        meta: { title: '维修报废管理' }
      },
      {
        path: 'announcements',
        name: 'TeacherAnnouncements',
        component: () => import('../views/teacher/Announcements.vue'),
        meta: { title: '公告管理' }
      },
      {
        path: 'statistics',
        name: 'TeacherStatistics',
        component: () => import('../views/teacher/Statistics.vue'),
        meta: { title: '数据统计' }
      },
      {
        path: 'settings',
        name: 'TeacherSettings',
        component: () => import('../views/teacher/Settings.vue'),
        meta: { title: '系统设置' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const userRole = localStorage.getItem('userRole')
  
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else if (to.meta.role && to.meta.role !== userRole) {
    next(userRole === 'student' ? '/student/dashboard' : '/teacher/dashboard')
  } else {
    next()
  }
})

export default router
