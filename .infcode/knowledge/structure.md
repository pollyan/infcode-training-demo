# 项目结构

## 根目录

```
├── frontend/          # 前端 SPA（纯 JS + CSS，无构建工具）
├── backend/           # Java 后端（Spring Boot 骨架）
├── mock-server/       # 模拟外部客户中心服务（Node.js）
├── docs/              # 业务文档与知识库
├── specs/             # 需求规格说明
├── scripts/           # 启动/检测/停止脚本
├── .infcode/          # InfCode 规则、技能与知识库配置
├── CONTEXT.md         # 领域词汇表（统一术语定义）
├── SETUP.md           # 环境搭建与启动指引
└── README.md          # 项目简介与当前状态说明
```

## frontend/

```
├── index.html             # 入口 HTML
├── server.js              # Node.js 静态文件服务器
├── package.json
└── src/
    ├── main.js            # 路由与页面挂载（hash 路由，#/customers）
    ├── styles.css         # 全局样式
    ├── pages/
    │   └── customer/
    │       └── CustomerDirectoryPage.js   # 客户主数据列表页（渲染+交互）
    └── services/
        ├── http.js                    # HTTP 封装（GET/POST + 错误解析）
        └── customerService.js         # 客户 API 调用（列表查询+同步待补充）
```

## backend/src/main/java/com/example/training/

```
├── TrainingApplication.java        # Spring Boot 启动类 + RestTemplate Bean
├── controller/
│   ├── HealthController.java        # GET /api/health 健康检查
│   └── CustomerDirectoryController.java  # GET /api/customers 列表查询（同步端点待补充）
├── service/
│   └── CustomerDirectoryService.java      # 客户业务逻辑（内存存储+筛选+同步）
├── dto/
│   ├── CommonResponse.java               # 统一返回结构 {success, message, data}
│   ├── CustomerDirectoryItemResponse.java # 客户列表项 DTO（12 字段）
│   ├── ExternalCustomerInfoDTO.java       # 外部数据映射后的内部 DTO（5 字段）
│   └── ExternalCustomerResponse.java      # 外部客户中心原始响应 DTO（含 @JsonProperty 映射）
├── integration/
│   └── ExternalCustomerCenterClient.java  # 外部客户中心 HTTP 客户端（鉴权+错误码映射）
├── exception/
│   ├── BizException.java           # 业务异常（含 userMessage，禁止透传技术细节）
│   └── GlobalExceptionHandler.java # @RestControllerAdvice → CommonResponse.failure
└── resources/
    └── application.properties      # server.port=8080
```

## backend/src/test/

```
└── java/com/example/training/
    ├── dto/ExternalCustomerResponseTest.java
    ├── integration/ExternalCustomerCenterClientTest.java
    ├── service/CustomerDirectoryServiceTest.java
    └── exception/BizExceptionTest.java
```

## mock-server/

```
├── server.js    # 模拟客户中心 API（含成功/熔断/限流/不存在/参数错误等场景）
├── package.json
```

## docs/

```
├── 需求说明.md
├── plans/                          # 实施计划
└── 知识库文档/
    ├── 业务全景与PRD_客户档案同步.md
    ├── 第三方客户中心_API契约.md
    ├── 代码架构与安全规范_v2.md
    └── 前端_UI_组件交互规范.md
```

## scripts/

```
├── check-env.sh    # 检测 Node/npm/Java/Maven/Git 环境
├── install-deps.sh # 安装前端和 mock 依赖
├── start-all.sh    # 一键启动所有服务
├── stop-all.sh     # 一键停止所有服务
```

## .infcode/

```
├── rules/          # 开发规则（前端/后端/集成/全局）
├── skills/         # InfCode 技能定义（TDD/调试/验证/需求评审等）
├── knowledge/      # 项目知识库（product.md / structure.md / tech.md）
├── docs/           # 历史文档
└── plan/           # 实施计划归档
```

## 关键文件说明

- **领域术语定义**：`CONTEXT.md` — 统一术语（客户主数据、同步外部档案、客户中心、同步状态）
- **客户列表 API**：`CustomerDirectoryController.java` — `GET /api/customers` 支持 keyword/customerStatus/syncStatus 筛选
- **同步业务逻辑**：`CustomerDirectoryService.java` — 内存存储、字段映射、电话脱敏、时间格式转换
- **外部集成入口**：`ExternalCustomerCenterClient.java` — RestTemplate + X-App-Key 鉴权 + 错误码映射 → BizException
- **前端列表页**：`CustomerDirectoryPage.js` — 筛选区、统计卡、表格区与行操作按钮（查看/编辑已预留，同步待补充）
- **需求规格**：`specs/requirement-spec-客户档案同步.md` — 同步功能的结构化需求规格说明
