# 登录功能实施计划

## 1. 目标与范围

### 目标
为当前 CRM 客户档案管理系统添加完整的登录认证功能，实现用户身份验证和会话管理。

### 范围
- 后端：实现登录认证接口、JWT Token 生成与验证、请求拦截
- 前端：实现登录页面、Token 存储与自动携带、未登录路由拦截
- 安全：密码加密存储、Token 过期处理、接口鉴权

### 不包含
- 注册功能
- 找回密码功能
- 权限管理（RBAC）
- 多因素认证（MFA）
- 用户管理界面

## 2. 关键假设与待确认问题

### 已明确的假设
1. 使用 JWT（JSON Web Token）作为认证机制
2. Token 有效期为 24 小时
3. 用户数据暂时存储在内存中（数据库集成留待后续）
4. 登录成功后跳转到客户列表页
5. 前端使用 localStorage 存储 Token

### 待确认问题
- [ ] 初始管理员账号密码（建议：admin / admin123）
- [ ] 是否需要"记住我"功能（延长 Token 有效期）
- [ ] Token 过期后的具体处理策略（跳转登录页还是弹窗提示）
- [ ] 是否需要前端密码强度校验

## 3. 实施方案

### 3.1 技术选型与架构

**后端技术栈**
- Spring Security 6（处理认证和授权）
- jjwt 库（JWT Token 生成与验证）
- BCrypt（密码加密）

**前端技术栈**
- 原生 JS 实现登录表单
- localStorage 存储 Token
- HTTP 请求拦截器自动携带 Token

**认证流程**
1. 用户输入用户名和密码
2. 前端调用后端 `/api/auth/login` 接口
3. 后端验证用户名密码，生成 JWT Token
4. 前端接收 Token 并存储到 localStorage
5. 后续请求自动在 Header 中携带 Token
6. 后端验证 Token 有效性，通过则处理请求

### 3.2 核心设计权衡

**权衡 1：Session vs JWT**
- 选择 JWT：无状态，适合前后端分离，支持横向扩展
- 放弃 Session：避免服务端状态管理，简化架构

**权衡 2：内存存储 vs 数据库**
- 选择内存存储：快速实现，适合培训演示
- 放弃数据库：避免配置复杂度，数据持久化留待后续

**权衡 3：全局拦截 vs 每个接口鉴权**
- 选择 Spring Security 全局拦截：统一处理，安全性高
- 放弃手动拦截：避免遗漏，减少维护成本

## 4. 关键文件与变更点

### 4.1 后端变更文件

**新增文件**
```
backend/src
  └── main
    ├── java/com/example/training
    │   ├── config
    │   │   └── SecurityConfig.java                    # Spring Security 配置
    │   ├── controller
    │   │   └── AuthController.java                     # 登录接口
    │   ├── dto
    │   │   ├── LoginRequest.java                      # 登录请求 DTO
    │   │   ├── LoginResponse.java                     # 登录响应 DTO
    │   │   └── JwtTokenUtil.java                      # JWT 工具类
    │   ├── service
    │   │   └── AuthService.java                       # 认证服务
    │   └── model
    │       └── User.java                              # 用户模型
    └── resources
        └── application.properties                    # 添加 JWT 配置
```

**修改文件**
```
backend/pom.xml                                       # 添加 JWT 和 Spring Security 依赖
```

### 4.2 前端变更文件

**新增文件**
```
frontend/src
  ├── pages
  │   └── auth
  │       └── LoginPage.js                            # 登录页面
  ├── services
  │   └── authService.js                             # 登录服务
  └── utils
      └── authUtils.js                                # Token 存储与工具函数
```

**修改文件**
```
frontend/src/main.js                                  # 添加路由守卫和登录页面路由
frontend/src/services/http.js                        # 添加请求拦截器自动携带 Token
frontend/src/styles.css                              # 添加登录页面样式
```

## 5. 验证方案

### 5.1 功能验证
- [ ] 使用正确的账号密码登录成功
- [ ] 登录成功后跳转到客户列表页
- [ ] Token 正确存储在 localStorage
- [ ] 访问客户列表接口自动携带 Token
- [ ] 使用错误的账号密码登录失败
- [ ] 未登录访问需要认证的接口返回 401
- [ ] Token 过期后自动跳转登录页

### 5.2 安全验证
- [ ] 密码在传输前未明文存储（前端）
- [ ] 密码在数据库中 BCrypt 加密（后端）
- [ ] Token 包含正确的用户信息
- [ ] Token 有效期设置为 24 小时
- [ ] Token 签名验证失败被拦截

### 5.3 用户体验验证
- [ ] 登录表单布局美观
- [ ] 登录失败有友好提示
- [ ] 登录中状态有 Loading 提示
- [ ] 输入框有基本校验（非空）
- [ ] Token 过期时提示用户需要重新登录

## 6. 任务分解清单

### 6.1 后端实施任务

- [ ] **任务 1：添加必要的 Maven 依赖**
  - 修改 `backend/pom.xml`，添加 Spring Security 和 jjwt 依赖

- [ ] **任务 2：创建用户模型**
  - 创建 `backend/src/main/java/com/example/training/model/User.java`
  - 定义用户名、密码字段

- [ ] **任务 3：创建登录相关 DTO**
  - 创建 `backend/src/main/java/com/example/training/dto/LoginRequest.java`
  - 创建 `backend/src/main/java/com/example/training/dto/LoginResponse.java`

- [ ] **任务 4：实现 JWT 工具类**
  - 创建 `backend/src/main/java/com/example/training/dto/JwtTokenUtil.java`
  - 实现 Token 生成方法
  - 实现 Token 验证方法
  - 实现从 Token 提取用户信息方法

- [ ] **任务 5：实现认证服务**
  - 创建 `backend/src/main/java/com/example/training/service/AuthService.java`
  - 实现用户名密码验证逻辑
  - 集成内存用户存储
  - �BCrypt 密码加密

- [ ] **任务 6：创建登录控制器**
  - 创建 `backend/src/main/java/com/example/training/controller/AuthController.java`
  - 实现 `/api/auth/login` 接口
  - 调用 AuthService 处理登录逻辑
  - 返回 JWT Token

- [ ] **任务 7：配置 Spring Security**
  - 创建 `backend/src/main/java/com/example/training/config/SecurityConfig.java`
  - 配置 JWT 认证过滤器
  - 配置路径权限（登录接口放行，其他接口需要认证）
  - 禁用 CSRF（前后端分离项目）

- [ ] **任务 8：添加配置项**
  - 修改 `backend/src/main/resources/application.properties`
  - 添加 JWT Secret 和过期时间配置

### 6.2 前端实施任务

- [ ] **任务 9：创建认证工具函数**
  - 创建 `frontend/src/utils/authUtils.js`
  - 实现 Token 存储方法（localStorage）
  - 实现 Token 读取方法
  - 实现 Token 删除方法
  - 实现 Token 是否存在判断方法

- [ ] **任务 10：创建登录服务**
  - 创建 `frontend/src/services/authService.js`
  - 实现登录接口调用
  - 集成 authUtils 存储 Token

- [ ] **任务 11：创建登录页面**
  - 创建 `frontend/src/pages/auth/LoginPage.js`
  - 实现登录表单（用户名、密码输入框）
  - 实现表单提交逻辑

  - 实现登录成功后跳转
  - 实现登录失败错误提示
  - 绑定登录按钮点击事件

- [ ] **任务 12：修改 HTTP 工具类**
  - 修改 `frontend/src/services/http.js`
  - 添加请求拦截器，从 localStorage 读取 Token
  - 在请求 Header 中添加 Authorization: Bearer {token}

- [ ] **任务 13：添加登录页面样式**
  - 修改 `frontend/src/styles.css`
  - 添加登录表单样式
  - 添加输入框和按钮样式

- [ ] **任务 14：修改路由逻辑**
  - 修改 `frontend/src/main.js`
  - 添加登录页面路由（#/login）
  - 添加路由守卫，检查 Token 是否存在
  - 未登录时自动跳转到登录页

### 6.3 集成测试任务

- [ ] **任务 15：验证登录流程**
  - 访问 http://127.0.0.1:5173，自动跳转到登录页
  - 使用正确账号密码登录
  - 验证是否跳转到客户列表页
  - 验证客户列表数据是否正常加载

- [ ] **任务 16：验证 Token 携带**
  - 登录后打开浏览器开发者工具
  - 查看 Network 面板中的请求 Header
  - 确认 Authorization Header 包含 Token

- [ ] **任务 17：验证错误处理**
  - 使用错误密码登录，验证错误提示
  - 清除 localStorage，验证是否跳转登录页
  - Token 过期后验证处理逻辑

- [ ] **任务 18：验证安全性**
  - 检查后端日志确认密码未明文打印
  - 验证 JWT Token 签名验证正确
  - 验证未认证访问接口返回 401

### 6.4 文档与清理任务

- [ ] **任务 19：更新项目文档**
  - 在 `README.md` 中添加登录功能说明
  - 记录默认管理员账号密码

- [ ] **任务 20：代码审查与优化**
  - 检查代码是否符合现有架构规范
  - 确认异常处理统一
  - 确认日志记录完整
