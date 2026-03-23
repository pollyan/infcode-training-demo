# 星辰集团统一客户中心 API 契约协议

## 1. 接口基础信息
- **接口名称**：按客户编号查询企业客户全量档案
- **通信协议**：HTTP / RESTful
- **Mock 服务地址**：`GET http://localhost:9090/mock/customer-center/customers/{customerCode}`
- **鉴权要求**：所有向客户中心发起的请求，必须在 HTTP Header 中携带身份标识：`X-App-Key: xingchen-crm-local` （缺失该请求头会导致认证失败）。

## 2. 响应报文结构规范
外部客户中心接口的数据结构偏向历史遗留系统的下划线命名风格。所有本系统的集成服务，**禁止将外部报文结构直接透传给前端**，必须做一层内部数据传输对象（DTO）映射的隔离。

### 2.1 成功响应 JSON 示例
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "cust_code": "C202503001",
    "cust_name": "北京星辰科技有限公司",
    "cust_status_cd": "ACTIVE",
    "contact_phone": "13800138000",
    "last_modified_date": "2026-03-23T10:00:00Z"
  }
}
```

### 2.2 常见错误码 (Error Codes) 对照表
集成方（后端）需要针对不同的业务错误做捕获与统一包装：
- `200`: 请求成功且正常（注：如果是 200，但 `data` 节点为 `null`，不代表业务成功，需视为档案不存在或特殊异常处理）。
- `4001`: 参数校验失败（非法格式）。
- `4004`: 客户档案不存在。
- `5000`: 上游核心链路熔断，服务不可用。
- `5002`: 请求触发并发限流 (Rate Limit Exceeded)。
