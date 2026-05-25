# 管理端"设备管理"功能实现文档

## 📋 功能概述

完成了老师端"设备管理"页面的后端实现，支持设备的CRUD操作、状态管理和二维码生成功能。

---

## 🏗️ 后端架构

### 1. 实体类（已存在）

**文件位置：** `backed/src/main/java/com/lab/backed/entity/Device.java`

```java
@Data
@TableName("device")
public class Device {
    private Integer id;
    private String code;              // 设备编号
    private String name;              // 设备名称
    private Integer categoryId;       // 分类ID
    private String brand;             // 品牌
    private String model;             // 型号
    private String spec;              // 规格参数
    private String technicalParams;   // 技术参数
    private String location;          // 存放位置
    private LocalDate purchaseDate;   // 购入日期
    private LocalDate warrantyDate;   // 保修截止日期
    private String status;            // 状态：available/borrowed/repair/scrap
    private Integer currentBorrowerId; // 当前借用人ID
    private LocalDateTime expectedReturnTime; // 预计归还时间
    private String description;       // 使用说明
    private String qrCode;            // 二维码标识
}
```

### 2. Mapper层（已存在）

**文件位置：** `backed/src/main/java/com/lab/backed/mapper/DeviceMapper.java`

```java
@Mapper
public interface DeviceMapper extends BaseMapper<Device> {
}
```

使用MyBatis-Plus的BaseMapper，自动提供基础CRUD方法。

### 3. Service层（新增）

#### 3.1 接口定义

**文件位置：** `backed/src/main/java/com/lab/backed/service/TeacherDeviceService.java`

```java
public interface TeacherDeviceService {
    // 获取设备列表（分页）
    Page<Map<String, Object>> getDeviceList(String keyword, String status, Integer page, Integer size);
    
    // 添加设备
    void createDevice(Device device);
    
    // 更新设备
    void updateDevice(Integer id, Device device);
    
    // 删除设备
    void deleteDevice(Integer id);
    
    // 修改设备状态
    void updateDeviceStatus(Integer id, String status, String reason);
    
    // 生成设备二维码
    Map<String, Object> generateQRCodes(List<Integer> deviceIds);
}
```

#### 3.2 实现类

**文件位置：** `backed/src/main/java/com/lab/backed/service/impl/TeacherDeviceServiceImpl.java`

**核心功能实现：**

##### ① 获取设备列表

```java
@Override
public Page<Map<String, Object>> getDeviceList(String keyword, String status, Integer page, Integer size) {
    // 构建查询条件
    LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<>();
    
    // 关键词搜索（设备名称或编号）
    if (keyword != null && !keyword.trim().isEmpty()) {
        wrapper.and(w -> w.like(Device::getName, keyword)
                         .or()
                         .like(Device::getCode, keyword));
    }
    
    // 状态筛选
    if (status != null && !status.trim().isEmpty()) {
        wrapper.eq(Device::getStatus, status);
    }
    
    // 按创建时间倒序
    wrapper.orderByDesc(Device::getCreatedAt);
    
    // 分页查询
    Page<Device> devicePage = new Page<>(page, size);
    Page<Device> result = deviceMapper.selectPage(devicePage, wrapper);
    
    // 转换为前端期望的格式
    List<Map<String, Object>> deviceList = result.getRecords().stream().map(d -> {
        Map<String, Object> dMap = new HashMap<>();
        dMap.put("id", d.getId());
        dMap.put("name", d.getName());
        dMap.put("code", d.getCode());
        dMap.put("category", getCategoryName(d.getCategoryId()));
        dMap.put("model", d.getModel());
        dMap.put("location", d.getLocation());
        dMap.put("purchaseDate", d.getPurchaseDate() != null ? d.getPurchaseDate().format(DATE_FORMATTER) : null);
        dMap.put("warrantyDate", d.getWarrantyDate() != null ? d.getWarrantyDate().format(DATE_FORMATTER) : null);
        dMap.put("status", d.getStatus());
        return dMap;
    }).collect(Collectors.toList());
    
    // 构建返回的分页对象
    Page<Map<String, Object>> returnPage = new Page<>(page, size);
    returnPage.setTotal(result.getTotal());
    returnPage.setRecords(deviceList);
    
    return returnPage;
}
```

**特点：**
- ✅ 支持关键词模糊搜索（设备名称或编号）
- ✅ 支持状态筛选（available/borrowed/repair/scrap）
- ✅ 分页查询，默认每页10条
- ✅ 关联查询分类名称
- ✅ 日期格式化为yyyy-MM-dd

##### ② 添加设备

```java
@Override
@Transactional
public void createDevice(Device device) {
    // 检查设备编号是否已存在
    LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(Device::getCode, device.getCode());
    Long count = deviceMapper.selectCount(wrapper);
    if (count > 0) {
        throw new RuntimeException("设备编号已存在");
    }
    
    // 设置默认值
    if (device.getStatus() == null || device.getStatus().trim().isEmpty()) {
        device.setStatus("available");
    }
    
    deviceMapper.insert(device);
}
```

**特点：**
- ✅ 唯一性校验：设备编号不能重复
- ✅ 默认状态设置为"available"
- ✅ 事务控制

##### ③ 更新设备

```java
@Override
@Transactional
public void updateDevice(Integer id, Device device) {
    // 检查设备是否存在
    Device existing = deviceMapper.selectById(id);
    if (existing == null) {
        throw new RuntimeException("设备不存在");
    }
    
    // 如果修改了编号，检查新编号是否已被其他设备使用
    if (!existing.getCode().equals(device.getCode())) {
        LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Device::getCode, device.getCode())
               .ne(Device::getId, id);
        Long count = deviceMapper.selectCount(wrapper);
        if (count > 0) {
            throw new RuntimeException("设备编号已存在");
        }
    }
    
    // 更新设备信息
    device.setId(id);
    deviceMapper.updateById(device);
}
```

**特点：**
- ✅ 存在性校验
- ✅ 编号唯一性校验（排除自身）
- ✅ 事务控制

##### ④ 删除设备

```java
@Override
@Transactional
public void deleteDevice(Integer id) {
    // 检查设备是否存在
    Device device = deviceMapper.selectById(id);
    if (device == null) {
        throw new RuntimeException("设备不存在");
    }
    
    // 检查设备是否正在被借用
    if ("borrowed".equals(device.getStatus())) {
        throw new RuntimeException("设备正在被借用，无法删除");
    }
    
    deviceMapper.deleteById(id);
}
```

**特点：**
- ✅ 存在性校验
- ✅ 业务规则校验：正在借用的设备不能删除
- ✅ 事务控制

##### ⑤ 修改设备状态

```java
@Override
@Transactional
public void updateDeviceStatus(Integer id, String status, String reason) {
    // 检查设备是否存在
    Device device = deviceMapper.selectById(id);
    if (device == null) {
        throw new RuntimeException("设备不存在");
    }
    
    // 验证状态值
    if (!"repair".equals(status) && !"scrap".equals(status)) {
        throw new RuntimeException("无效的状态值");
    }
    
    // 更新状态
    device.setStatus(status);
    
    // 如果是报废，清空当前借用人
    if ("scrap".equals(status)) {
        device.setCurrentBorrowerId(null);
        device.setExpectedReturnTime(null);
    }
    
    deviceMapper.updateById(device);
    
    // TODO: 记录状态变更日志
    System.out.println("设备状态变更 - ID: " + id + ", 状态: " + status + ", 原因: " + reason);
}
```

**特点：**
- ✅ 状态值校验（仅允许repair或scrap）
- ✅ 报废时自动清空借用人信息
- ✅ 记录状态变更日志（TODO：可创建日志表）

##### ⑥ 生成二维码

```java
@Override
public Map<String, Object> generateQRCodes(List<Integer> deviceIds) {
    // 验证设备ID
    for (Integer deviceId : deviceIds) {
        Device device = deviceMapper.selectById(deviceId);
        if (device == null) {
            throw new RuntimeException("设备ID " + deviceId + " 不存在");
        }
    }
    
    // 模拟生成PDF URL
    String pdfUrl = "/download/qr-codes-" + System.currentTimeMillis() + ".pdf";
    
    Map<String, Object> result = new HashMap<>();
    result.put("pdfUrl", pdfUrl);
    
    return result;
}
```

**特点：**
- ✅ 批量验证设备ID
- ⚠️ 目前为模拟实现，需要集成二维码生成库（如ZXing）和PDF生成库（如iText）

### 4. Controller层（新增）

**文件位置：** `backed/src/main/java/com/lab/backed/controller/TeacherDeviceController.java`

```java
@RestController
@RequestMapping("/api/v1/teacher/devices")
@RequiredArgsConstructor
public class TeacherDeviceController {
    
    private final TeacherDeviceService teacherDeviceService;
    
    // GET /api/v1/teacher/devices - 获取设备列表
    @GetMapping
    public Result<Map<String, Object>> getDeviceList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        Page<Map<String, Object>> result = teacherDeviceService.getDeviceList(keyword, status, page, size);
        
        Map<String, Object> data = Map.of(
            "total", result.getTotal(),
            "list", result.getRecords()
        );
        
        return Result.success(data);
    }
    
    // POST /api/v1/teacher/devices - 添加设备
    @PostMapping
    public Result<Void> createDevice(@RequestBody Device device) {
        teacherDeviceService.createDevice(device);
        return Result.success();
    }
    
    // PUT /api/v1/teacher/devices/{id} - 更新设备
    @PutMapping("/{id}")
    public Result<Void> updateDevice(@PathVariable Integer id, @RequestBody Device device) {
        teacherDeviceService.updateDevice(id, device);
        return Result.success();
    }
    
    // DELETE /api/v1/teacher/devices/{id} - 删除设备
    @DeleteMapping("/{id}")
    public Result<Void> deleteDevice(@PathVariable Integer id) {
        teacherDeviceService.deleteDevice(id);
        return Result.success();
    }
    
    // PUT /api/v1/teacher/devices/{id}/status - 修改设备状态
    @PutMapping("/{id}/status")
    public Result<Void> updateDeviceStatus(
            @PathVariable Integer id,
            @RequestBody Map<String, String> params) {
        
        String status = params.get("status");
        String reason = params.get("reason");
        
        teacherDeviceService.updateDeviceStatus(id, status, reason);
        return Result.success();
    }
    
    // POST /api/v1/teacher/devices/qr-codes - 生成二维码
    @PostMapping("/qr-codes")
    public Result<Map<String, Object>> generateQRCodes(@RequestBody Map<String, List<Integer>> params) {
        List<Integer> deviceIds = params.get("deviceIds");
        if (deviceIds == null || deviceIds.isEmpty()) {
            return Result.error("请选择要生成二维码的设备");
        }
        Map<String, Object> result = teacherDeviceService.generateQRCodes(deviceIds);
        return Result.success(result);
    }
}
```

### 5. 全局异常处理（新增）

**文件位置：** `backed/src/main/java/com/lab/backed/config/GlobalExceptionHandler.java`

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(RuntimeException.class)
    public Result<Void> handleRuntimeException(RuntimeException e) {
        return Result.error(e.getMessage());
    }
    
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        e.printStackTrace();
        return Result.error("服务器内部错误");
    }
}
```

**作用：**
- ✅ 统一捕获并处理异常
- ✅ 将异常信息转换为标准响应格式
- ✅ 避免敏感信息泄露

---

## 🔌 API接口文档

### 1. 获取设备列表

**接口地址：** `GET /api/v1/teacher/devices`

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| keyword | String | 否 | 搜索关键词（设备名称/编号） |
| status | String | 否 | 状态筛选：available/borrowed/repair/scrap |
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页数量，默认10 |

**响应数据：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 156,
    "list": [
      {
        "id": 1,
        "name": "显微镜",
        "code": "DEV-001",
        "category": "生物设备",
        "model": "CX23",
        "location": "A-201",
        "purchaseDate": "2024-03-15",
        "warrantyDate": "2026-03-15",
        "status": "available"
      }
    ]
  },
  "timestamp": 1705315200000
}
```

---

### 2. 添加设备

**接口地址：** `POST /api/v1/teacher/devices`

**请求体：**

```json
{
  "name": "离心机",
  "code": "DEV-100",
  "categoryId": 1,
  "brand": "Eppendorf",
  "model": "5424R",
  "spec": "24 x 1.5/2.0 mL",
  "technicalParams": "最高转速15,000 rpm",
  "location": "B-301",
  "purchaseDate": "2024-06-01",
  "warrantyDate": "2026-06-01",
  "description": "高速冷冻离心机"
}
```

**响应数据：**

```json
{
  "code": 200,
  "message": "success",
  "data": null,
  "timestamp": 1705315200000
}
```

---

### 3. 更新设备

**接口地址：** `PUT /api/v1/teacher/devices/{id}`

**路径参数：**

| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | Integer | 设备ID |

**请求体：** 同添加设备

**响应数据：** 同添加设备

---

### 4. 删除设备

**接口地址：** `DELETE /api/v1/teacher/devices/{id}`

**路径参数：**

| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | Integer | 设备ID |

**响应数据：**

```json
{
  "code": 200,
  "message": "success",
  "data": null,
  "timestamp": 1705315200000
}
```

---

### 5. 修改设备状态

**接口地址：** `PUT /api/v1/teacher/devices/{id}/status`

**路径参数：**

| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | Integer | 设备ID |

**请求体：**

```json
{
  "status": "repair",
  "reason": "电机故障，需要更换"
}
```

**响应数据：** 同删除设备

---

### 6. 生成二维码

**接口地址：** `POST /api/v1/teacher/devices/qr-codes`

**请求体：**

```json
{
  "deviceIds": [1, 2, 3]
}
```

**响应数据：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "pdfUrl": "/download/qr-codes-1705315200000.pdf"
  },
  "timestamp": 1705315200000
}
```

---

## 🎨 前端对接

### 1. API配置

**文件位置：** `frontend/src/api/teacher/index.js`

前端API已经配置完成，与后端接口完全匹配：

```javascript
export const teacherApi = {
  // 获取设备列表
  getDevices(params) {
    if (USE_MOCK) return mock.getTeacherDevices(params)
    return request.get('/teacher/devices', { params })
  },

  // 添加设备
  createDevice(data) {
    return request.post('/teacher/devices', data)
  },

  // 编辑设备
  updateDevice(id, data) {
    return request.put(`/teacher/devices/${id}`, data)
  },

  // 删除设备
  deleteDevice(id) {
    return request.delete(`/teacher/devices/${id}`)
  },

  // 修改设备状态
  updateDeviceStatus(id, data) {
    return request.put(`/teacher/devices/${id}/status`, data)
  },

  // 生成二维码
  generateQRCodes(deviceIds) {
    return request.post('/teacher/devices/qr-codes', { deviceIds })
  }
}
```

### 2. 页面组件

**文件位置：** `frontend/src/views/teacher/Devices.vue`

前端页面已完整实现，包括：
- ✅ 设备列表展示（表格）
- ✅ 搜索和筛选功能
- ✅ 添加/编辑对话框
- ✅ 状态修改对话框
- ✅ 批量选择设备
- ✅ 二维码生成
- ✅ 分页功能

### 3. 启用真实API

修改 `frontend/src/api/teacher/index.js`：

```javascript
const USE_MOCK = false  // 改为false启用真实API
```

---

## 🧪 测试步骤

### 1. 启动后端服务

```bash
cd backed
mvn spring-boot:run
```

### 2. 启动前端服务

```bash
cd frontend
npm run dev
```

### 3. 访问设备管理页面

1. 使用老师账号登录（工号：T001，密码：123456）
2. 访问 http://localhost:3000/teacher/devices
3. 修改 `USE_MOCK = false` 启用真实API

### 4. 功能测试清单

#### ✅ 获取设备列表
- [ ] 默认加载第一页数据
- [ ] 按关键词搜索（设备名称/编号）
- [ ] 按状态筛选
- [ ] 分页切换正常

#### ✅ 添加设备
- [ ] 点击"添加设备"按钮打开对话框
- [ ] 填写必填字段（名称、编号、分类、位置）
- [ ] 提交成功，列表刷新
- [ ] 设备编号重复时提示错误

#### ✅ 编辑设备
- [ ] 点击"编辑"按钮打开对话框
- [ ] 修改设备信息
- [ ] 提交成功，列表刷新
- [ ] 修改编号时检查唯一性

#### ✅ 删除设备
- [ ] 点击"删除"按钮弹出确认框
- [ ] 确认后删除成功
- [ ] 正在借用的设备提示无法删除

#### ✅ 修改状态
- [ ] 点击"状态"按钮打开对话框
- [ ] 选择状态（维修中/已报废）
- [ ] 填写原因
- [ ] 提交成功，状态更新

#### ✅ 生成二维码
- [ ] 勾选多个设备
- [ ] 点击"生成二维码"按钮
- [ ] 提示生成成功
- [ ] （TODO）下载PDF文件

---

## 📊 数据库表结构

### device表

```sql
CREATE TABLE `device` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `code` VARCHAR(50) NOT NULL COMMENT '设备编号（唯一）',
  `name` VARCHAR(100) NOT NULL COMMENT '设备名称',
  `category_id` INT NOT NULL COMMENT '分类ID（外键）',
  `brand` VARCHAR(50) DEFAULT NULL COMMENT '品牌',
  `model` VARCHAR(100) DEFAULT NULL COMMENT '型号',
  `spec` TEXT DEFAULT NULL COMMENT '规格参数',
  `technical_params` TEXT DEFAULT NULL COMMENT '技术参数',
  `location` VARCHAR(100) NOT NULL COMMENT '存放位置',
  `purchase_date` DATE DEFAULT NULL COMMENT '购入日期',
  `warranty_date` DATE DEFAULT NULL COMMENT '保修截止日期',
  `status` ENUM('available','borrowed','repair','scrap') NOT NULL DEFAULT 'available' COMMENT '状态',
  `current_borrower_id` INT DEFAULT NULL COMMENT '当前借用人ID',
  `expected_return_time` DATETIME DEFAULT NULL COMMENT '预计归还时间',
  `description` TEXT DEFAULT NULL COMMENT '使用说明',
  `qr_code` VARCHAR(255) DEFAULT NULL COMMENT '二维码标识',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备表';
```

---

## 🔧 技术要点

### 1. MyBatis-Plus的使用

- ✅ 继承BaseMapper获得基础CRUD能力
- ✅ 使用LambdaQueryWrapper进行类型安全查询
- ✅ 分页插件自动处理分页逻辑

### 2. 事务管理

- ✅ 使用@Transactional注解保证数据一致性
- ✅ 写操作（增删改）都包含在事务中

### 3. 异常处理

- ✅ 业务异常抛出RuntimeException
- ✅ 全局异常处理器统一捕获并转换
- ✅ 返回标准错误响应

### 4. 数据校验

- ✅ 设备编号唯一性校验
- ✅ 设备存在性校验
- ✅ 状态值合法性校验
- ✅ 业务规则校验（如：借用中的设备不能删除）

### 5. 日期格式化

- ✅ 使用DateTimeFormatter格式化LocalDate
- ✅ 统一格式：yyyy-MM-dd

### 6. 关联查询

- ✅ 根据categoryId查询分类名称
- ✅ 空值处理：未分类显示"未分类"

---

## 🚀 性能优化建议

### 1. 数据库索引

确保以下字段有索引：
- ✅ `code`（唯一索引）
- ✅ `category_id`（普通索引）
- ✅ `status`（普通索引）
- ✅ `created_at`（用于排序）

### 2. 缓存策略

可以考虑缓存：
- 分类名称映射（DeviceCategory）
- 高频访问的设备信息

### 3. 分页优化

- 当前使用offset分页，大数据量时可改用游标分页
- 限制最大page size（如最多100条/页）

### 4. 二维码生成优化

- 异步生成二维码，避免阻塞主线程
- 使用消息队列处理批量生成任务
- 缓存生成的PDF文件

---

## 📝 TODO清单

### 高优先级
- [ ] 实现真实的二维码生成功能（集成ZXing库）
- [ ] 实现PDF打包功能（集成iText库）
- [ ] 添加设备状态变更日志表
- [ ] 完善文件上传功能（设备图片）

### 中优先级
- [ ] 添加设备分类管理接口
- [ ] 实现批量导入设备功能（Excel）
- [ ] 实现批量导出设备功能（Excel）
- [ ] 添加设备借用历史记录查询

### 低优先级
- [ ] 实现设备预约日历视图
- [ ] 添加设备维修记录关联
- [ ] 实现设备报废审批流程
- [ ] 添加设备统计报表

---

## 🐛 常见问题

### Q1: 设备编号重复怎么办？

**A:** 系统会自动检测设备编号的唯一性，如果重复会抛出异常："设备编号已存在"。

### Q2: 为什么删除设备失败？

**A:** 可能的原因：
1. 设备正在被借用（status = "borrowed"）
2. 设备ID不存在

### Q3: 如何修改设备状态为"available"？

**A:** 当前接口只支持修改为"repair"或"scrap"。如果需要恢复为"available"，需要通过归还流程自动更新。

### Q4: 二维码生成为什么返回的是模拟URL？

**A:** 目前二维码生成功能是模拟实现，需要集成第三方库（如ZXing）来生成真实的二维码图片。

---

## 📚 相关文档

- [需求文档](requirement.md) - 查看完整的需求说明
- [数据库设计](backed/src/main/resources/db/schema.sql) - 查看表结构
- [前端页面](frontend/src/views/teacher/Devices.vue) - 查看前端实现

---

## ✨ 总结

✅ **已完成的功能：**
1. 设备列表查询（分页、筛选、搜索）
2. 设备添加（含唯一性校验）
3. 设备编辑（含编号唯一性校验）
4. 设备删除（含业务规则校验）
5. 设备状态修改（维修/报废）
6. 二维码生成（模拟实现）
7. 全局异常处理

✅ **前后端对接：**
- API路径完全匹配
- 请求参数格式一致
- 响应数据结构一致
- 错误处理机制完善

✅ **代码质量：**
- 类型安全的Lambda查询
- 事务控制保证数据一致性
- 完善的参数校验和业务规则校验
- 清晰的代码结构和注释

现在您可以重启后端并访问设备管理页面进行测试了！🎉
