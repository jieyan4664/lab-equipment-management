<template>
  <div class="devices-page">
    <el-card>
      <!-- 筛选栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="类别">
          <el-select 
            v-model="searchForm.categoryId" 
            placeholder="请选择分类" 
            clearable 
            style="width: 180px"
            @change="handleSearch"
          >
            <el-option 
              v-for="category in categories" 
              :key="category.id" 
              :label="category.name" 
              :value="category.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select 
            v-model="searchForm.status" 
            placeholder="请选择状态" 
            clearable 
            style="width: 150px"
            @change="handleSearch"
          >
            <el-option label="可借用" value="available" />
            <el-option label="已借出" value="borrowed" />
            <el-option label="维修中" value="repair" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索设备名称/编号"
            clearable
            prefix-icon="Search"
            @keyup.enter="handleSearch"
            @input="handleKeywordInput"
            style="width: 250px"
          />
        </el-form-item>
        <el-form-item>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 统计信息 -->
      <div class="stats-bar" v-if="total > 0">
        <span>共找到 <strong>{{ total }}</strong> 个设备</span>
      </div>

      <!-- 设备列表 -->
      <div v-loading="loading" class="device-list-container">
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
                <div class="image-overlay">
                  <el-tag :type="getStatusType(device.status)" size="large">
                    {{ getStatusText(device.status) }}
                  </el-tag>
                </div>
              </div>
              <div class="device-info">
                <h3 class="device-name" :title="device.name">{{ device.name }}</h3>
                <p class="device-code">
                  <el-icon><Ticket /></el-icon>
                  {{ device.code }}
                </p>
                <p class="device-model" v-if="device.model">
                  <el-icon><Box /></el-icon>
                  {{ device.model }}
                </p>
                <p class="device-location">
                  <el-icon><Location /></el-icon>
                  {{ device.location }}
                </p>
                <div class="device-footer">
                  <el-button
                    text
                    :type="device.isFavorited ? 'warning' : 'default'"
                    @click.stop="toggleFavorite(device)"
                    class="favorite-btn"
                  >
                    <el-icon><Star /></el-icon>
                    {{ device.isFavorited ? '已收藏' : '收藏' }}
                  </el-button>
                  <el-button
                    type="primary"
                    size="small"
                    @click.stop="goToDetail(device.id)"
                    :disabled="device.status === 'repair'"
                  >
                    {{ device.status === 'available' ? '立即预约' : '查看详情' }}
                  </el-button>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <el-empty v-if="deviceList.length === 0 && !loading" description="暂无符合条件的设备">
          <el-button type="primary" @click="handleReset">清除筛选条件</el-button>
        </el-empty>
      </div>

      <!-- 分页 -->
      <el-pagination
        v-if="total > 0"
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="total"
        :page-sizes="[12, 24, 36, 48]"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="loadDevices"
        @size-change="handleSizeChange"
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
import { Star, Ticket, Box, Location, Search } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const deviceList = ref([])
const total = ref(0)
const categories = ref([])

const searchForm = reactive({
  categoryId: '',
  status: '',
  keyword: ''
})

const pagination = reactive({
  page: 1,
  size: 12
})

// 防抖定时器
let searchTimer = null

// 加载设备分类
const loadCategories = async () => {
  try {
    // 调用后端API获取分类
    const res = await studentApi.getCategories()
    // 过滤出二级分类（子分类），排除顶级分类（生物设备、化学设备）
    categories.value = res.filter(cat => cat.parentId !== 0)
  } catch (error) {
    console.error('加载分类失败:', error)
    ElMessage.error('加载分类失败')
  }
}

// 加载设备列表
const loadDevices = async () => {
  loading.value = true
  try {
    const params = {
      ...searchForm,
      page: pagination.page,
      size: pagination.size
    }
    const res = await studentApi.getDevices(params)
    deviceList.value = res.list || []
    total.value = res.total || 0
  } catch (error) {
    ElMessage.error('加载设备列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  pagination.page = 1
  loadDevices()
}

// 关键词输入防抖
const handleKeywordInput = () => {
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  searchTimer = setTimeout(() => {
    handleSearch()
  }, 500)
}

// 重置筛选
const handleReset = () => {
  searchForm.categoryId = ''
  searchForm.status = ''
  searchForm.keyword = ''
  handleSearch()
}

// 分页大小改变
const handleSizeChange = () => {
  pagination.page = 1
  loadDevices()
}

// 跳转到详情页
const goToDetail = (id) => {
  router.push(`/student/devices/${id}`)
}

// 切换收藏状态
const toggleFavorite = async (device) => {
  try {
    await studentApi.toggleFavorite({
      deviceId: device.id,
      action: device.isFavorited ? 'remove' : 'add'
    })
    device.isFavorited = !device.isFavorited
    ElMessage.success(device.isFavorited ? '收藏成功' : '已取消收藏')
  } catch (error) {
    ElMessage.error('操作失败')
    console.error(error)
  }
}

// 获取状态标签类型
const getStatusType = (status) => {
  const map = {
    available: 'success',
    borrowed: 'warning',
    repair: 'danger',
    scrap: 'info'
  }
  return map[status] || 'info'
}

// 获取状态文本
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
  loadCategories()
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
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}

.stats-bar {
  margin-bottom: 20px;
  padding: 10px 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
  font-size: 14px;
  color: #606266;
}

.stats-bar strong {
  color: #409eff;
  font-size: 16px;
}

.device-list-container {
  min-height: 400px;
}

.device-list {
  margin-bottom: 20px;
}

.device-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: all 0.3s;
  border-radius: 8px;
  overflow: hidden;
}

.device-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
}

.device-image {
  position: relative;
  width: 100%;
  height: 200px;
  overflow: hidden;
  background-color: #f5f7fa;
}

.device-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.device-card:hover .device-image img {
  transform: scale(1.05);
}

.image-overlay {
  position: absolute;
  top: 10px;
  right: 10px;
}

.device-info {
  padding: 15px;
}

.device-info h3 {
  margin: 0 0 12px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.device-info p {
  margin: 8px 0;
  font-size: 13px;
  color: #606266;
  display: flex;
  align-items: center;
  gap: 5px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.device-info p .el-icon {
  font-size: 14px;
  color: #909399;
}

.device-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 15px;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
}

.favorite-btn {
  padding: 5px 10px;
}

.pagination {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .search-form {
    display: block;
  }
  
  .search-form .el-form-item {
    display: block;
    margin-bottom: 15px;
  }
  
  .search-form .el-form-item__label {
    display: block;
    margin-bottom: 5px;
  }
  
  .search-form .el-select,
  .search-form .el-input {
    width: 100% !important;
  }
}
</style>
