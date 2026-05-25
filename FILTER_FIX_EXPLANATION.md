# 设备管理筛选功能修复说明

## 🔍 问题诊断

### 问题描述
在管理端设备管理页面，通过关键字搜索或状态筛选后，数据显示没有变化。

### 根本原因
前端配置 `USE_MOCK = true`，使用的是Mock数据。而Mock数据的 `getTeacherDevices` 方法**没有处理筛选参数**（keyword和status），只是简单地返回所有设备的分页数据。

---

## ✅ 解决方案

### 方案1：修复Mock数据（已完成）✅

**修改文件：** `frontend/src/utils/mock.js`

**修改内容：**
```javascript
async getTeacherDevices(params) {
  await delay(300)
  const page = params.page || 1
  const size = params.size || 10
  
  // 筛选数据
  let filteredDevices = mockDevices
  
  // 关键词筛选（设备名称或编号）
  if (params.keyword && params.keyword.trim()) {
    const keyword = params.keyword.toLowerCase()
    filteredDevices = filteredDevices.filter(device => 
      device.name.toLowerCase().includes(keyword) || 
      device.code.toLowerCase().includes(keyword)
    )
  }
  
  // 状态筛选
  if (params.status && params.status.trim()) {
    filteredDevices = filteredDevices.filter(device => 
      device.status === params.status
    )
  }
  
  return {
    total: filteredDevices.length,
    list: filteredDevices.slice((page - 1) * size, page * size)
  }
}
```

**优点：**
- ✅ 立即生效，无需重启后端
- ✅ 可以快速测试前端筛选逻辑
- ✅ 适合开发阶段调试

**缺点：**
- ⚠️ 数据是静态的，无法测试真实的数据库查询
- ⚠️ 不支持复杂的业务逻辑（如唯一性校验）

---

### 方案2：启用真实API（推荐）🚀

**步骤1：修改前端配置**

编辑 `frontend/src/api/teacher/index.js`：

```javascript
const USE_MOCK = false  // 改为false启用真实API
```

**步骤2：确保后端服务运行**

```bash
cd backed
mvn spring-boot:run
```

**步骤3：测试功能**

访问 http://localhost:3000/teacher/devices

**优点：**
- ✅ 使用真实的数据库数据
- ✅ 完整的业务逻辑（唯一性校验、事务控制等）
- ✅ 可以测试所有CRUD操作

**缺点：**
- ⚠️ 需要确保数据库中有测试数据
- ⚠️ 需要后端服务正常运行

---

## 🧪 测试步骤

### 测试Mock模式（当前）

1. **刷新页面**
   - 由于修改了Mock代码，刷新浏览器即可生效

2. **测试关键词搜索**
   - 在搜索框输入"显微镜"
   - 点击"搜索"按钮
   - ✅ 应该只显示包含"显微镜"的设备

3. **测试设备编号搜索**
   - 在搜索框输入"DEV-001"
   - 点击"搜索"按钮
   - ✅ 应该只显示编号为"DEV-001"的设备

4. **测试状态筛选**
   - 选择状态"可借用"
   - 点击"搜索"按钮
   - ✅ 应该只显示状态为"available"的设备

5. **测试组合筛选**
   - 输入关键词"显微镜"
   - 选择状态"可借用"
   - 点击"搜索"按钮
   - ✅ 应该只显示名称包含"显微镜"且状态为"可借用"的设备

6. **测试重置功能**
   - 点击"重置"按钮
   - ✅ 应该清除所有筛选条件，显示全部设备

---

## 📊 后端实现检查清单

后端设备管理功能已完整实现，包括：

### ✅ 已实现的功能

1. **获取设备列表** `GET /api/v1/teacher/devices`
   - ✅ 分页查询
   - ✅ 关键词搜索（设备名称/编号）
   - ✅ 状态筛选
   - ✅ 关联查询分类名称

2. **添加设备** `POST /api/v1/teacher/devices`
   - ✅ 唯一性校验（设备编号）
   - ✅ 默认状态设置

3. **更新设备** `PUT /api/v1/teacher/devices/{id}`
   - ✅ 存在性校验
   - ✅ 编号唯一性校验

4. **删除设备** `DELETE /api/v1/teacher/devices/{id}`
   - ✅ 业务规则校验（借用中的设备不能删除）

5. **修改设备状态** `PUT /api/v1/teacher/devices/{id}/status`
   - ✅ 状态值校验（repair/scrap）
   - ✅ 报废时清空借用人信息

6. **生成二维码** `POST /api/v1/teacher/devices/qr-codes`
   - ✅ 批量验证设备ID
   - ⚠️ 模拟实现（返回PDF URL）

### 📁 相关文件

- Controller: `backed/src/main/java/com/lab/backed/controller/TeacherDeviceController.java`
- Service: `backed/src/main/java/com/lab/backed/service/TeacherDeviceService.java`
- ServiceImpl: `backed/src/main/java/com/lab/backed/service/impl/TeacherDeviceServiceImpl.java`
- ExceptionHandler: `backed/src/main/java/com/lab/backed/config/GlobalExceptionHandler.java`

---

## 🔄 切换到真实API的步骤

当您准备好切换到真实API时：

### 1. 修改前端配置

```javascript
// frontend/src/api/teacher/index.js
const USE_MOCK = false  // 从true改为false
```

### 2. 确保数据库有测试数据

执行以下SQL插入测试数据：

```sql
-- 插入设备分类
INSERT INTO device_category (name, parent_id, lab_type, sort_order) VALUES
('生物设备', 0, 'bio', 1),
('化学设备', 0, 'chem', 2);

-- 插入测试设备
INSERT INTO device (code, name, category_id, brand, model, spec, location, purchase_date, warranty_date, status) VALUES
('DEV-001', '光学显微镜', 1, '奥林巴斯', 'CX23', '40x-1000x', 'A栋-201-1号柜', '2024-03-15', '2026-03-15', 'available'),
('DEV-002', '电子天平', 2, '梅特勒', 'ME204E', '0.1mg', 'B栋-301-2号柜', '2024-05-20', '2026-05-20', 'available'),
('DEV-003', '离心机', 1, 'Eppendorf', '5424R', '15000rpm', 'A栋-202-3号柜', '2024-06-01', '2026-06-01', 'borrowed'),
('DEV-004', 'pH计', 2, '雷磁', 'PHS-3C', '0.01pH', 'B栋-302-4号柜', '2024-07-10', '2026-07-10', 'repair'),
('DEV-005', '分光光度计', 2, '岛津', 'UV-1800', '190-1100nm', 'B栋-303-5号柜', '2024-08-15', '2026-08-15', 'available');
```

### 3. 重启前端服务

```bash
cd frontend
npm run dev
```

### 4. 测试真实API

- 刷新浏览器
- 尝试搜索和筛选
- 尝试添加、编辑、删除设备
- 查看浏览器开发者工具的Network标签，确认请求发送到后端

---

## 🐛 常见问题

### Q1: 修改Mock后为什么还是没有效果？

**A:** 请确保：
1. 保存了文件
2. 刷新了浏览器（Ctrl+F5 强制刷新）
3. 检查浏览器控制台是否有错误

### Q2: 切换到真实API后出现404错误？

**A:** 请检查：
1. 后端服务是否正常运行
2. 后端端口是否正确（默认8080）
3. 前端代理配置是否正确（vite.config.js）

### Q3: 搜索后显示"加载设备列表失败"？

**A:** 可能的原因：
1. 后端服务未启动
2. 数据库连接失败
3. 接口路径不匹配

检查浏览器控制台的错误信息，查看具体的错误原因。

### Q4: 如何查看Mock数据和真实API的区别？

**A:** 打开浏览器开发者工具（F12）：
- Mock模式：不会看到网络请求
- 真实API：可以看到 `/api/v1/teacher/devices` 的请求

---

## 📝 总结

✅ **已完成：**
1. 修复Mock数据的筛选逻辑
2. 关键词搜索支持（设备名称/编号）
3. 状态筛选支持
4. 组合筛选支持

🚀 **下一步建议：**
1. 先测试Mock模式，确保前端筛选逻辑正确
2. 准备数据库测试数据
3. 切换到真实API（USE_MOCK = false）
4. 测试完整的CRUD功能

现在请刷新浏览器，测试搜索和筛选功能！🎉
