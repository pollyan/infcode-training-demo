import { queryCustomerByCode } from "../../services/customerService.js";

export function renderCustomerSearchPage() {
  return `
    <div class="page-shell">
      <header class="top-bar">
        <h1>客户信息查询</h1>
      </header>
      <main class="content">
        <div class="page-actions">
          <a class="link-button" href="#/">返回首页</a>
        </div>
        <section class="card">
          <span class="tag">training/done</span>
          <h2>客户查询练习</h2>
          <p>请输入客户编号，例如 <code>C202503001</code>、<code>C202503002</code> 或 <code>C202503500</code>。</p>
          <form id="customer-search-form" class="search-form">
            <label class="field">
              <span>客户编号</span>
              <input id="customer-code-input" name="customerCode" value="C202503001" placeholder="请输入客户编号">
            </label>
            <button type="submit" class="primary-button">查询</button>
          </form>
        </section>
        <section class="card" id="customer-result-card">
          <h2>查询结果</h2>
          <p class="muted-text">尚未查询，请先输入客户编号并点击查询。</p>
        </section>
      </main>
    </div>
  `;
}

function renderSuccessResult(customer) {
  return `
    <h2>查询结果</h2>
    <div class="result-grid">
      <div class="result-item">
        <span class="result-label">客户编号</span>
        <strong>${customer.customerCode}</strong>
      </div>
      <div class="result-item">
        <span class="result-label">客户名称</span>
        <strong>${customer.customerName}</strong>
      </div>
      <div class="result-item">
        <span class="result-label">客户状态</span>
        <strong>${customer.customerStatus}</strong>
      </div>
      <div class="result-item">
        <span class="result-label">联系电话</span>
        <strong>${customer.contactPhone}</strong>
      </div>
      <div class="result-item">
        <span class="result-label">最近更新时间</span>
        <strong>${customer.updatedTime}</strong>
      </div>
    </div>
  `;
}

function renderErrorResult(message) {
  return `
    <h2>查询结果</h2>
    <div class="result-error">
      <strong>查询失败</strong>
      <p>${message}</p>
    </div>
  `;
}

export function bindCustomerSearchPage(root) {
  const form = root.querySelector("#customer-search-form");
  const input = root.querySelector("#customer-code-input");
  const resultCard = root.querySelector("#customer-result-card");

  if (!form || !input || !resultCard) return;

  form.addEventListener("submit", async (event) => {
    event.preventDefault();

    const customerCode = input.value.trim();
    if (!customerCode) {
      resultCard.innerHTML = renderErrorResult("请输入客户编号后再查询。");
      return;
    }

    resultCard.innerHTML = `
      <h2>查询结果</h2>
      <p class="muted-text">正在查询，请稍候...</p>
    `;

    try {
      const customer = await queryCustomerByCode(customerCode);
      resultCard.innerHTML = renderSuccessResult(customer);
    } catch (error) {
      resultCard.innerHTML = renderErrorResult(error.message || "查询失败，请稍后重试。");
    }
  });
}
