<template>
  <div class="devices-page">
    <el-card>
      <!-- 筛选栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="类别">
          <el-select v-model="searchForm.categoryId" placeholder="请选择" clearable style="width: 150px">
            <el-option label="生物设备" value="bio" />
            <el-option label="化学设备" value="chem" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 150px">
            <el-option label="可借用" value="available" />
            <el-option label="已借出" value="borrowed" />
            <el-option label="维修中" value="repair" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="设备名称/编号"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 设备列表 -->
      <el-row :gutter="20" class="device-list">
        <el-col
          v-for="device in deviceList"
          :key="device.id"
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
        >
          <el-card class="device-card" shadow="hover" @click="goToDetail(device.id)">
            <div class="device-image">
              <img :src="device.thumbnail || '/images/device/default.jpg'" alt="设备图片" />
            </div>
            <div class="device-info">
              <h3 class="device-name">{{ device.name }}</h3>
              <p class="device-code">编号: {{ device.code }}</p>
              <p class="device-model">型号: {{ device.model }}</p>
              <p class="device-location">位置: {{ device.location }}</p>
              <div class="device-footer">
                <el-tag :type="getStatusType(device.status)">
                  {{ getStatusText(device.status) }}
                </el-tag>
                <el-button
                  text
                  type="primary"
                  @click.stop="toggleFavorite(device)"
                >
                  <el-icon><Star /></el-icon>
                </el-button>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-empty v-if="deviceList.length === 0 && !loading" description="暂无设备" />

      <!-- 分页 -->
      <el-pagination
        v-if="total > 0"
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="total"
        :page-sizes="[12, 24, 36]"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="loadDevices"
        @size-change="loadDevices"
        class="pagination"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { studentApi } from '../../api/student'
import { ElMessage } from 'element-plus'
import { Star } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const deviceList = ref([])
const total = ref(0)

const searchForm = reactive({
  categoryId: '',
  status: '',
  keyword: ''
})

const pagination = reactive({
  page: 1,
  size: 12
})

const loadDevices = async () => {
  loading.value = true
  try {
    const params = {
      ...searchForm,
      page: pagination.page,
      size: pagination.size
    }
    const res = await studentApi.getDevices(params)
    deviceList.value = res.list
    total.value = res.total
  } catch (error) {
    ElMessage.error('加载设备列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadDevices()
}

const handleReset = () => {
  searchForm.categoryId = ''
  searchForm.status = ''
  searchForm.keyword = ''
  handleSearch()
}

const goToDetail = (id) => {
  router.push(`/student/devices/${id}`)
}

const toggleFavorite = async (device) => {
  try {
    await studentApi.toggleFavorite({
      deviceId: device.id,
      action: device.isFavorited ? 'remove' : 'add'
    })
    device.isFavorited = !device.isFavorited
    ElMessage.success(device.isFavorited ? '收藏成功' : '取消收藏')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const getStatusType = (status) => {
  const map = {
    available: 'success',
    borrowed: 'warning',
    repair: 'danger'
  }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = {
    available: '可借用',
    borrowed: '已借出',
    repair: '维修中'
  }
  return map[status] || status
}

onMounted(() => {
  loadDevices()
})
</script>

<style scoped>
.devices-page {
  max-width: 1400px;
  margin: 0 auto;
}

.search-form {
  margin-bottom: 20px;
}

.device-list {
  min-height: 400px;
}

.device-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.device-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.device-image {
  width: 100%;
  height: 180px;
  overflow: hidden;
  border-radius: 4px;
  margin-bottom: 15px;
}

.device-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.device-info h3 {
  margin: 0 0 10px 0;
  font-size: 16px;
  color: #333;
}

.device-info p {
  margin: 5px 0;
  font-size: 13px;
  color: #666;
}

.device-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}

.pagination {
  margin-top: 20px;
  justify-content: center;
}
</style>
