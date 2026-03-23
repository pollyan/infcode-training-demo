# SETUP

## 1. 环境要求

### 前端与 mock 服务

- Node.js 20+
- npm 10+

### 后端

- Java 17+
- Maven 3.9+

## 2. 启动顺序

建议按以下顺序启动：

1. 启动 mock 外部服务
2. 启动后端服务
3. 启动前端页面

## 3. 推荐直接用脚本

在项目根目录运行：

```bash
./scripts/check-env.sh
./scripts/start-all.sh
```

停止服务：

```bash
./scripts/stop-all.sh
```

## 4. 启动命令

### 4.1 启动 mock 外部服务

```bash
cd mock-server
npm install
npm start
```

启动后访问：

`http://127.0.0.1:9090/health`

### 4.2 启动后端服务

```bash
cd backend
mvn spring-boot:run
```

启动后访问：

`http://127.0.0.1:8080/api/health`

### 4.3 启动前端页面

```bash
cd frontend
npm install
npm start
```

启动后访问：

`http://127.0.0.1:5173`

## 5. 训练建议顺序

### 如果你要做客户演练

建议优先使用 `main` 分支。

```bash
git checkout main
```

它保留了真实列表页骨架，但没有补完“同步外部档案”能力，适合让学员现场完成。

### 如果你要看参考答案

建议使用 `done` 分支。

```bash
git checkout done
```

它包含一份完整参考实现，适合讲师备课、对照或兜底。

## 6. 训练中建议引用的材料

建议全面引用系统业务与架构规范：

- `docs/需求说明.md`
- `docs/知识库文档/业务全景与PRD_客户档案同步.md`
- `docs/知识库文档/第三方客户中心_API契约.md`
- `docs/知识库文档/代码架构与安全规范_v2.md`
- `docs/知识库文档/前端_UI_组件交互规范.md`
- `.infcode/rules/`

然后在上下文构建阶段，通过 DeepMap Lite 自动生成：

- `Product.md`
- `structure.md`
- `tech.md`

## 7. 当前参考实现说明

当前仓库中的参考实现包含：

1. 客户主数据列表页
2. 列表查询与筛选
3. 行操作“同步外部档案”
4. 外部客户中心调用 client
5. 基础字段映射、状态刷新与错误处理

## 8. 培训时推荐演示流程

1. 建立上下文
2. 做需求理解
3. 输出实施计划
4. 先做后端
5. 再做前端
6. 再做测试与排错
7. 最后做 Diff 审核
