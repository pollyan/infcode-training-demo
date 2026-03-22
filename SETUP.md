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

## 3. 启动命令

### 3.1 启动 mock 外部服务

```bash
cd mock-server
npm install
npm start
```

启动后访问：

`http://127.0.0.1:9090/health`

### 3.2 启动后端服务

```bash
cd backend
mvn spring-boot:run
```

启动后访问：

`http://127.0.0.1:8080/api/health`

### 3.3 启动前端页面

```bash
cd frontend
npm install
npm start
```

启动后访问：

`http://127.0.0.1:5173`

## 4. 训练建议顺序

### 如果你要做客户演练

建议优先使用 `training/base` 分支。

```bash
git checkout training/base
```

它保留了基础骨架，但没有补完客户查询功能，适合让学员现场完成。

### 如果你要看参考答案

建议使用 `training/done` 分支。

```bash
git checkout training/done
```

它包含一份完整参考实现，适合讲师备课、对照或兜底。

## 5. 训练中建议引用的材料

优先引用：

- `docs/Product.md`
- `docs/structure.md`
- `docs/tech.md`
- `docs/需求说明.md`
- `docs/外部集成说明.md`
- `.infcode/rules/`

## 6. 当前参考实现说明

当前仓库中的参考实现包含：

1. 前端客户查询页面
2. 前端查询 service
3. 后端客户查询接口
4. 外部客户中心调用 client
5. 基础字段映射与错误处理

## 7. 培训时推荐演示流程

1. 建立上下文
2. 做需求理解
3. 输出实施计划
4. 先做后端
5. 再做前端
6. 再做测试与排错
7. 最后做 Diff 审核
