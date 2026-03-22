# backend

这是演练项目的 Java 后端骨架。

当前已提供：

- 启动入口
- 健康检查接口
- 统一返回结构
- 全局异常处理骨架

培训中建议让学员完成以下内容：

- 客户查询请求 DTO
- 客户查询响应 DTO
- 外部客户中心 DTO
- 客户查询 Controller
- 客户查询 Service
- 外部客户中心 Integration Client

## 推荐运行方式

```bash
mvn spring-boot:run
```

## 默认接口

- `GET /api/health`
