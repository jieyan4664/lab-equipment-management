<template>
  <div class="repairs-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>维修报废管理</span>
          <el-button type="primary" @click="showRepairDialog">登记维修</el-button>
        </div>
      </template>

      <!-- 维修列表 -->
      <el-table :data="repairList" border v-loading="loading">
        <el-table-column prop="deviceName" label="设备名称" width="150" />
        <el-table-column prop="category" label="类别" width="100" />
        <el-table-column prop="location" label="存放位置" width="150" />
        <el-table-column prop="repairDate" label="维修日期" width="120" />
        <el-table-column prop="repairPerson" label="维修人员" width="120" />
        <el-table-column prop="cost" label="维修费用" width="100" />
        <el-table-column prop="result" label="维修结果" width="100">
          <template #default="{ row }">
            <el-tag :type="row.result === 'repaired' ? 'success' : 'danger'">
              {{ row.result === 'repaired' ? '已修复' : '无法修复' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="说明" show-overflow-tooltip />
      </el-table>

      <el-empty v-if="repairList.length === 0 && !loading" description="暂无维修记录" />
    </el-card>

    <!-- 维修登记对话框 -->
    <el-dialog v-model="repairDialogVisible" title="登记维修" width="600px">
      <el-form :model="repairForm" label-width="100px">
        <el-form-item label="设备" required>
          <el-select v-model="repairForm.deviceId" placeholder="请选择设备" style="width: 100%" v-loading="deviceLoading">
            <el-option 
              v-for="device in deviceList" 
              :key="device.id" 
              :label="`${device.name}-${device.code}`" 
              :value="device.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="维修日期" required>
          <el-date-picker v-model="repairForm.repairDate" type="date" style="width: 100%" />
        </el-form-item>
        <el-form-item label="维修人员" required>
          <el-input v-model="repairForm.repairPerson" />
        </el-form-item>
        <el-form-item label="维修费用">
          <el-input-number v-model="repairForm.cost" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="维修结果" required>
          <el-radio-group v-model="repairForm.result">
            <el-radio label="repaired">已修复</el-radio>
            <el-radio label="unrepairable">无法修复</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model="repairForm.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="repairDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRepair">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { teacherApi } from '../../api/teacher'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const repairList = ref([])
const repairDialogVisible = ref(false)
const deviceLoading = ref(false)
const deviceList = ref([])

const repairForm = reactive({
  deviceId: null,
  repairDate: '',
  repairPerson: '',
  cost: 0,
  result: 'repaired',
  description: ''
})

const loadRepairs = async () => {
  loading.value = true
  try {
    const res = await teacherApi.getRepairs({})
    repairList.value = res.list || []
  } catch (error) {
    ElMessage.error('加载维修列表失败')
  } finally {
    loading.value = false
  }
}

// 加载设备列表
const loadDevices = async () => {
  deviceLoading.value = true
  try {
    const res = await teacherApi.getDevices({ page: 1, size: 100 })
    deviceList.value = res.list || []
  } catch (error) {
    console.error('加载设备列表失败:', error)
    ElMessage.error('加载设备列表失败')
  } finally {
    deviceLoading.value = false
  }
}

const showRepairDialog = () => {
  // 如果设备列表为空，先加载
  if (deviceList.value.length === 0) {
    loadDevices()
  }
  repairDialogVisible.value = true
}

const submitRepair = async () => {
  // 表单验证
  if (!repairForm.deviceId) {
    ElMessage.warning('请选择设备')
    return
  }
  if (!repairForm.repairDate) {
    ElMessage.warning('请选择维修日期')
    return
  }
  if (!repairForm.repairPerson) {
    ElMessage.warning('请输入维修人员')
    return
  }
  
  try {
    // 格式化日期为字符串
    const submitData = {
      ...repairForm,
      repairDate: formatDate(repairForm.repairDate)
    }
    
    await teacherApi.createRepair(submitData)
    ElMessage.success('登记成功')
    repairDialogVisible.value = false
    // 重置表单
    resetForm()
    loadRepairs()
  } catch (error) {
    console.error('登记维修失败:', error)
    ElMessage.error(error.message || '操作失败')
  }
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 重置表单
const resetForm = () => {
  repairForm.deviceId = null
  repairForm.repairDate = ''
  repairForm.repairPerson = ''
  repairForm.cost = 0
  repairForm.result = 'repaired'
  repairForm.description = ''
}

onMounted(() => {
  loadRepairs()
  loadDevices() // 页面加载时加载设备列表
})
</script>

<style scoped>
.repairs-page {
  max-width: 1400px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
