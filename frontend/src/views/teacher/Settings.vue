<template>
  <div class="settings-page">
    <el-tabs v-model="activeTab">
      <!-- 实验室信息 -->
      <el-tab-pane label="实验室信息" name="labInfo">
        <el-card>
          <el-form :model="settings.labInfo" label-width="120px">
            <el-form-item label="实验室名称">
              <el-input v-model="settings.labInfo.name" />
            </el-form-item>
            <el-form-item label="开放时间">
              <el-input v-model="settings.labInfo.openHours" type="textarea" :rows="2" />
            </el-form-item>
            <el-form-item label="规则说明">
              <el-input v-model="settings.labInfo.rules" type="textarea" :rows="5" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveSettings">保存</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <!-- 预约规则 -->
      <el-tab-pane label="预约规则" name="reservationRules">
        <el-card>
          <el-form :model="settings.reservationRules" label-width="150px">
            <el-form-item label="最大预约时长(天)">
              <el-input-number v-model="settings.reservationRules.maxDuration" :min="1" />
            </el-form-item>
            <el-form-item label="提前预约天数">
              <el-input-number v-model="settings.reservationRules.maxAdvanceDays" :min="1" />
            </el-form-item>
            <el-form-item label="取消提前时间(小时)">
              <el-input-number v-model="settings.reservationRules.cancelAdvanceHours" :min="1" />
            </el-form-item>
            <el-form-item label="最大借用数">
              <el-input-number v-model="settings.reservationRules.maxBorrowCount" :min="1" />
            </el-form-item>
            <el-form-item label="时段粒度(小时)">
              <el-input-number v-model="settings.reservationRules.slotGranularity" :min="1" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveSettings">保存</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <!-- 归还提醒 -->
      <el-tab-pane label="归还提醒" name="reminderSettings">
        <el-card>
          <el-form :model="settings.reminderSettings" label-width="150px">
            <el-form-item label="超时阈值(小时)">
              <el-input-number v-model="settings.reminderSettings.overdueThreshold" :min="1" />
            </el-form-item>
            <el-form-item label="催还方式">
              <el-checkbox-group v-model="settings.reminderSettings.remindMethods">
                <el-checkbox label="sms">短信</el-checkbox>
                <el-checkbox label="inapp">站内信</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
            <el-form-item label="催还间隔">
              <el-select v-model="settings.reminderSettings.remindInterval">
                <el-option label="每天" value="daily" />
                <el-option label="每半天" value="half_daily" />
              </el-select>
            </el-form-item>
            <el-form-item label="提前提醒(小时)">
              <el-input-number v-model="settings.reminderSettings.advanceRemindHours" :min="1" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveSettings">保存</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <!-- 角色权限 -->
      <el-tab-pane label="角色权限" name="rolePermissions">
        <el-card>
          <div v-for="(role, index) in settings.rolePermissions" :key="index" class="role-item">
            <el-form :model="role" label-width="120px">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="角色名称">
                    <el-input v-model="role.roleName" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="角色编码">
                    <el-input v-model="role.roleCode" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-form-item label="角色描述">
                <el-input v-model="role.description" type="textarea" :rows="2" />
              </el-form-item>
              <el-form-item label="权限模块">
                <el-checkbox-group v-model="role.permissions">
                  <el-checkbox label="dashboard">管理仪表盘</el-checkbox>
                  <el-checkbox label="devices">设备管理</el-checkbox>
                  <el-checkbox label="reservations">预约审核</el-checkbox>
                  <el-checkbox label="borrows">借用归还</el-checkbox>
                  <el-checkbox label="students">学生管理</el-checkbox>
                  <el-checkbox label="repairs">维修报废</el-checkbox>
                  <el-checkbox label="announcements">公告管理</el-checkbox>
                  <el-checkbox label="statistics">数据统计</el-checkbox>
                  <el-checkbox label="settings">系统设置</el-checkbox>
                </el-checkbox-group>
              </el-form-item>
              <el-form-item>
                <el-button type="danger" size="small" @click="deleteRole(index)" :disabled="settings.rolePermissions.length <= 1">删除</el-button>
              </el-form-item>
            </el-form>
            <el-divider v-if="index < settings.rolePermissions.length - 1" />
          </div>
          <el-button type="primary" @click="addRole">添加角色</el-button>
          <el-button type="primary" @click="saveSettings" style="margin-left: 10px">保存所有设置</el-button>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { teacherApi } from '../../api/teacher'
import { ElMessage } from 'element-plus'

const activeTab = ref('labInfo')

const settings = reactive({
  labInfo: {
    name: '',
    openHours: '',
    rules: ''
  },
  reservationRules: {
    maxDuration: 3,
    maxAdvanceDays: 7,
    cancelAdvanceHours: 24,
    maxBorrowCount: 3,
    slotGranularity: 2
  },
  reminderSettings: {
    overdueThreshold: 24,
    remindMethods: ['sms', 'inapp'],
    remindInterval: 'daily',
    advanceRemindHours: 2
  },
  rolePermissions: [
    {
      roleName: '普通老师',
      roleCode: 'teacher',
      description: '普通老师角色，拥有基本管理权限',
      permissions: ['dashboard', 'devices', 'reservations', 'borrows', 'students', 'repairs', 'announcements', 'statistics', 'settings']
    },
    {
      roleName: '管理员',
      roleCode: 'admin',
      description: '管理员角色，拥有全部管理权限',
      permissions: ['dashboard', 'devices', 'reservations', 'borrows', 'students', 'repairs', 'announcements', 'statistics', 'settings']
    }
  ]
})

const loadSettings = async () => {
  try {
    const res = await teacherApi.getSettings()
    Object.assign(settings, res)
  } catch (error) {
    ElMessage.error('加载设置失败')
  }
}

const saveSettings = async () => {
  try {
    const payload = {
      labInfo: settings.labInfo,
      reservationRules: settings.reservationRules,
      reminderSettings: settings.reminderSettings,
      rolePermissions: settings.rolePermissions
    }
    await teacherApi.updateSettings(payload)
    ElMessage.success('保存成功')
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const addRole = () => {
  settings.rolePermissions.push({
    roleName: '',
    roleCode: '',
    description: '',
    permissions: []
  })
}

const deleteRole = (index) => {
  settings.rolePermissions.splice(index, 1)
}

onMounted(() => {
  loadSettings()
})
</script>

<style scoped>
.settings-page {
  max-width: 800px;
  margin: 0 auto;
}
</style>