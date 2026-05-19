<template>
  <div class="teacher-devices">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>设备管理</span>
          <div>
            <el-button type="primary" @click="showAddDialog">添加设备</el-button>
            <el-button @click="handleBatchQRCode">生成二维码</el-button>
          </div>
        </div>
      </template>

      <!-- 筛选 -->
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="设备名称/编号" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable>
            <el-option label="可借用" value="available" />
            <el-option label="已借出" value="borrowed" />
            <el-option label="维修中" value="repair" />
            <el-option label="已报废" value="scrap" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadDevices">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 设备表格 -->
      <el-table :data="deviceList" border v-loading="loading" @selection-change="handleSelectionChange" width="100%">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="name" label="设备名称" width="150" />
        <el-table-column prop="code" label="设备编号" width="120" />
        <el-table-column prop="category" label="类别" width="100" />
        <el-table-column prop="model" label="型号" width="120" />
        <el-table-column prop="location" label="存放位置" width="150" />
        <el-table-column prop="purchaseDate" label="购入日期" width="120" />
        <el-table-column prop="warrantyDate" label="保修截止" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="showEditDialog(row)">编辑</el-button>
            <el-button text type="warning" @click="showStatusDialog(row)">状态</el-button>
            <el-button text type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-if="total > 0"
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="loadDevices"
        class="pagination"
      />
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑设备' : '添加设备'" width="700px">
      <el-form :model="deviceForm" :rules="rules" ref="formRef" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="设备名称" prop="name">
              <el-input v-model="deviceForm.name" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="设备编号" prop="code">
              <el-input v-model="deviceForm.code" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="分类" prop="categoryId">
              <el-select v-model="deviceForm.categoryId" placeholder="请选择">
                <el-option label="生物设备" :value="1" />
                <el-option label="化学设备" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="品牌">
              <el-input v-model="deviceForm.brand" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="型号">
              <el-input v-model="deviceForm.model" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="存放位置" prop="location">
              <el-input v-model="deviceForm.location" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="规格参数">
          <el-input v-model="deviceForm.spec" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="技术参数">
          <el-input v-model="deviceForm.technicalParams" type="textarea" :rows="2" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="购入日期">
              <el-date-picker v-model="deviceForm.purchaseDate" type="date" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="保修截止">
              <el-date-picker v-model="deviceForm.warrantyDate" type="date" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="使用说明">
          <el-input v-model="deviceForm.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">保存</el-button>
      </template>
    </el-dialog>

    <!-- 状态修改对话框 -->
    <el-dialog v-model="statusDialogVisible" title="修改设备状态" width="500px">
      <el-form :model="statusForm" label-width="100px">
        <el-form-item label="状态">
          <el-select v-model="statusForm.status" placeholder="请选择">
            <el-option label="维修中" value="repair" />
            <el-option label="已报废" value="scrap" />
          </el-select>
        </el-form-item>
        <el-form-item label="原因">
          <el-input v-model="statusForm.reason" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="statusDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitStatus">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { teacherApi } from '../../api/teacher'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const deviceList = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const statusDialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const selectedDevices = ref([])
const currentDevice = ref(null)

const searchForm = reactive({
  keyword: '',
  status: ''
})

const pagination = reactive({
  page: 1,
  size: 10
})

const deviceForm = reactive({
  name: '',
  code: '',
  categoryId: '',
  brand: '',
  model: '',
  spec: '',
  technicalParams: '',
  location: '',
  purchaseDate: '',
  warrantyDate: '',
  description: ''
})

const statusForm = reactive({
  status: '',
  reason: ''
})

const rules = {
  name: [{ required: true, message: '请输入设备名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入设备编号', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  location: [{ required: true, message: '请输入存放位置', trigger: 'blur' }]
}

const loadDevices = async () => {
  loading.value = true
  try {
    const params = {
      ...searchForm,
      page: pagination.page,
      size: pagination.size
    }
    const res = await teacherApi.getDevices(params)
    deviceList.value = res.list
    total.value = res.total
  } catch (error) {
    ElMessage.error('加载设备列表失败')
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.status = ''
  loadDevices()
}

const showAddDialog = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

const showEditDialog = (row) => {
  isEdit.value = true
  currentDevice.value = row
  Object.assign(deviceForm, row)
  dialogVisible.value = true
}

const showStatusDialog = (row) => {
  currentDevice.value = row
  statusForm.status = ''
  statusForm.reason = ''
  statusDialogVisible.value = true
}

const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        if (isEdit.value) {
          await teacherApi.updateDevice(currentDevice.value.id, deviceForm)
          ElMessage.success('更新成功')
        } else {
          await teacherApi.createDevice(deviceForm)
          ElMessage.success('添加成功')
        }
        dialogVisible.value = false
        loadDevices()
      } catch (error) {
        ElMessage.error('操作失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

const submitStatus = async () => {
  try {
    await teacherApi.updateDeviceStatus(currentDevice.value.id, statusForm)
    ElMessage.success('状态修改成功')
    statusDialogVisible.value = false
    loadDevices()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该设备吗?', '提示', { type: 'warning' })
    await teacherApi.deleteDevice(row.id)
    ElMessage.success('删除成功')
    loadDevices()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleSelectionChange = (selection) => {
  selectedDevices.value = selection
}

const handleBatchQRCode = async () => {
  if (selectedDevices.value.length === 0) {
    ElMessage.warning('请先选择设备')
    return
  }
  
  try {
    const ids = selectedDevices.value.map(d => d.id)
    const res = await teacherApi.generateQRCodes(ids)
    ElMessage.success('二维码生成成功')
    // 可以下载PDF
  } catch (error) {
    ElMessage.error('生成失败')
  }
}

const resetForm = () => {
  Object.assign(deviceForm, {
    name: '',
    code: '',
    categoryId: '',
    brand: '',
    model: '',
    spec: '',
    technicalParams: '',
    location: '',
    purchaseDate: '',
    warrantyDate: '',
    description: ''
  })
}

const getStatusType = (status) => {
  const map = {
    available: 'success',
    borrowed: 'warning',
    repair: 'danger',
    scrap: 'info'
  }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = {
    available: '可借用',
    borrowed: '已借出',
    repair: '维修中',
    scrap: '已报废'
  }
  return map[status] || status
}

onMounted(() => {
  loadDevices()
})
</script>

<style scoped>
.teacher-devices {
  max-width: 1400px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination {
  margin-top: 20px;
  justify-content: center;
}
</style>
