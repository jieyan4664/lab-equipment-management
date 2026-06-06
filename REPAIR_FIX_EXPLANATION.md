# 维修登记功能修复说明

## 🐛 问题描述

用户报告："维修报废"只显示记录，点击"登记维修"并填写信息后无法添加。

## 🔍 问题分析

经过排查，发现以下问题：

### 1. 日期格式问题
- **前端**：`el-date-picker`组件返回的是Date对象
- **后端**：期望接收字符串格式的日期（yyyy-MM-dd）
- **结果**：日期解析失败，导致请求报错

### 2. 设备选项硬编码
- **问题**：设备选择器的选项是硬编码的（只有2个固定选项）
- **影响**：如果数据库中没有ID为1或2的设备，或者有更多设备，用户无法正确选择
- **结果**：可能选择到不存在的设备，导致后端校验失败

### 3. deviceId初始值问题
- **问题**：`deviceId`初始值为空字符串`''`
- **影响**：表单验证时，空字符串被视为有值，但实际上不是有效的数字ID
- **结果**：可能传递无效的deviceId到后端

### 4. 缺少表单验证
- **问题**：提交前没有验证必填字段
- **影响**：用户可以提交空表单，导致后端报错
- **结果**：用户体验差，错误提示不明确

### 5. API使用Mock数据
- **问题**：`getDevices`方法使用了Mock数据（`USE_MOCK = true`）
- **影响**：无法获取真实的设备列表
- **结果**：设备选择器显示的是假数据

## ✅ 修复方案

### 1. 添加日期格式化函数

```javascript
// 格式化日期
const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}
```

在提交前将Date对象转换为字符串：
```javascript
const submitData = {
  ...repairForm,
  repairDate: formatDate(repairForm.repairDate)
}
```

### 2. 动态加载设备列表

```javascript
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
```

修改设备选择器：
```vue
<el-select v-model="repairForm.deviceId" placeholder="请选择设备" v-loading="deviceLoading">
  <el-option 
    v-for="device in deviceList" 
    :key="device.id" 
    :label="`${device.name}-${device.code}`" 
    :value="device.id" 
  />
</el-select>
```

### 3. 修正deviceId初始值

```javascript
const repairForm = reactive({
  deviceId: null,  // 从 '' 改为 null
  repairDate: '',
  repairPerson: '',
  cost: 0,
  result: 'repaired',
  description: ''
})
```

### 4. 添加表单验证

```javascript
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
  
  // ... 提交逻辑
}
```

### 5. 添加错误处理和日志

```javascript
try {
  await teacherApi.createRepair(submitData)
  ElMessage.success('登记成功')
  repairDialogVisible.value = false
  resetForm()
  loadRepairs()
} catch (error) {
  console.error('登记维修失败:', error)
  ElMessage.error(error.message || '操作失败')
}
```

### 6. 启用真实API

修改 `frontend/src/api/teacher/index.js`：

```javascript
// 修改前
getDevices(params) {
  if (USE_MOCK) return mock.getTeacherDevices(params)
  return request.get('/teacher/devices', { params })
}

// 修改后
getDevices(params) {
  return request.get('/teacher/devices', { params })
}
```

### 7. 添加表单重置功能

```javascript
const resetForm = () => {
  repairForm.deviceId = null
  repairForm.repairDate = ''
  repairForm.repairPerson = ''
  repairForm.cost = 0
  repairForm.result = 'repaired'
  repairForm.description = ''
}
```

在提交成功后调用：
```javascript
ElMessage.success('登记成功')
repairDialogVisible.value = false
resetForm()  // 重置表单
loadRepairs()
```

### 8. 页面加载时初始化设备列表

```javascript
onMounted(() => {
  loadRepairs()
  loadDevices() // 页面加载时加载设备列表
})
```

## 📝 修改的文件

### 1. frontend/src/views/teacher/Repairs.vue
- ✅ 添加日期格式化函数
- ✅ 添加设备列表加载功能
- ✅ 修改设备选择器为动态加载
- ✅ 添加表单验证
- ✅ 添加错误处理和日志
- ✅ 添加表单重置功能
- ✅ 修正deviceId初始值

### 2. frontend/src/api/teacher/index.js
- ✅ 移除Mock调用，启用真实API

## 🧪 测试步骤

### 1. 准备测试数据

确保数据库中有设备数据：

```sql
-- 检查是否有设备数据
SELECT id, code, name, status FROM device LIMIT 10;

-- 如果没有，插入测试数据
INSERT INTO device (code, name, category_id, brand, model, location, purchase_date, status) VALUES
('DEV-001', '显微镜', 1, '奥林巴斯', 'CX23', 'A栋-201-1号柜', '2024-03-15', 'available'),
('DEV-002', '离心机', 2, '湘仪', 'TGL-16', 'B栋-301-2号柜', '2023-06-20', 'available'),
('DEV-003', '分光光度计', 2, '上海精科', '722N', 'B栋-302-3号柜', '2024-01-10', 'available');
```

### 2. 重启服务

```bash
# 后端（如果已启动则跳过）
cd backed
mvn spring-boot:run

# 前端（如果已启动则刷新浏览器即可）
cd frontend
npm run dev
```

### 3. 测试维修登记功能

1. **登录系统**
   - 使用老师账号登录（工号：T001，密码：123456）

2. **访问维修页面**
   - 访问 http://localhost:3000/teacher/repairs

3. **验证设备列表加载**
   - ✅ 页面加载时，设备列表应该自动加载
   - ✅ 点击"登记维修"按钮，设备选择器应显示所有可用设备

4. **测试表单验证**
   - 点击"登记维修"按钮
   - 不填写任何信息，直接点击"保存"
   - ✅ 应该提示"请选择设备"
   
   - 选择设备后，再次点击"保存"
   - ✅ 应该提示"请选择维修日期"
   
   - 选择日期后，再次点击"保存"
   - ✅ 应该提示"请输入维修人员"

5. **测试完整流程**
   - 选择一个设备（如：显微镜-DEV-001）
   - 选择维修日期（如：2026-01-20）
   - 输入维修人员（如：张师傅）
   - 输入维修费用（如：500）
   - 选择维修结果（如：已修复）
   - 输入说明（如：更换光源系统）
   - 点击"保存"
   
   **预期结果：**
   - ✅ 提示"登记成功"
   - ✅ 对话框关闭
   - ✅ 维修列表自动刷新，显示新添加的记录
   - ✅ 表单被重置，可以再次登记

6. **验证设备状态更新**
   - 如果选择"已修复"：
     - ✅ 设备状态应该变为`available`
   
   - 如果选择"无法修复"：
     - ✅ 设备状态应该变为`scrap`

7. **查看浏览器控制台**
   - 打开浏览器开发者工具（F12）
   - 切换到Console标签
   - 执行维修登记操作
   - ✅ 不应该有错误日志
   - ✅ 如果失败，应该看到详细的错误信息

## 💡 常见问题

### Q1: 设备选择器显示为空？
**原因**：数据库中没有设备数据
**解决**：执行上面的SQL插入测试数据

### Q2: 提示"设备不存在"？
**原因**：选择的设备ID在数据库中不存在
**解决**：刷新页面重新加载设备列表

### Q3: 提示"无效的维修结果"？
**原因**：维修结果不是"repaired"或"unrepairable"
**解决**：确保选择正确的维修结果选项

### Q4: 日期格式错误？
**原因**：日期没有被正确格式化
**解决**：检查`formatDate`函数是否正确执行

### Q5: 网络请求失败？
**原因**：后端服务未启动或端口不对
**解决**：
1. 检查后端是否运行在 http://localhost:8080
2. 检查浏览器控制台的Network标签，查看请求详情

## 🎯 修复效果

修复前：
- ❌ 点击"登记维修"无法添加
- ❌ 设备选项是硬编码的
- ❌ 没有表单验证
- ❌ 错误提示不明确
- ❌ 使用Mock数据

修复后：
- ✅ 可以正常登记维修
- ✅ 设备列表动态加载
- ✅ 完整的表单验证
- ✅ 清晰的错误提示
- ✅ 使用真实API
- ✅ 自动重置表单
- ✅ 详细的错误日志

## 📌 注意事项

1. **确保后端服务已启动**
   - 维修登记需要调用后端API
   - 检查 http://localhost:8080 是否可访问

2. **确保数据库中有设备数据**
   - 设备选择器需要从数据库加载设备列表
   - 如果没有设备，请先添加设备

3. **检查浏览器控制台**
   - 如果遇到问题，查看Console和Network标签
   - 会有详细的错误信息和请求详情

4. **维修结果的影响**
   - 选择"已修复" → 设备状态变为available
   - 选择"无法修复" → 设备状态变为scrap
   - 请根据实际情况选择

---

现在您可以测试维修登记功能了！如果还有问题，请查看浏览器控制台的错误信息。🎉
