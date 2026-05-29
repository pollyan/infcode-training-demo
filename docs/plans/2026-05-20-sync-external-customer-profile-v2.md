# 同步外部档案 实施计划 (V2)

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 在客户主数据列表页每行操作列新增"同步外部档案"按钮，实现从客户中心到本地客户主数据的单向同步，含按钮状态管理、行级局部更新、错误映射和脱敏。

**Architecture:** 后端已具备 Service/Integration/DTO/Exception 完整骨架，本次主要增量是：Controller 层暴露同步 API、修复 Mock 服务字段映射不匹配、前端新增同步按钮与交互逻辑。严格遵循 TDD 原则，先写测试再写实现。

**Tech Stack:** 前端原生 JavaScript (ES Module) + fetch API；后端 Spring Boot 3.3.2 + Java 17 + RestTemplate；Mock 服务 Node.js 20+

---

## 代码现状与差距分析

| 模块 | 已实现 | 缺失/需修复 |
|------|--------|------------|
| 后端 Controller | GET /api/customers 列表 | POST /api/customers/{code}/sync 同步接口 |
| 后端 Service | syncCustomerProfile() 完整逻辑 | ✅ 已完成 |
| 后端 Integration | ExternalCustomerCenterClient 完整 | ✅ 已完成 |
| 后端 DTO/Exception | ExternalCustomerResponse, BizException 等 | ✅ 已完成 |
| 后端 Controller 测试 | 空 | 需新建 CustomerDirectoryControllerTest |
| 前端 Service | fetchCustomerDirectory() | syncCustomerProfile() 方法 |
| 前端 Page | 列表页渲染+筛选 | 同步按钮+按钮状态管理+行级更新+超时处理 |
| 前端 http.js | httpGet/httpPost | ✅ 已完成 |
| Mock 服务 | 基本数据但字段名与 DTO 不匹配 | 需修复字段名 (cust_status_cd, last_modified_date) |

---

## Task 1: 修复 Mock 服务字段映射不匹配

**问题:** mock-server 返回的字段名（`cust_status`, `updated_at`）与 `ExternalCustomerResponse` DTO 的 `@JsonProperty` 映射（`cust_status_cd`, `last_modified_date`）不一致，导致 JSON 反序列化失败。

**Files:**
- Modify: `mock-server/server.js`

**Step 1: 修复 Mock 数据字段名**

将 mock-server 的 `cust_status` → `cust_status_cd`，`updated_at` → `last_modified_date`：

```javascript
const customerData = {
  C202503001: {
    code: 200,
    message: "success",
    data: {
      cust_code: "C202503001",
      cust_name: "上海星河建设有限公司",
      cust_status_cd: "ACTIVE",
      contact_phone: "13800000000",
      last_modified_date: "2026-03-23T10:00:00"
    }
  },
  C202503002: {
    code: 200,
    message: "success",
    data: {
      cust_code: "C202503002",
      cust_name: "杭州云启信息科技有限公司",
      cust_status_cd: "ACTIVE",
      contact_phone: null,
      last_modified_date: "2026-03-22T11:05:00"
    }
  },
  C202503500: {
    code: 5000,
    message: "upstream circuit breaker",
    data: null
  },
  C202503998: {
    code: 200,
    message: "success",
    data: null
  },
  C202504021: {
    code: 200,
    message: "success",
    data: {
      cust_code: "C202504021",
      cust_name: "嘉兴明德劳务服务有限公司",
      cust_status_cd: "ACTIVE",
      contact_phone: "13700001111",
      last_modified_date: "2026-03-19T14:12:00"
    }
  }
};
```

同时修复 HTTP 状态码判断逻辑：业务错误码 4001/4004/5000/5002 应返回 HTTP 200（业务错误在 JSON body 中表示，而非 HTTP 状态码），只有真正找不到路由才返回 404：

```javascript
if (!payload) {
  writeJson(res, 200, {
    code: 4004,
    message: "客户档案不存在",
    data: null
  });
  return;
}

writeJson(res, 200, payload);
```

**Step 2: 新增错误码测试数据**

为测试各种错误场景，添加更多 mock 数据：

```javascript
  C2025034001: {
    code: 4001,
    message: "参数校验失败",
    data: null
  },
  C2025035002: {
    code: 5002,
    message: "请求触发并发限流",
    data: null
  }
```

**Step 3: 验证 Mock 服务启动**

Run: `cd mock-server && npm start`
Expected: 输出 "Mock server running at http://127.0.0.1:9090"

**Step 4: 提交**

```bash
git add mock-server/server.js
git commit -m "fix(mock): 修复字段名映射与HTTP状态码，与DTO对齐"
```

---

## Task 2: 后端 Controller 添加同步 API (TDD)

**Files:**
- Create: `backend/src/test/java/com/example/training/controller/CustomerDirectoryControllerTest.java`
- Modify: `backend/src/main/java/com/example/training/controller/CustomerDirectoryController.java`

**Step 1: 编写 Controller 测试（失败测试）**

```java
package com.example.training.controller;

import com.example.training.dto.CustomerDirectoryItemResponse;
import com.example.training.service.CustomerDirectoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerDirectoryController.class)
@DisplayName("客户目录 Controller 测试")
class CustomerDirectoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerDirectoryService customerDirectoryService;

    @Test
    @DisplayName("同步接口应返回 200 和同步结果")
    void syncCustomerProfile_shouldReturnSyncResult() throws Exception {
        CustomerDirectoryItemResponse response = new CustomerDirectoryItemResponse();
        response.setCustomerCode("C202503001");
        response.setCustomerName("上海星河建设有限公司");
        response.setCustomerStatus("ACTIVE");
        response.setContactPhone("138****8000");
        response.setSyncStatus("SUCCESS");

        when(customerDirectoryService.syncCustomerProfile("C202503001"))
            .thenReturn(response);

        mockMvc.perform(post("/api/customers/C202503001/sync"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.customerCode").value("C202503001"))
            .andExpect(jsonPath("$.data.syncStatus").value("SUCCESS"));
    }

    @Test
    @DisplayName("同步接口-客户不存在时应返回失败响应")
    void syncCustomerProfile_customerNotFound_shouldReturnFailure() throws Exception {
        when(customerDirectoryService.syncCustomerProfile("C999999999"))
            .thenThrow(new com.example.training.exception.BizException("客户不存在"));

        mockMvc.perform(post("/api/customers/C999999999/sync"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("客户不存在"));
    }
}
```

**Step 2: 运行测试确认失败**

Run: `cd backend && mvn test -pl . -Dtest=CustomerDirectoryControllerTest -DfailIfNoTests=false`
Expected: FAIL — Controller 没有 `POST /{customerCode}/sync` 端点

**Step 3: 实现 Controller 同步端点**

在 `CustomerDirectoryController.java` 中添加同步接口：

```java
@PostMapping("/{customerCode}/sync")
public CommonResponse<CustomerDirectoryItemResponse> syncCustomerProfile(
    @PathVariable String customerCode
) {
    return CommonResponse.success(customerDirectoryService.syncCustomerProfile(customerCode));
}
```

**Step 4: 运行测试确认通过**

Run: `cd backend && mvn test -pl . -Dtest=CustomerDirectoryControllerTest -DfailIfNoTests=false`
Expected: PASS

**Step 5: 提交**

```bash
git add backend/src/main/java/com/example/training/controller/CustomerDirectoryController.java
git add backend/src/test/java/com/example/training/controller/CustomerDirectoryControllerTest.java
git commit -m "feat(backend): Controller 层添加同步 API，含 Controller 测试"
```

---

## Task 3: 优化 GlobalExceptionHandler — 区分 BizException

**问题:** 当前 `GlobalExceptionHandler` 只有一个通用 `Exception` handler，BizException 会走通用处理返回 `exception.getMessage()`。但 BizException 携带 `userMessage` 字段应优先使用。同时需确保 BizException 返回 HTTP 200 + `{success: false}`，而非 HTTP 5xx。

**Files:**
- Modify: `backend/src/main/java/com/example/training/exception/GlobalExceptionHandler.java`

**Step 1: 编写 GlobalExceptionHandler 测试（失败测试）**

在 `CustomerDirectoryControllerTest` 中添加一条测试（或新建测试类）验证 BizException 返回 `success=false`：

已在 Task 2 Step 1 中包含此测试用例（`syncCustomerProfile_customerNotFound_shouldReturnFailure`），此处无需额外测试。

**Step 2: 修改 GlobalExceptionHandler**

```java
package com.example.training.exception;

import com.example.training.dto.CommonResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public CommonResponse<Void> handleBizException(BizException exception) {
        return CommonResponse.failure(exception.getUserMessage());
    }

    @ExceptionHandler(Exception.class)
    public CommonResponse<Void> handleException(Exception exception) {
        return CommonResponse.failure(exception.getMessage() == null ? "internal error" : exception.getMessage());
    }
}
```

**Step 3: 运行全部后端测试确认通过**

Run: `cd backend && mvn test`
Expected: 全部 PASS

**Step 4: 提交**

```bash
git add backend/src/main/java/com/example/training/exception/GlobalExceptionHandler.java
git commit -m "feat(backend): GlobalExceptionHandler 区分 BizException 返回友好错误"
```

---

## Task 4: 前端 Service 层新增同步接口方法

**Files:**
- Modify: `frontend/src/services/customerService.js`

**Step 1: 添加 syncCustomerProfile 方法**

```javascript
export async function syncCustomerProfile(customerCode) {
  const response = await httpPost(`${API_BASE}/api/customers/${customerCode}/sync`);

  if (!response.success) {
    throw new Error(response.message || "同步失败，请稍后重试");
  }

  return response.data;
}
```

**Step 2: 提交**

```bash
git add frontend/src/services/customerService.js
git commit -m "feat(frontend): Service 层新增客户档案同步接口方法"
```

---

## Task 5: 前端页面 — 同步按钮与同步状态列

**Files:**
- Modify: `frontend/src/pages/customer/CustomerDirectoryPage.js`

**Step 1: 导入同步方法**

修改文件顶部 import：

```javascript
import { fetchCustomerDirectory, syncCustomerProfile } from "../../services/customerService.js";
```

**Step 2: 表格列头添加"同步状态"列**

修改 `<thead>` 部分，在"更新时间"后、"操作"前插入"同步状态"列头：

```html
<th>更新时间</th>
<th>同步状态</th>
<th>操作</th>
```

同时修改 `<td colspan="8">` 为 `<td colspan="9">`。

**Step 3: renderTableRows 添加同步状态列和同步按钮**

在每行中，更新时间后面添加同步状态列，操作列中添加同步按钮：

```javascript
// 在 updatedTime 的 <td> 后面添加：
<td>
  <span class="status-pill ${getStatusClass(customer.syncStatus)}">${escapeHtml(customer.syncStatus || "PENDING")}</span>
</td>
// 操作列添加同步按钮：
<td>
  <div class="table-actions">
    <button type="button" class="ghost-button" data-action="view" data-code="${escapeHtml(customer.customerCode)}">查看</button>
    <button type="button" class="ghost-button" data-action="edit" data-code="${escapeHtml(customer.customerCode)}">编辑</button>
    <button type="button" class="ghost-button" data-action="sync" data-code="${escapeHtml(customer.customerCode)}">同步外部档案</button>
  </div>
</td>
```

**Step 4: 添加按钮状态管理（syncingStates 映射）**

在 state 对象中新增 `syncingCodes` Set 和 `timeoutCodes` Set：

```javascript
const state = {
  filters: { ...DEFAULT_FILTERS },
  customers: [],
  alertMessage: "",
  alertType: "success",
  loading: false,
  syncingCodes: new Set(),  // 正在同步中的客户编号集合
  timeoutCodes: new Set()   // 超时待重试的客户编号集合
};
```

**Step 5: 修改 renderTableRows 中的同步按钮渲染逻辑**

同步按钮需根据状态动态渲染：

```javascript
// 同步按钮渲染逻辑
function renderSyncButton(customer, syncingCodes, timeoutCodes) {
  const code = customer.customerCode;
  if (syncingCodes.has(code)) {
    return `<button type="button" class="ghost-button" disabled data-action="syncing" data-code="${escapeHtml(code)}">同步中...</button>`;
  }
  if (timeoutCodes.has(code)) {
    return `<button type="button" class="ghost-button" data-action="sync" data-code="${escapeHtml(code)}">重试</button>`;
  }
  return `<button type="button" class="ghost-button" data-action="sync" data-code="${escapeHtml(code)}">同步外部档案</button>`;
}
```

在 renderTableRows 中调用此函数替代硬编码按钮。

**Step 6: 添加同步处理函数**

在 `bindCustomerDirectoryPage` 内添加 `handleSyncCustomer`：

```javascript
const SYNC_TIMEOUT_MS = 5000;

async function handleSyncCustomer(customerCode) {
  // 标记为同步中
  state.syncingCodes.add(customerCode);
  state.timeoutCodes.delete(customerCode);
  state.alertMessage = "";
  renderState();

  let timeoutId;
  const timeoutPromise = new Promise((_, reject) => {
    timeoutId = setTimeout(() => reject(new Error("同步超时，请点击重试")), SYNC_TIMEOUT_MS);
  });

  try {
    const updatedCustomer = await Promise.race([
      syncCustomerProfile(customerCode),
      timeoutPromise
    ]);

    clearTimeout(timeoutId);

    // 行级局部更新
    const index = state.customers.findIndex(c => c.customerCode === customerCode);
    if (index !== -1) {
      state.customers[index] = { ...state.customers[index], ...updatedCustomer };
    }

    state.syncingCodes.delete(customerCode);
    state.timeoutCodes.delete(customerCode);
    state.alertType = "success";
    state.alertMessage = "客户档案同步成功";
    renderState();

  } catch (error) {
    clearTimeout(timeoutId);
    state.syncingCodes.delete(customerCode);

    if (error.message === "同步超时，请点击重试") {
      state.timeoutCodes.add(customerCode);
      // 更新该行同步状态为 FAILED
      const index = state.customers.findIndex(c => c.customerCode === customerCode);
      if (index !== -1) {
        state.customers[index].syncStatus = "FAILED";
      }
    } else {
      state.timeoutCodes.delete(customerCode);
    }

    state.alertType = "error";
    state.alertMessage = error.message || "同步失败，请稍后重试";
    renderState();
  }
}
```

**Step 7: 修改点击事件处理**

在 stateRoot 事件监听中添加 sync action 处理：

```javascript
if (action === "sync") {
  await handleSyncCustomer(customerCode);
  return;
}
if (action === "syncing") {
  return; // 同步中按钮已禁用，忽略点击
}
```

**Step 8: 提交**

```bash
git add frontend/src/pages/customer/CustomerDirectoryPage.js
git commit -m "feat(frontend): 添加同步按钮、状态管理、行级更新与超时重试"
```

---

## Task 6: 前端样式 — 同步按钮与同步状态样式

**Files:**
- Modify: `frontend/src/styles.css`

**Step 1: 添加同步按钮禁用态样式**

已有 `.ghost-button:disabled` 覆盖禁用态，但可以加一个更明确的同步中样式：

```css
.ghost-button[data-action="syncing"] {
  opacity: 0.55;
  cursor: not-allowed;
  color: var(--muted);
}
```

**Step 2: 确认同步状态 pill 已有样式覆盖**

`status-success` / `status-warning` / `status-danger` / `status-muted` 已在 CSS 中定义，分别对应 SUCCESS / PENDING / FAILED / 其他状态，无需额外样式。

**Step 3: 提交**

```bash
git add frontend/src/styles.css
git commit -m "style(frontend): 添加同步按钮禁用态样式"
```

---

## Task 7: 集成验证 — 全链路测试

**Step 1: 启动全部服务**

```bash
./scripts/stop-all.sh && ./scripts/start-all.sh
```

Expected: 三个服务全部启动成功（backend:8080, frontend:5173, mock:9090）

**Step 2: 后端单元测试全量**

Run: `cd backend && mvn test`
Expected: 全部 PASS（含新增 Controller 测试）

**Step 3: 浏览器端到端验证**

打开 http://localhost:5173，执行以下操作：

- 确认每行操作列有"同步外部档案"按钮
- 确认表格新增了"同步状态"列
- 点击 C202503001 的"同步外部档案"按钮 → 按钮 变为"同步中..."（禁用）→ 成功后恢复为"同步外部档案"，行数据更新，顶部显示"客户档案同步成功"
- 点击 C202503500 的"同步外部档案"按钮 → 失败，显示"客户中心服务暂时不可用，请稍后重试"
- 确认联系电话已脱敏（138****8000 格式）
- 确认不存在的客户编号（如 C999999999）同步时返回"客户不存在"

**Step 4: 提交最终版本**

```bash
git add -A
git commit -m "chore: 同步外部档案功能全链路集成验证完成"
```

---

## 关键设计说明

### 字段映射规则（已实现）

| 外部字段 (下划线) | DTO @JsonProperty | 内部 DTO 字段 (驼峰) | 处理逻辑 |
|------------------|-------------------|---------------------|---------|
| cust_code | @JsonProperty("cust_code") | customerCode | 直接映射 |
| cust_name | @JsonProperty("cust_name") | customerName | 直接映射 |
| cust_status_cd | @JsonProperty("cust_status_cd") | customerStatus | 直接映射 |
| contact_phone | @JsonProperty("contact_phone") | contactPhone | 后端脱敏 |
| last_modified_date | @JsonProperty("last_modified_date") | lastModifiedDate → updatedTime | 格式转换 |

### API 接口设计

| 方法 | URL | 请求 | 响应 |
|------|-----|------|------|
| POST | /api/customers/{customerCode}/sync | 无 body | CommonResponse<CustomerDirectoryItemResponse> |

### 按钮状态流转

```
同步外部档案 → [点击] → 同步中...(disabled) → [成功] → 同步外部档案
                                    ↓ [失败]        ↓ [超时]
                              同步外部档案         重试
```

### 错误映射规则

| 外部错误码 | BizException.userMessage | 前端展示 |
|-----------|------------------------|---------|
| 4001 | 参数校验失败，请检查客户编号格式 | 同步失败：请求参数异常 |
| 4004 | 客户档案不存在 | 同步失败：客户档案不存在 |
| 5000 | 服务暂时不可用，请稍后重试 | 客户中心服务暂时不可用，请稍后重试 |
| 5002 | 请求过于频繁，请稍后重试 | 客户中心服务暂时不可用，请稍后重试 |
| 网络/HTTP异常 | 获取客户档案失败，请稍后重试 | 客户中心服务暂时不可用，请稍后重试 |
| 前端超时 | - | 同步超时，请点击重试 |

---

**计划完成。选择执行方式：**

1. **Subagent-Driven (本次会话)** — 我在此会话中分派子代理逐个执行任务，并在任务间进行代码审查
2. **Parallel Session (独立会话)** — 打开新会话使用 executing-plans skill 执行，有检查点

选择哪种方式？