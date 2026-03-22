export function renderHomePage() {
  return `
    <div class="page-shell">
      <header class="top-bar">
        <h1>InfCode 基础练习项目</h1>
      </header>
      <main class="content">
        <span class="tag">training/base</span>
        <h2>当前状态：基础骨架已就绪，客户查询功能待实现</h2>
        <div class="card-grid">
          <section class="card">
            <h2>这次练习会做什么</h2>
            <ul>
              <li>新增客户信息查询页面</li>
              <li>新增前端查询 service</li>
              <li>新增后端客户查询接口</li>
              <li>接入 mock 外部客户中心服务</li>
              <li>完成字段映射、错误处理和验证</li>
            </ul>
          </section>
          <section class="card">
            <h2>推荐训练流程</h2>
            <ol>
              <li>先引用 docs 与规则文件</li>
              <li>先做需求理解和实施规划</li>
              <li>先完成后端，再补前端</li>
              <li>最后做测试、排错和 Diff 审核</li>
            </ol>
          </section>
          <section class="card">
            <h2>前端待补内容</h2>
            <p>已提供客户查询页面和查询 service 的参考实现，可直接作为讲师标准答案。</p>
            <p><a class="link-button" href="#/customer-search">打开客户信息查询页</a></p>
          </section>
          <section class="card">
            <h2>后端待补内容</h2>
            <div class="code-block">controller/CustomerQueryController.java
service/CustomerQueryService.java
dto/CustomerQueryRequest.java
dto/CustomerQueryResponse.java
dto/ExternalCustomerInfoDTO.java
integration/CustomerCenterClient.java</div>
          </section>
        </div>
      </main>
    </div>
  `;
}
