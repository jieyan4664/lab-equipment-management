# 学生端 - 设备查询页面功能说明

## 📋 功能概述

学生端"设备查询"页面提供了完整的设备浏览、筛选、搜索和收藏功能。

## ✨ 主要功能

### 1. 设备列表展示
- ✅ 卡片式布局，响应式设计（支持xs/sm/md/lg不同屏幕）
- ✅ 显示设备图片、名称、编号、型号、存放位置
- ✅ 状态标签（可借用/已借出/维修中/已报废）
- ✅ 悬停动画效果

### 2. 筛选功能
- ✅ **分类筛选**：按设备分类过滤（显微镜、离心机、培养箱等8个分类）
- ✅ **状态筛选**：按设备状态过滤（可借用、已借出、维修中）
- ✅ **关键词搜索**：支持设备名称、编号、品牌、型号搜索
- ✅ **实时搜索**：关键词输入防抖（500ms），自动触发搜索

### 3. 分页功能
- ✅ 支持每页显示12/24/36/48条
- ✅ 页码跳转
- ✅ 显示总记录数

### 4. 收藏功能
- ✅ 点击收藏按钮切换收藏状态
- ✅ 收藏状态视觉反馈（黄色星标+文字）
- ✅ 操作成功提示

### 5. 交互优化
- ✅ 点击卡片跳转到设备详情页
- ✅ 维修中设备禁用"立即预约"按钮
- ✅ 空状态提示，支持一键清除筛选
- ✅ 加载状态显示
- ✅ 统计信息展示（共找到X个设备）

## 🎨 UI特点

### 筛选栏
- 清晰的表单布局
- 下拉选择自动触发搜索
- 搜索框带图标前缀
- 底部分隔线

### 设备卡片
```
┌─────────────────────┐
│   设备图片           │
│   [状态标签]         │
├─────────────────────┤
│ 设备名称             │
│ 🎫 设备编号          │
│ 📦 型号              │
│ 📍 存放位置          │
├─────────────────────┤
│ ⭐ 收藏  [立即预约]  │
└─────────────────────┘
```

### 视觉效果
- 卡片悬停上浮 + 阴影加深
- 图片悬停放大效果
- 状态标签颜色区分
- 图标增强可读性

## 📊 数据结构

### 设备对象
```javascript
{
  id: 1,
  name: '光学显微镜',
  code: 'DEV-BIO-001',
  category: '显微镜',
  brand: '奥林巴斯',
  model: 'CX23',
  spec: '40x-1000x',
  technicalParams: 'LED光源，双目镜筒',
  location: 'A栋-201-1号柜',
  purchaseDate: '2024-03-15',
  status: 'available',  // available/borrowed/repair/scrap
  thumbnail: '/images/devices/microscope_1.jpg',
  isFavorited: false
}
```

### 分类对象
```javascript
{
  id: 1,
  name: '显微镜',
  parentId: 1
}
```

## 🔧 技术实现

### 组件结构
```
Devices.vue
├── 筛选表单 (el-form)
│   ├── 分类选择 (el-select)
│   ├── 状态选择 (el-select)
│   └── 关键词搜索 (el-input)
├── 统计栏
├── 设备列表 (el-row + el-col)
│   └── 设备卡片 (el-card)
├── 空状态 (el-empty)
└── 分页 (el-pagination)
```

### 关键逻辑

#### 1. 防抖搜索
```javascript
let searchTimer = null

const handleKeywordInput = () => {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    handleSearch()
  }, 500)
}
```

#### 2. 数据加载
```javascript
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
  } finally {
    loading.value = false
  }
}
```

#### 3. 收藏切换
```javascript
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
  }
}
```

## 📱 响应式设计

| 屏幕尺寸 | 每行显示数量 | 说明 |
|---------|------------|------|
| xs (<768px) | 1个 | 手机竖屏 |
| sm (≥768px) | 2个 | 手机横屏/小平板 |
| md (≥992px) | 3个 | 平板/小笔记本 |
| lg (≥1200px) | 4个 | 桌面显示器 |

## 🎯 Mock数据

当前使用Mock数据，包含15个真实设备：
- 生物设备：7个（显微镜×2、离心机×2、培养箱×2、分光光度计×1）
- 化学设备：8个（反应釜×2、滴定仪×2、色谱仪×2、pH计×2）

设备状态分布：
- 可借用：12个（80%）
- 已借出：2个（13.3%）
- 维修中：1个（6.7%）

## 🔄 后续优化建议

1. **后端集成**
   - [ ] 实现真实的分类API (`GET /student/categories`)
   - [ ] 实现收藏列表API (`GET /student/favorites`)
   - [ ] 添加设备排序功能（按名称、购入日期等）

2. **功能增强**
   - [ ] 添加高级筛选（价格范围、购入日期范围）
   - [ ] 添加视图切换（卡片视图/列表视图）
   - [ ] 添加批量操作（批量收藏、批量导出）
   - [ ] 添加最近浏览记录

3. **性能优化**
   - [ ] 图片懒加载
   - [ ] 虚拟滚动（大数据量时）
   - [ ] 缓存筛选条件到localStorage

4. **用户体验**
   - [ ] 添加骨架屏加载效果
   - [ ] 添加设备对比功能
   - [ ] 添加二维码扫描快速定位
   - [ ] 添加设备使用评价展示

## 📝 相关文件

- 页面组件：`frontend/src/views/student/Devices.vue`
- API接口：`frontend/src/api/student/index.js`
- Mock数据：`frontend/src/utils/mock.js`
- 路由配置：`frontend/src/router/index.js`

## 🚀 测试账号

- 学号：`2024001` ~ `2024010`
- 密码：`123456`

---

**最后更新**：2026-01-19  
**版本**：v1.0.0
