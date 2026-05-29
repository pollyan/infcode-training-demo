# 客户档案同步功能实现计划

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 在客户主数据列表页新增"同步外部档案"功能，调用外部客户中心接口获取最新客户资料并更新本地数据。

**Architecture:** 采用经典的分层架构，前端在 Service 层封装 API 调用，后端在 Service 层处理业务逻辑，Integration 层负责外部系统集成。遵循 TDD 原则，先写测试再写实现。

**Tech Stack:** 前端原生 JavaScript (ES6+)，后端 Spring Boot 3.3.2 + Java 17，Mock 服务 Node.js。

---

## 任务总览

本功能涉及前端和后端的协同开发，核心流程为：
1. 前端在操作列新增"同步"按钮
2. 点击按钮调用后端同步接口
3. 后端调用外部客户中心 Mock 服务
4. 后端完成字段映射和数据更新
5. 前端更新单行状态并显示提示

---

## Task 1: 前端 Service 层新增同步接口方法

**Files:**
- Modify: `frontend/src/services/customerService.js`

**Step 1: 查看现有 Service 文件结构**

查看 `customerService.js` 的现有代码，了解接口封装模式。

**Step 2: 添加同步接口方法**

在 `customerService.js` 中新增 `syncCustomerProfile` 方法：

```javascript
export async function syncCustomerProfile(customer(customerCode) {
  const response = await http.post(`/api/customers/${customerCode}/sync`);
  return response.data;
}
```

**Step 3: 提交代码**

```bash
git add frontend/src/services/customerService.js
git commit -m "feat(frontend): 新增客户档案同步接口方法"
```

---

## Task 2: 前端页面添加同步按钮

**Files:**
- Modify: `frontend/src/pages/customer/CustomerDirectoryPage.js`

**Step 1: 在表格操作列添加同步按钮**

修改 `renderTableRows` 函数，在操作列中添加"同步"按钮（约第 93-97 行）：

```javascript
<div class="table-actions">
  <button type="button" class="ghost-button" data-action="view" data-code="${escapeHtml(customer.customerCode)}">查看</button>
  <button type="button" class="ghost-button" data-action="edit" data-code="${escapeHtml(customer.customerCode)}">编辑</button>
  <button type="button" class="ghost-button" data-action="sync" data-code="${escapeHtml(customer.customerCode)}">同步</button>
</div>
```

**Step 2: 在按钮点击事件处理中添加同步逻辑**

修改 `bindCustomerDirectoryPage` 函数中的点击事件处理（约第 294-309 行），添加 `sync` 操作的处理：

```javascript
if (action === "sync") {
  await handleSyncCustomer(customerCode);
  return;
}
```

**Step 3: 添加同步处理函数**

在 `bindCustomerDirectoryPage` 函数内部，`loadCustomers` 函数之后添加 `handleSyncCustomer` 函数：

```javascript
async function handleSyncCustomer(customerCode) {
  const customerIndex = state.customers.findIndex(c => c.customerCode === customerCode);
  if (customerIndex === -1) return;
  
  // 临时更新该行状态为同步中
  const originalSyncStatus = state.customers[customerIndex].syncStatus;
  state.customers[customerIndex].syncStatus = "PENDING";
  renderState();
  
  try {
    {
      const updatedCustomer = await syncCustomerProfile(customerCode);
      // 更新该行数据
      state.customers[customerIndex] = {
        ...state.customers[customerIndex],
        ...updatedCustomer
      };
      state.alertType = "success";
      state.alertMessage = `${customerCode} 档案同步成功`;
    } catch (error) {
      // 恢复原同步状态
      state.customers[customerIndexIndex].syncStatus = originalSyncStatus;
      state.alertType = "error";
      state.alertMessage = error.message || `${customerCode} 档案同步失败，请稍后重试`;
    } finally {
      renderState();
    }
  }
}
```

**Step 4: 导入同步接口方法**

在文件顶部添加导入：

```javascript
import { fetchCustomerDirectory, syncCustomerProfile } from "../../services/customerService.js";
```

**Step 5: 提交代码**

```bash
git add frontend/src/pages/customer/CustomerDirectoryPage.js
git commit -m "feat(frontend): 添加同步按钮和处理逻辑"
```

---

## Task 3: 后端创建外部客户信息响应 DTO

**Files:**
- Create: `backend/src/main/java/com/example/training/dto/ExternalCustomerResponse.java`

**Step 1: 创建外部客户中心响应 DTO**

```java
package com.example.training.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 外部客户中心响应 DTO
 * 注意：字段名使用下划线风格，映射自第三方系统
 */
public class ExternalCustomerResponse {
    
    private Integer code;
    
    private String message;
    
    private ExternalCustomerData data;
    
    // Getters and Setters
    public Integer getCode() {
        return code;
    }
    
    public void setCode(Integer code) {
        this.code = code;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public ExternalCustomerData getData() {
        return data;
    }
    
    public void setData(ExternalCustomerData data) {
        this.data = data;
    }
    
    /**
     * 外部客户数据
     */
    public static class ExternalCustomerData {
        
        @JsonProperty("cust_code")
        private String customerCode;
        
        @JsonProperty("cust_name")
        private String customerName;
        
        @JsonProperty("cust_status_cd")
        private String customerStatus;
        
        @JsonProperty("contact_phone")
        private String contactPhone;
        
        @JsonProperty("last_modified_date")
        private String lastModifiedDate;
        
        // Getters and Setters
        public String getCustomerCode() {
            return customerCode;
        }
        
        public void setCustomerCode(String customerCode) {
            this.customerCode = customerCode;
        }
        
        public String getCustomerName() {
            return customerName;
        }
        
        public void setCustomerName(String customerName) {
            this.customerName = customerName;
        }
        
        public String getCustomerStatus() {
            return customerStatus;
        }
        
        public void setCustomerStatus(String customerStatus) {
            this.customerStatus = customerStatus;
        }
        
        public String getContactPhone() {
            return contactPhone;
        }
        
        public void setContactPhone(String contactPhone) {
            this.contactPhone = contactPhone;
        }
        
        public String getLastModifiedDate() {
            return lastModifiedDate;
        }
        
        public void setLastModifiedDate(String lastModifiedDate) {
            this.lastModifiedDate = lastModifiedDate;
        }
    }
}
```

**Step 2: 提交代码**

```bash
git add backend/src/main/java/com/example/training/dto/ExternalCustomerResponse.java
git commit -m "feat(backend): 创建外部客户中心响应 DTO"
```

---

## Task 4: 后端创建业务异常类

**Files:**
- Create: `backend/src/main/java/com/example/training/exception/BizException.java`

**Step 1: 创建业务异常类**

```java
package com.example.training.exception;

/**
 * 业务异常
 * 用于表示业务逻辑错误，携带用户友好的错误提示
 */
public class BizException extends RuntimeException {
    
    private final String userMessage;
    
    public BizException(String userMessage) {
        super(userMessage);
        this.userMessage = userMessage;
    }
    
    public BizException(String userMessage, Throwable cause) {
        super(userMessage, cause);
        this.userMessage = userMessage;
    }
    
    public String getUserMessage() {
        return userMessage;
    }
}
```

**Step 2: 修改全局异常处理器处理 BizException**

修改 `GlobalExceptionHandler.java`，添加对 `BizException` 的处理：

```java
@ExceptionHandler(BizException.class)
public ResponseEntity<CommonResponse<?>> handleBizException(BizException ex) {
    return ResponseEntity.ok(CommonResponse.error(ex.getUserMessage()));
}
```

**Step 3: 提交代码**

```bash
git add backend/src/main/java/com/example/training/exception/BizException.java
git add backend/src/main/java/com/example/training/exception/GlobalExceptionHandler.java
git commit -m "feat(backend): 创建业务异常类并完善异常处理"
```

---

## Task 5: 后端创建外部系统集成类

**Files:**
- Create: `backend/src/main/java/com/example/training/integration/ExternalCustomerCenterClient.java`

**Step 1: 创建外部客户中心集成客户端**

```java
package com.example.training.integration;

import com.example.training.dto.ExternalCustomerResponse;
import com.example.training.exception.BizException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 外部客户中心集成客户端
 * 负责与第三方客户中心系统的交互
 */
@Component
public class ExternalCustomerCenterClient {
    
    private static final Logger logger = LoggerFactory.getLogger(ExternalCustomerCenterClient.class);
    
    private static final String APP_KEY = "xingchen-crm-local";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        .withZone(ZoneId.systemDefault());
    
    private final RestTemplate restTemplate;
    
    @Value("${external.customer-center.url:http://localhost:9090/mock/customer-center}")
    private String customerCenterBaseUrl;
    
    public ExternalCustomerCenterClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    /**
     * 查询客户档案
     * 
     * @param customerCode 客户编号
     * @return 外部客户信息
     * @throws BizException 当外部服务调用失败时抛出
     */
    public ExternalCustomerResponse.ExternalCustomerData fetchCustomerProfile(String customerCode) {
        String url = customerCenterBaseUrl + "/customers/" + customerCode;
        
        logger.info("[客户档案同步] 开始调用外部客户中心, customerCode={}", customerCode);
        
        try {
            // 构建请求头
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-App-Key", APP_KEY);
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
            
            // 发起请求
            ResponseEntity<ExternalCustomerResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                ExternalCustomerResponse.class
            );
            
            ExternalCustomerResponse externalResponse = response.getBody();
            
            if (externalResponse == null || externalResponse.getCode() == null) {
                logger.error("[客户档案同步] 外部客户中心响应为空, customerCode={}", customerCode);
                throw new BizException("外部客户中心服务响应异常，请稍后重试");
            }
            
            // 处理错误码
            if (!200.equals(externalResponse.getCode())) {
                String errorMessage = mapErrorCodeToMessage(externalResponse.getCode());
                logger.warn("[客户档案同步] 外部客户中心返回错误, customerCode={}, code={}, message={}",
                    customerCode, externalResponse.getCode(), externalResponse.getMessage());
                throw new BizException(errorMessage);
            }
            
            // 检查数据
            if (externalResponse.getData() == null) {
                logger.warn("[客户档案同步] 外部客户中心返回数据为空, customerCode={}", customerCode);
                throw new BizException("外部客户中心未找到该客户档案");
            }
            
            logger.info("[客户档案同步] 成功获取客户档案, customerCode={}", customerCode);
            return externalResponse.getData();
            
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            logger.error("[客户档案同步] 调用外部客户中心异常, customerCode={}", customerCode, e);
            throw new BizException("网络连接失败，请稍后重试");
        }
    }
    
    /**
     * 错误码映射
     */
    private String mapErrorCodeToMessage(Integer code) {
        return switch (code) {
            case 4001 -> "请求参数格式错误";
            case 4004 -> "外部客户中心未找到该客户档案";
            case 5000 -> "外部客户中心服务不可用";
            case 5002 -> "请求过于频繁，请稍后重试";
            default -> "外部客户中心返回未知错误：" + code;
        };
    }
}
```

**Step 2: 配置 RestTemplate Bean**

在 `TrainingApplication.java` 中添加 RestTemplate 配置：

```java
@Bean
public RestTemplate restTemplate() {
    return new RestTemplate();
}
```

**Step 3: 提交代码**

```bash
git add backend/src/main/java/com/example/training/integration/ExternalCustomerCenterClient.java
git add backend/src/main/java/com/example/training/TrainingApplication.java
git commit -m "feat(backend): 创建外部客户中心集成客户端"
```

---

## Task 6: 后端 Service 层实现同步逻辑

**Files:**
- Modify: `backend/src/main/java/com/example/training/service/CustomerDirectoryService.java`

**Step 1: 在 Service 中添加同步方法**

添加 `syncCustomerProfile` 方法：

```java
import com.example.training.dto.ExternalCustomerResponse;
import com.example.training.integration.ExternalCustomerCenterClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class CustomerDirectoryService {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomerDirectoryService.class);
    
    private final ExternalCustomerCenterClient externalCustomerCenterClient;
    
    public CustomerDirectoryService(ExternalCustomerCenterClient externalCustomerCenterClient) {
        this.externalCustomerCenterClient = externalCustomerCenterClient;
        initializeCustomerStore();
    }
    
    /**
     * 同步客户档案
     * 
     * @param customerCode 客户编号
     * @return 更新后的客户信息
     */
    public CustomerDirectoryItemResponse syncCustomerProfile(String customerCode) {
        logger.info("[客户档案同步] 开始同步客户档案, customerCode={}", customerCode);
        
        // 调用外部客户中心
        ExternalCustomerResponse.ExternalCustomerData externalData = 
            externalCustomerCenterClient.fetchCustomerProfile(customerCode);
        
        // 更新本地数据
        CustomerDirectoryItemResponse customer = customerStore.get(customerCode);
        if (customer == null) {
            logger.warn("[客户档案同步] 客户不存在, customerCode={}", customerCode);
            throw new BizException("客户不存在");
        }
        
        // 字段映射
        customer.setCustomerName(externalData.getCustomerName());
        customer.setCustomerStatus(externalData.getCustomerStatus());
        
        // 联系电话脱敏处理
        String maskedPhone = maskPhoneNumber(externalData.getContactPhone());
        customer.setContactPhone(maskedPhone);
        
        // 格式化更新时间
        String formattedTime = formatUpdateTime(externalData.getLastModifiedDate());
        customer.setUpdatedTime(formattedTime);
        customer.setLastSyncTime(DATE_TIME_FORMATTER.format(LocalDateTime.now()));
        
        // 更新同步状态
        customer.setSyncStatus("SUCCESS");
        customer.setSyncMessage("同步成功");
        
        logger.info("[客户档案同步] 同步完成, customerCode={}", customerCode);
        return copyItem(customer);
    }
    
    /**
     * 手机号脱敏
     * 保留前3后4，中间4位用星号屏蔽
     */
    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 7) {
            return phoneNumber;
        }
        int length = phoneNumber.length();
        return phoneNumber.substring(0, 3) + "****" + phoneNumber.substring(length - 4);
    }
    
    /**
     * 格式化更新时间
     */
    private String formatUpdateTime(String lastModifiedDate) {
        if (lastModifiedDate == null) {
            return DATE_TIME_FORMATTER.format(LocalDateTime.now());
        }
        try {
            // 尝试解析 ISO 8601 格式时间
            Instant instant = Instant.parse(lastModifiedDate);
            return DATE_TIME_FORMATTER.format(LocalDateTime.ofInstant(instant, ZoneId.systemDefault()));
        } catch (Exception e) {
            logger.warn("[客户档案同步] 时间格式解析失败, lastModifiedDate={}", lastModifiedDate);
            return DATE_TIME_FORMATTER.format(LocalDateTime.now());
        }
    }
}
```

**Step 2: 提交代码**

```bash
git add backend/src/main/java/com/example/training/service/CustomerDirectoryService.java
git commit -m "feat(backend): Service 层实现客户档案同步逻辑"
```

---

## Task 7: 后端 Controller 层添加同步接口

**Files:**
- Modify: `backend/src/main/java/com/example/training/controller/CustomerDirectoryController.java`

**Step 1: 添加同步接口方法**

```java
@PostMapping("/{customerCode}/sync")
public CommonResponse<CustomerDirectoryItemResponse> syncCustomerProfile(
    @PathVariable String customerCode
) {
    return CommonResponse.success(customerDirectoryService.syncCustomerProfile(customerCode));
}
```

**Step 2: 提交代码**

```bash
git add backend/src/main/java/com/example/training/controller/CustomerDirectoryController.java
git commit -m "feat(backend): Controller 层添加同步接口"
```

---

## 验证步骤

所有代码实现完成后，按以下步骤验证功能：

**1. 启动所有服务**

```bash
./scripts/start-all.sh
```

**2. 访问前端页面**

打开浏览器访问：http://localhost:5173

**3. 测试同步功能**

- 找到任意客户记录，点击"同步"按钮
- 观察按钮下方是否显示加载状态
- 检查是否显示成功提示
- 验证该行的客户名称、状态、联系电话、更新时间是否更新
- 验证同步状态是否变为 SUCCESS

**4. 测试异常场景**

- 暂停 Mock 服务，点击同步按钮，验证是否显示友好的错误提示
- 启动 Mock 服务，验证同步功能恢复正常

**5. 检查后端日志**

观察后端日志，验证：
- 请求日志是否包含 customerCode
- 调用外部服务的前后日志是否完整
- 联系电话是否已脱敏

---

## 关键设计说明

### 字段映射规则

| 外部字段 (下划线) | 内部字段 (驼峰) | 处理逻辑 |
|------------------|----------------|---------|
| cust_code | customerCode | 直接映射 |
| cust_name | customerName | 直接映射 |
| cust_status_cd | customerStatus | 直接映射 |
| contact_phone | contactPhone | 脱敏处理 |
| last_modified_date | updatedTime | 格式转换 |

### 安全规范

1. **数据脱敏**：联系电话在后端完成脱敏，保留前3后4
2. **异常处理**：使用 BizException 统一包装业务异常，返回友好提示
3. **日志规范**：关键操作记录日志，包含 customerCode 标识
4. **鉴权要求**：调用外部客户中心必须携带 X-App-Key 请求头

### 前端规范

1. **接口封装**：所有接口调用在 Service 层完成
2. **状态更新**：使用局部状态更新，不刷新整个页面
3. **用户反馈**：使用统一的消息提示组件
4. **防抖处理**：同步期间显示加载状态，防止重复点击

---

**计划完成。选择执行方式：**

1. **Subagent-Driven (本次会话)** - 我在此会话中分派子代理逐个执行任务，并在任务间进行代码审查
2. **Parallel Session (独立会话)** - 打开新会话使用 executing-plans skill 执行，有检查点

选择哪种方式？
