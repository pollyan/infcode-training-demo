# InfCode 基础练习项目

这是一个用于 InfCode 培训演练的基础项目。

它现在被调整成一个更接近真实业务系统的练习项目。

它的定位是：

- 提供一个像存量 CRM 模块的真实工程骨架
- 提供一个可运行的前端与 mock 外部服务
- 提供一个可扩展的 Java 后端骨架
- 把“列表页增量需求”留给培训现场通过 InfCode 完成

## 项目结构

代码库地址：https://github.com/pollyan/infcode-training-demo

```text
02_练习代码库/
  frontend/
  backend/
  mock-server/
  docs/
  scripts/
  .infcode/
```

## 当前状态

### 已具备

- 客户主数据列表页骨架
- 筛选区、统计卡和表格区
- 基础 HTTP 封装与列表查询
- 后端健康检查骨架
- 统一返回结构骨架
- 全局异常处理骨架
- mock 外部客户中心服务
- 训练用文档与规则

### 故意未完成

以下内容建议在 `main` 分支中通过 InfCode 完成：

- 列表页“同步外部档案”行操作
- 前端同步 service 与状态刷新
- 后端同步接口与返回 DTO
- 第三方客户中心集成逻辑
- 同步状态刷新与错误处理补齐

## 推荐启动方式

也可以直接使用根目录脚本：

```bash
./scripts/check-env.sh
./scripts/start-all.sh
```

停止服务：

```bash
./scripts/stop-all.sh
```

## 培训环境建议

如果本代码库用于客户现场培训，建议优先遵循以下原则：

- 尽量使用客户真实网络、真实开发机器、真实 IDE 和真实项目环境
- 讲师提前按客户实际 IDE 和版本做一轮预演
- 对 Java 多模块项目，尽量从项目根目录打开完整工程
- 培训过程除验证功能开发闭环外，也同步记录真实环境中暴露的问题

### 前端

```bash
cd frontend
npm start
```

默认地址：

`http://localhost:5173`

### mock 外部服务

```bash
cd mock-server
npm start
```

默认地址：

`http://localhost:9090`

### 后端

后端提供的是 Java 骨架项目，建议在具备 Java 和 Maven 环境的机器上运行。

```bash
cd backend
mvn spring-boot:run
```

默认地址建议：

`http://localhost:8080`

## 启动脚本

根目录已提供：

- `scripts/check-env.sh`：检测 Node、npm、Java、Maven、Git 和关键项目目录
- `scripts/install-deps.sh`：安装前端和 mock 服务依赖
- `scripts/start-all.sh`：一键检测、安装并启动前端、后端和 mock 服务
- `scripts/stop-all.sh`：停止已启动的服务

## 培训建议

1. 先引用 `docs/需求说明.md` 及 `docs/知识库文档/` 下的业务概念与规范资料
2. 再上传并引用 `.infcode/rules/` 中的规则
3. 如现场有真实需求或接口 `DOCX` 文档，也一并上传
4. 在上下文构建阶段创建或引用知识库，并使用 DeepMap Lite 生成 `Product.md`、`structure.md`、`tech.md`
5. 再让 AI 做需求拆解和实施规划
6. 最后完成“客户主数据列表页里的同步外部档案”需求，并记录现场暴露的问题

## 讲师优先阅读

如果你是讲师，建议优先阅读：

`SETUP.md`
