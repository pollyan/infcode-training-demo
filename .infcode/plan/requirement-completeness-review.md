# 需求文档完备性审查：同步外部档案

> 审查对象：`docs/知识库文档/需求说明.md` v2.0
> 对照基准：`specs/requirement-spec-客户档案同步.md`、当前代码库、知识库文档
> 审查日期：2026-05-27

---

## 审查结论：基本完备，有 7 处需补充或澄清

需求说明 v2.0 覆盖了核心业务场景（按钮、状态管理、后端流程、错误映射、脱敏），结构清晰、术语一致。但与详细规格说明（requirement-spec）和代码现状对照后，发现以下缺口：

---

## 缺口清单

### 🔴 缺口 1：表格需新增"同步状态"列，但需求说明未提及

**现状**：FR-5 说"当前行的五个字段值更新为最新值"，其中包含"同步状态"。但当前表格只有 8 列（编号/名称/负责人/风险/状态/电话/时间/操作），**没有同步状态列**。用户无法看到同步状态的初始值和变化过程。

**spec 对照**：requirement-spec FR-001 AC3 只说按钮与现有按钮并列，也未显式要求新增列。但实施计划 Task 5 明确添加了同步状态列。

**建议**：在 FR-1 中补充"表格新增同步状态列，显示每条客户主数据的同步状态（SUCCESS / PENDING / FAILED）"。

---

### 🔴 缺口 2：前端超时后该行同步状态应更新为 FAILED，但需求说明未明确

**现状**：FR-2 说超时后按钮文本变为"重试"，FR-6 说展示"同步超时，请点击重试"提示。但没有说明**超时后该行的同步状态字段应更新为 FAILED**。

**spec 对照**：FR-004 AC3 明确说"当前行同步状态更新为 FAILED"。

**建议**：在 FR-2 或 FR-6 中补充"超时后当前行的同步状态更新为 FAILED"。

---

### 🟡 缺口 3：空值保护规则缺失

**现状**：需求说明未提及当外部接口返回部分字段为 null（如 contact_phone 为 null）时的处理策略。

**PRD 对照**：`业务全景与PRD_客户档案同步.md` 第 4 节明确说"外部接口如果缺失了部分非必填字段，本地落库与展示时不得将其强制覆盖为空白，需做合理的空值保护"。

**代码现状**：mock 数据 C202503002 的 contact_phone 为 null，当前 Service 的 syncCustomerProfile 会将 null 传入 maskPhoneNumber，返回 null，然后 customer.setContactPhone(null) 把原有值（"-"）覆盖为 null。

**建议**：新增 FR-8 或补充到 FR-3 中："当外部字段值为 null 或缺失时，不得覆盖本地已有值，保留原值不变"。

---

### 🟡 缺口 4：同步操作的"异步 vs 同步等待"语义矛盾

**现状**：需求说明 FR-4/FR-5 描述的是**客户端等待结果**模式（行级局部更新、5 秒超时），但 requirement-spec FR-005 AC5 说"后端同步操作为异步执行，一旦接收请求即承诺完成，不受客户端连接状态影响"。

**矛盾**：如果后端是异步的，前端如何做到行级局部更新？如果前端等待后端返回结果，那就不是异步。

**实际代码**：当前 Service 的 syncCustomerProfile 是**同步阻塞**方法——调用外部 API、更新数据、返回结果。Controller 也是同步返回。

**建议**：在需求说明中明确"本需求采用同步等待模式：前端发起请求后等待后端返回结果（最长 5 秒），后端同步处理并返回更新后的客户数据"。消除"异步执行"的歧义。requirement-spec 中的 AC5 应删除或修改。

---

### 🟡 缺口 5：成功/失败提示的展示形式与消失时机未明确

**现状**：FR-5 说"页面顶部展示提示消息"，FR-6 说"展示错误提示"。但没有说明：
- 提示是 banner 形式还是 toast/popover？
- 提示是否自动消失？消失时间？
- 多次同步时提示是否会叠加？

**代码现状**：前端当前使用 `notice-banner` 展示提示，不自动消失，新提示会替换旧提示。

**UI 规范对照**：`前端_UI_组件交互规范.md` 要求"禁用原生弹窗"、"统筹提示分发"，但项目无 UI 框架，无现成 Message 组件。

**建议**：补充"提示以页面顶部 banner 形式展示，新提示替换旧提示，不自动消失，用户下次操作时清除"。

---

### 🟡 缺口 6：API 接口路径和 HTTP 方法未明确

**现状**：需求说明说"调用后端同步接口"，但未定义接口路径和 HTTP 方法。

**spec/实施计划**：接口为 `POST /api/customers/{customerCode}/sync`，无请求 body。

**建议**：在 FR-3 中补充 API 规格："后端暴露 POST /api/customers/{customerCode}/sync 接口，路径参数为客户编号，无请求 body，返回 CommonResponse<CustomerDirectoryItemResponse>"。

---

### 🟡 缺口 7：BizException.userMessage 到前端展示的两层映射未明确

**现状**：需求说明 FR-6 的错误映射表直接列出了"外部场景 → 前端展示"，看起来是一步映射。但实际是两层：
- 第一层：外部错误码 → BizException.userMessage（后端映射）
- 第二层：BizException.userMessage → 前端展示文案（前端展示）

**代码现状**：ExternalCustomerCenterClient 将 4004 映射为 `BizException("客户档案不存在")`，前端收到 `message: "客户档案不存在"` 后展示 `"同步失败：客户档案不存在"`——前端加了"同步失败："前缀。

**建议**：在 FR-6 中补充说明两层映射机制，明确后端 BizException.userMessage 的具体文案与前端展示文案的区别（前端统一加"同步失败："或"客户中心服务暂时不可用，"前缀）。

---

## 已完备的方面（确认无缺口）

| 方面 | 评估 |
|------|------|
| 术语一致性 | ✅ 与 CONTEXT.md 完全一致，无 Avoid 词汇 |
| 按钮状态流转 | ✅ 默认→同步中→成功恢复 / 失败恢复 / 超时→重试 |
| 行级局部更新 | ✅ 明确禁止整页重载 |
| 联系电话脱敏规则 | ✅ 前3后4中间星号，后端脱敏 |
| 外部字段禁止透传 | ✅ 明确要求映射为内部 DTO |
| 错误禁止透传 | ✅ 详细错误映射表 |
| 明确排除项 | ✅ 批量同步/权限/定时/历史/WebSocket/新页面 |
| 非功能需求指标 | ✅ P95/P99/超时阈值/局部更新/脱敏 |
| 鉴权要求 | ✅ X-App-Key: xingchen-crm-local |

---

## 潜在风险（非需求文档问题，但影响实施）

### ⚠️ Spring Security 默认拦截

pom.xml 引入了 `spring-boot-starter-security`，但无自定义 SecurityConfig。Spring Boot 3.3.2 默认会拦截所有请求（返回 401）。需求明确说"无需权限验证"，但代码现状会导致所有 API 被拦截。**需在实施前添加 SecurityConfig 放行所有请求**。

### ⚠️ GlobalExceptionHandler 缺少 BizException 专属 handler

当前只有一个通用 `Exception.class` handler，BizException 走通用处理返回 `exception.getMessage()`。虽然 BizException 的 getMessage() 和 userMessage 相同（构造函数中 `super(userMessage)`），但语义上应优先使用 `getUserMessage()`，且需确保 BizException 返回 HTTP 200 + `{success: false}` 而非 HTTP 5xx。

---

## Task Breakdown Checklist

- [ ] 在 FR-1 中补充"表格新增同步状态列"
- [ ] 在 FR-2 或 FR-6 中补充"超时后当前行同步状态更新为 FAILED"
- [ ] 新增 FR-8（空值保护）：外部字段为 null 时不得覆盖本地已有值
- [ ] 在 FR-3 或新增段落中明确"采用同步等待模式"，消除异步歧义
- [ ] 补充提示的展示形式（banner）和消失时机（不自动消失，新提示替换旧提示）
- [ ] 在 FR-3 中补充 API 规格（POST /api/customers/{customerCode}/sync）
- [ ] 在 FR-6 中补充两层错误映射机制说明（后端 userMessage vs 前端展示文案）
- [ ] （非需求文档）添加 SecurityConfig 放行所有请求
- [ ] （非需求文档）GlobalExceptionHandler 添加 BizException 专属 handler