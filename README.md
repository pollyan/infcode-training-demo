# InfCode 基础练习项目

这是一个用于 InfCode 培训演练的基础项目。

它的定位是：

- 提供一个可讲解的真实工程骨架
- 提供一个可运行的前端与 mock 外部服务
- 提供一个可扩展的 Java 后端骨架
- 把“客户信息查询”功能留给培训现场通过 InfCode 完成

## 项目结构

```text
06_基础练习项目/
  frontend/
  backend/
  mock-server/
  docs/
  .infcode/
  SETUP.md
  TRAINER_RUNBOOK.md
  教学对照清单.md
```

## 当前状态

### 已具备

- 前端首页与静态服务
- 基础样式
- 基础 HTTP 封装
- 后端健康检查骨架
- 统一返回结构骨架
- 全局异常处理骨架
- mock 外部客户中心服务
- 训练用文档与规则

### 故意未完成

以下内容建议在培训中通过 InfCode 完成：

- 客户查询页面
- 客户查询前端 service
- 后端客户查询接口
- DTO 定义
- 第三方接口集成逻辑
- 字段映射
- 错误处理补齐

## 推荐启动方式

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

## 培训建议

1. 先引用 `docs/` 中的文档
2. 再引用 `.infcode/rules/` 中的规则
3. 再让 AI 做需求拆解和实施规划
4. 最后完成“客户信息查询”功能

## 讲师优先阅读

如果你是讲师，建议阅读顺序：

1. `SETUP.md`
2. `TRAINER_RUNBOOK.md`
3. `教学对照清单.md`
