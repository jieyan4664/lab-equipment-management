<template>
  <div class="profile-page">
    <el-row :gutter="20">
      <!-- 个人信息 -->
      <el-col :span="16">
        <el-card>
          <template #header>
            <span>个人信息</span>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="姓名">{{ profile.student?.name }}</el-descriptions-item>
            <el-descriptions-item label="学号">{{ profile.student?.studentNo }}</el-descriptions-item>
            <el-descriptions-item label="班级">{{ profile.student?.class }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">
              {{ profile.student?.phone }}
              <el-button text type="primary" @click="showEditDialog('phone')">编辑</el-button>
            </el-descriptions-item>
            <el-descriptions-item label="邮箱" :span="2">
              {{ profile.student?.email }}
              <el-button text type="primary" @click="showEditDialog('email')">编辑</el-button>
            </el-descriptions-item>
            <el-descriptions-item label="准入状态">
              <el-tag :type="profile.student?.accessStatus === 'normal' ? 'success' : 'danger'">
                {{ profile.student?.accessStatus === 'normal' ? '正常' : '禁用' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="准入有效期">
              {{ profile.student?.accessExpire }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 违规记录 -->
        <el-card style="margin-top: 20px">
          <template #header>
            <span>违规记录</span>
          </template>
          <el-table :data="profile.violations || []" border>
            <el-table-column prop="time" label="违规时间" width="180" />
            <el-table-column prop="deviceName" label="设备名称" width="150" />
            <el-table-column prop="type" label="违规类型">
              <template #default="{ row }">
                {{ getViolationTypeText(row.type) }}
              </template>
            </el-table-column>
            <el-table-column prop="punishment" label="处罚措施">
              <template #default="{ row }">
                {{ getPunishmentText(row.punishment) }}
              </template>
            </el-table-column>
            <el-table-column prop="teacherName" label="处理老师" />
          </el-table>
          <el-empty v-if="!profile.violations?.length" description="暂无违规记录" />
        </el-card>
      </el-col>

      <!-- 统计信息 -->
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>借用统计</span>
          </template>
          <div class="stats-list">
            <div class="stat-item">
              <div class="stat-label">累计借用次数</div>
              <div class="stat-value">15</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">当前借用数</div>
              <div class="stat-value">2</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">违规次数</div>
              <div class="stat-value" style="color: #F56C6C">1</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 编辑对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑信息" width="500px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item v-if="editField === 'phone'" label="联系电话">
          <el-input v-model="editForm.value" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item v-if="editField === 'email'" label="邮箱">
          <el-input v-model="editForm.value" placeholder="请输入邮箱" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { studentApi } from '../../api/student'
import { ElMessage } from 'element-plus'

const profile = ref({})
const editDialogVisible = ref(false)
const editField = ref('')
const editForm = reactive({
  value: ''
})

const loadProfile = async () => {
  try {
    profile.value = await studentApi.getProfile()
  } catch (error) {
    ElMessage.error('加载个人信息失败')
  }
}

const showEditDialog = (field) => {
  editField.value = field
  editForm.value = profile.value.student?.[field] || ''
  editDialogVisible.value = true
}

const submitEdit = async () => {
  try {
    await studentApi.updateProfile({
      [editField.value]: editForm.value
    })
    ElMessage.success('更新成功')
    editDialogVisible.value = false
    loadProfile()
  } catch (error) {
    ElMessage.error('更新失败')
  }
}

const getViolationTypeText = (type) => {
  const map = {
    overdue: '超时归还',
    damage: '设备损坏',
    other: '其他违规'
  }
  return map[type] || type
}

const getPunishmentText = (punishment) => {
  const map = {
    warning: '警告',
    ban: '禁止借用',
    compensation: '赔偿'
  }
  return map[punishment] || punishment
}

onMounted(() => {
  loadProfile()
})
</script>

<style scoped>
.profile-page {
  max-width: 1400px;
  margin: 0 auto;
}

.stats-list {
  padding: 20px 0;
}

.stat-item {
  text-align: center;
  padding: 20px 0;
  border-bottom: 1px solid #f0f0f0;
}

.stat-item:last-child {
  border-bottom: none;
}

.stat-label {
  font-size: 14px;
  color: #999;
  margin-bottom: 10px;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #409EFF;
}
</style>
