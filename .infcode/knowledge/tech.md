# 核心技术

| 层 | 技术 | 说明 |
|---|---|---|
| 前端 | 原生 JavaScript (ES Module) | 无框架，纯 JS 模板渲染 + 事件绑定 |
| 前端服务器 | Node.js http 模块 | 自建静态文件服务器（端口 5173），无 Vite/Webpack |
| 后端 | Java 17 + Spring Boot 3.3.2 | REST API 骨架，内嵌 Tomcat |
| HTTP 客户端 | Spring RestTemplate | 调用外部客户中心，由 TrainingApplication 提供 Bean |
| 安全 | Spring Security + JWT (jjwt 0.11.5) | 已引入但当前未深度配置 |
| Mock 服务 | Node.js http 模块 | 模拟客户中心 API（端口 9090），覆盖成功/熔断/限流/参数错误/不存在等场景 |
| 测试 | JUnit 5 + Mockito + AssertJ | 后端单元测试骨架（4 个测试类） |

# 项目使用的工具

- **构建工具**：Maven 3.9+（后端）；前端和 mock 无构建工具，直接用 Node 运行
- **包管理**：npm（前端/mock 依赖管理，当前无额外依赖）
- **运行时**：Node.js 20+（前端/mock）、Java 17+（后端）
- **脚本工具**：Shell 脚本（`scripts/` 下的 check-env/install-deps/start-all/stop-all）

# 关键依赖

## 后端（pom.xml）

| 依赖 | 版本 | 用途 |
|---|---|---|
| spring-boot-starter-web | 3.3.2 | REST API 框架 |
| spring-boot-starter-security | 3.3.2 | 安全框架（当前未深度配置） |
| jjwt-api / jjwt-impl / jjwt-jackson | 0.11.5 | JWT 令牌处理 |
| spring-boot-starter-test | 3.3.2 | 测试框架（含 JUnit 5） |
| junit-jupiter-api / junit-jupiter-engine | Spring Boot 管理 | JUnit 5 测试 |
| mockito-core | Spring Boot 管理 | Mock 测试 |
| assertj-core | Spring Boot 管理 | 断言库 |

## 前端 / Mock 服务

无第三方依赖，仅使用 Node.js 内置模块（`http`、`fs`、`path`、`url`）。

# 构建、开发与执行命令

## 启动（推荐脚本方式）

```bash
# 环境检测
./scripts/check-env.sh

# 安装依赖并启动所有服务
./scripts/start-all.sh

# 停止所有服务
./scripts/stop-all.sh
```

## 单独启动

| 服务 | 命令 | 地址 |
|---|---|---|
| Mock 客户中心 | `cd mock-server && npm start` | http://127.0.0.1:9090 |
| 后端 | `cd backend && mvn spring-boot:run` | http://127.0.0.1:8080 |
| 前端 | `cd frontend && npm start` | http://127.0.0.1:5173 |

## 构建

```bash
cd backend && mvn package
# 前端/mock 无构建步骤，直接运行源码
```

## 测试

```bash
cd backend && mvn test    # 后端单元测试
# 前端/mock 当前无自动化测试
```

# 配置与规范

- **后端端口**：`server.port=8080`（application.properties）
- **外部服务地址**：`external.customer-center.url` 默认 `http://localhost:9090/mock/customer-center`（可通过 application.properties 覆盖）
- **鉴权头**：`X-App-Key: xingchen-crm-local`（硬编码在 ExternalCustomerCenterClient）
- **统一返回结构**：`CommonResponse<T>` — `{ success, message, data }`；成功时 success=true/message="success"，失败时 success=false/message=错误提示
- **异常处理**：所有异常经 `GlobalExceptionHandler` → `CommonResponse.failure`；业务异常用 `BizException`（含 `userMessage`，禁止透传技术细节）
- **外部字段映射**：`cust_code→customerCode`、`cust_name→customerName`、`cust_status_cd→customerStatus`、`contact_phone→contactPhone`、`last_modified_date→updatedTime`
- **数据脱敏**：`maskPhoneNumber` 保留前3后4，中间4位用星号屏蔽（如 138****5678），短于7位不脱敏
- **时间格式**：外部 ISO_LOCAL_DATE_TIME → 内部 `yyyy-MM-dd HH:mm:ss`
- **前端路由**：基于 hash（`#/customers`），无路由库
- **编码分层**：后端 Controller → Service → DTO → Integration；前端页面不写请求逻辑，接口调用统一在 services 层
- **CORS 配置**：后端 Controller 允许 `http://127.0.0.1:5173` 和 `http://localhost:5173`
