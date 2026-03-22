# 技术栈

## 核心技术

### 前端技术栈
- **原生 JavaScript (ES6+)**：使用现代 JavaScript 特性，采用 ES Module 模块化
- **HTML5 + CSS3**：标准 Web 技术构建用户界面
- **Hash 路由**：基于 URL Hash 实现单页应用路由导航
- **Fetch API**：原生 HTTP 请求接口，用于与后端通信

### 后端技术栈
- **Java 17**：使用 LTS 版本的 Java 语言
- **Spring Boot 3.3.2**：基于 Spring Boot 框架构建 RESTful API
- **Spring Web**：提供 Web MVC 功能和 HTTP 服务支持
- **Maven**：项目构建和依赖管理工具

### Mock 服务技术
- **Node.js**：JavaScript 运行时环境
- **原生 HTTP 模块**：使用 Node.js 内置 http 模块实现 Mock 服务器

## 项目使用的工具

### 开发工具
- **Maven**：后端项目构建、依赖管理和打包
- **Node.js**：前端开发服务器和 Mock 服务器运行环境
- **Git**：版本控制系统

### 服务器工具
- **前端开发服务器**：基于 Node.js http 模块实现的静态文件服务器
- **Spring Boot 内置 Tomcat**：后端应用服务器

## 关键依赖

### 后端依赖 (pom.xml)
```xml
<dependencies>
    <!-- Spring Boot Web Starter：提供 Web MVC、RESTful API、内嵌 Tomcat 等功能 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
</dependencies>
```

**依赖说明**：
- `spring-boot-starter-web`：Spring Boot Web 开发核心依赖，包含：
  - Spring MVC：Web 框架
  - Jackson：JSON 序列化/反序列化
  - Tomcat：内嵌 Web 服务器
  - Validation：参数校验

### 前端依赖 (package.json)
```json
{
  "name": "infcode-training-frontend",
  "version": "1.0.0",
  "type": "module"
}
```

**说明**：
- 前端项目采用原生 JavaScript，无第三方依赖
- `"type": "module"`：启用 ES Module 支持

### Mock Server 依赖
- 使用 Node.js 原生模块，无第三方依赖

## 构建、开发和运行命令

### 前端命令

#### 启动前端开发服务器
```bash
cd frontend
npm start
```
- **端口**：5173
- **访问地址**：http://localhost:5173
- **功能**：提供静态文件服务，支持前端页面访问

### 后端命令

#### 编译和运行后端服务
```bash
cd backend
mvn spring-boot:run
```
- **端口**：8080
- **API 基础路径**：http://localhost:8080
- **健康检查接口**：http://localhost:8080/health

#### 打包后端应用
```bash
cd backend
mvn clean package
```
- **输出**：`target/infcode-training-backend-1.0.0.jar`

#### 运行打包后的应用
```bash
java -jar target/infcode-training-backend-1.0.0.jar
```

### Mock Server 命令

#### 启动 Mock 服务器
```bash
cd mock-server
node server.js
```
- **端口**：9090
- **API 路径**：http://localhost:9090/mock/customer-center/customers/{customerCode}
- **功能**：模拟外部客户中心服务

### 一键启动脚本
```bash
# 启动所有服务
./scripts/start-all.sh

# 停止所有服务
./scripts/stop-all.sh

# 检查环境
./scripts/check-env.sh

# 安装依赖
./scripts/install-deps.sh
```

### 完整启动顺序
1. 启动 Mock Server（端口 9090）
2. 启动后端服务（端口 8080）
3. 启动前端服务（端口 5173）

## 配置文件分析

### 后端配置 (application.properties)
```properties
server.port=8080
```
- **说明**：配置 Spring Boot 应用监听端口为 8080

### 前端服务器配置 (server.js)
- **端口**：5173（硬编码）
- **静态文件目录**：当前目录
- **支持文件类型**：HTML、CSS、JavaScript

### Mock Server 配置 (server.js)
- **端口**：9090（硬编码）
- **模拟数据**：内置客户数据对象
- **支持客户编码**：C202503001、C202503002、C202503500、C202503998、C202504021

## 编码规范

### 前端编码规范
- 新增页面放在 `src/pages/` 目录
- 页面不直接编写复杂请求逻辑
- 接口调用统一放在 `src/services/` 目录
- 查询失败时展示用户可理解的提示信息

### 后端编码规范
- 遵循 Controller、Service、DTO、Integration 分层架构
- 不在 Controller 中直接调用外部接口
- 外部字段需映射为内部 DTO 对象
- 发生异常时返回统一错误结构

### 集成开发规范
- 所有第三方调用统一放在 `integration` 目录
- 做好超时、失败和字段缺失处理
- 不允许将第三方原始错误直接透传到前端

## 架构特点

1. **前后端分离**：前端和后端独立部署，通过 RESTful API 通信
2. **分层架构**：后端采用 Controller-Service-Integration 三层架构
3. **依赖隔离**：通过 Mock Server 隔离外部服务依赖，便于开发和测试
4. **轻量级技术栈**：前端使用原生 JavaScript，后端仅依赖 Spring Boot Web，学习成本低
5. **教学友好**：项目结构清晰，代码简洁，适合培训和演示场景
