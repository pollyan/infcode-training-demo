# 项目结构

## 根目录结构

```
infcode-training-demo/
├── frontend/              # 前端应用目录
├── backend/               # 后端应用目录
├── mock-server/           # 外部服务模拟器
├── trainer-kit/           # 讲师培训资料
├── scripts/               # 启动和环境检查脚本
├── docs/                  # 项目文档
└── README.md              # 项目说明文档
```

## 前端目录结构 (frontend/)

```
frontend/
├── index.html             # 应用入口 HTML 文件
├── server.js              # 前端开发服务器
├── package.json           # 前端项目配置文件
└── src/                   # 源代码目录
    ├── main.js            # 前端应用主入口
    ├── styles.css         # 全局样式文件
    ├── pages/             # 页面组件目录
    │   └── customer/      # 客户目录模块
    │       └── CustomerDirectoryPage.js
    └── services/          # 服务层目录
        ├── http.js        # HTTP 请求封装
        └── customerService.js  # 客户服务接口
```

**关键说明**：
- `pages/` 目录：存放所有页面组件，按功能模块划分子目录
- `services/` 目录：统一管理后端 API 调用逻辑，页面组件不直接编写请求代码
- `main.js`：负责路由初始化和页面渲染

## 后端目录结构 (backend/)

```
backend/
├── pom.xml                # Maven 项目配置文件
├── README.md              # 后端说明文档
└── src/main/
    ├── java/com/example/training/
    │   ├── TrainingApplication.java  # Spring Boot 应用入口
    │   ├── controller/    # 控制器层
    │   │   ├── HealthController.java
    │   │   └── CustomerDirectoryController.java
    │   ├── service/       # 服务层
    │   │   └── CustomerDirectoryService.java
    │   ├── integration/   # 外部集成层
    │   │   └── CustomerCenterClient.java
    │   ├── dto/           # 数据传输对象
    │   │   ├── CommonResponse.java
    │   │   ├── CustomerDirectoryItemResponse.java
    │   │   └── ExternalCustomerInfoDTO.java
    │   └── exception/     # 异常处理
    │       └── GlobalExceptionHandler.java
    └── resources/
        └── application.properties  # 应用配置文件
```

**分层架构说明**：
- **Controller 层**：处理 HTTP 请求，负责参数校验和响应封装
- **Service 层**：实现业务逻辑，协调各层组件完成功能
- **Integration 层**：封装所有第三方服务调用，处理外部接口交互
- **DTO 层**：定义数据传输对象，实现内外部数据模型的隔离
- **Exception 层**：统一异常处理，返回标准化错误响应

## Mock Server 目录结构 (mock-server/)

```
mock-server/
├── server.js              # Mock 服务器实现
└── package.json           # 项目配置文件
```

**功能说明**：
- 模拟外部客户中心服务，提供客户信息查询接口
- 端口：9090
- 接口路径：`/mock/customer-center/customers/{customerCode}`

## 培训资料目录 (trainer-kit/)

```
trainer-kit/
├── README.md              # 讲师入口文档
├── SETUP.md               # 环境准备指南
├── TRAINER_RUNBOOK.md     # 讲师操作手册
├── 开场10分钟讲稿.md       # 开场讲稿
├── 教学对照清单.md         # 培训对照清单
└── 故障注入说明.md         # 故障注入指南
```

**用途说明**：
- 为讲师提供培训准备、现场教学和课后复盘的完整指导
- 包含环境搭建、教学节奏控制、学员问题处理等内容

## 脚本目录 (scripts/)

```
scripts/
├── start-all.sh           # 一键启动所有服务
├── stop-all.sh            # 停止所有服务
├── check-env.sh           # 环境检查脚本
└── install-deps.sh        # 依赖安装脚本
```

## 文档目录 (docs/)

```
docs/
├── 需求说明.md             # 需求说明文档
└── 外部集成说明.md         # 外部集成说明文档
```

## 关键文件位置

| 文件类型 | 路径 | 说明 |
|---------|------|------|
| 前端入口 | `frontend/src/main.js` | 前端应用启动入口 |
| 后端入口 | `backend/src/main/java/com/example/training/TrainingApplication.java` | Spring Boot 应用入口 |
| 客户目录 API | `backend/src/main/java/com/example/training/controller/CustomerDirectoryController.java` | 客户目录接口 |
| 外部服务集成 | `backend/src/main/java/com/example/training/integration/CustomerCenterClient.java` | 客户中心客户端 |
| 前端客户服务 | `frontend/src/services/customerService.js` | 前端客户查询服务 |
| Mock 服务器 | `mock-server/server.js` | 外部服务模拟器 |
| 应用配置 | `backend/src/main/resources/application.properties` | 后端应用配置 |

## 项目特点

1. **清晰的分层架构**：前后端均采用分层设计，职责明确，易于维护和扩展
2. **模块化组织**：按功能模块划分目录，代码组织清晰
3. **完整的培训体系**：包含讲师资料和学员练习材料，适合教学场景
4. **外部服务隔离**：通过 Integration 层和 Mock Server 实现外部依赖的解耦
