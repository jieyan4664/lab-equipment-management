import request from '../../utils/request'
import mock from '../../utils/mock'

// 是否使用Mock数据
const USE_MOCK = true

export const authApi = {
  // 登录
  login(data) {
    if (USE_MOCK) return mock.login(data)
    return request.post('/auth/login', data)
  },

  // 获取验证码
  getCaptcha() {
    return request.get('/auth/captcha')
  },

  // 获取当前用户信息
  getCurrentUser() {
    if (USE_MOCK) return mock.getCurrentUser()
    return request.get('/auth/current')
  }
}
