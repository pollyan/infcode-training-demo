import { fetchCustomerDirectory, syncCustomerProfile } from "../../services/customerService.js";

const DEFAULT_FILTERS = {
  keyword: "",
  customerStatus: "",
  syncStatus: ""
};

function escapeHtml(value) {
  return String(value ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll("\"", "&quot;")
    .replaceAll("'", "&#39;");
}

function getStatusClass(status) {
  if (status === "ACTIVE") return "status-success";
  if (status === "INACTIVE") return "status-muted";
  if (status === "SUCCESS") return "status-success";
  if (status === "FAILED") return "status-danger";
  if (status === "PENDING") return "status-warning";
  return "status-muted";
}

function renderSummaryCards(customers) {
  const totalCount = customers.length;
  const activeCount = customers.filter((item) => item.customerStatus === "ACTIVE").length;
  const pendingSyncCount = customers.filter((item) => item.syncStatus !== "SUCCESS").length;
  const highRiskCount = customers.filter((item) => item.riskLevel === "高").length;

  return `
    <section class="summary-grid">
      <article class="summary-card summary-card-primary">
        <span class="summary-label">客户总数</span>
        <strong>${totalCount}</strong>
        <span class="summary-hint">当前筛选结果中的客户档案数量</span>
      </article>
      <article class="summary-card">
        <span class="summary-label">启用客户</span>
        <strong>${activeCount}</strong>
        <span class="summary-hint">状态正常，可继续跟进和签约</span>
      </article>
      <article class="summary-card">
        <span class="summary-label">待同步档案</span>
        <strong>${pendingSyncCount}</strong>
        <span class="summary-hint">建议优先处理主数据不同步客户</span>
      </article>
      <article class="summary-card">
        <span class="summary-label">高风险客户</span>
        <strong>${highRiskCount}</strong>
        <span class="summary-hint">涉及冻结、异常或重点跟进状态</span>
      </article>
    </section>
  `;
}

function renderTableRows(customers, syncingCustomerCode) {
  if (!customers.length) {
    return `
      <tr>
        <td colspan="9" class="table-empty">
          当前筛选条件下没有找到客户，请调整筛选条件后重试。
        </td>
      </tr>
    `;
  }

  return customers.map((customer) => {
    const syncButtonText = syncingCustomerCode === customer.customerCode ? "同步中..." : "同步外部档案";
    const syncDisabled = syncingCustomerCode === customer.customerCode ? "disabled" : "";

    return `
      <tr>
        <td>
          <div class="table-main-text">${escapeHtml(customer.customerCode)}</div>
          <div class="table-sub-text">${escapeHtml(customer.customerType)}</div>
        </td>
        <td>
          <div class="table-main-text">${escapeHtml(customer.customerName)}</div>
          <div class="table-sub-text">${escapeHtml(customer.industry)}</div>
        </td>
        <td>${escapeHtml(customer.ownerName)}</td>
        <td>
          <span class="status-pill ${getStatusClass(customer.riskLevel === "高" ? "FAILED" : "SUCCESS")}">${escapeHtml(customer.riskLevel)}</span>
        </td>
        <td>
          <span class="status-pill ${getStatusClass(customer.customerStatus)}">${escapeHtml(customer.customerStatus)}</span>
        </td>
        <td>
          <span class="status-pill ${getStatusClass(customer.syncStatus)}">${escapeHtml(customer.syncStatus)}</span>
          <div class="table-sub-text">${escapeHtml(customer.syncMessage)}</div>
        </td>
        <td>${escapeHtml(customer.contactPhone || "-")}</td>
        <td>
          <div class="table-main-text">${escapeHtml(customer.updatedTime || "-")}</div>
          <div class="table-sub-text">上次同步：${escapeHtml(customer.lastSyncTime || "未同步")}</div>
        </td>
        <td>
          <div class="table-actions">
            <button type="button" class="ghost-button" data-action="view" data-code="${escapeHtml(customer.customerCode)}">查看</button>
            <button type="button" class="ghost-button" data-action="edit" data-code="${escapeHtml(customer.customerCode)}">编辑</button>
            <button type="button" class="primary-button small-button" data-action="sync" data-code="${escapeHtml(customer.customerCode)}" ${syncDisabled}>${syncButtonText}</button>
          </div>
        </td>
      </tr>
    `;
  }).join("");
}

function renderPageState({ customers, syncingCustomerCode, alertMessage, alertType, loading }) {
  const alertHtml = alertMessage
    ? `<div class="notice-banner ${alertType === "error" ? "notice-error" : "notice-success"}">${escapeHtml(alertMessage)}</div>`
    : "";

  const loadingHtml = loading
    ? `<div class="loading-inline">正在加载客户列表，请稍候...</div>`
    : "";

  return `
    ${renderSummaryCards(customers)}
    ${alertHtml}
    ${loadingHtml}
    <section class="table-card">
      <div class="table-toolbar">
        <div>
          <h2>客户档案列表</h2>
          <p>列表页已具备筛选、状态展示和常规操作，本次练习聚焦“同步外部档案”能力。</p>
        </div>
        <div class="toolbar-actions">
          <button type="button" class="ghost-button" disabled>批量分配</button>
          <button type="button" class="ghost-button" disabled>导出名单</button>
          <button type="button" class="primary-button" disabled>新建客户</button>
        </div>
      </div>
      <div class="table-wrapper">
        <table class="data-table">
          <thead>
            <tr>
              <th>客户编号</th>
              <th>客户名称</th>
              <th>负责人</th>
              <th>风险等级</th>
              <th>客户状态</th>
              <th>同步状态</th>
              <th>联系电话</th>
              <th>更新时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>${renderTableRows(customers, syncingCustomerCode)}</tbody>
        </table>
      </div>
    </section>
  `;
}

export function renderCustomerDirectoryPage() {
  return `
    <div class="admin-shell">
      <aside class="sidebar">
        <div class="sidebar-brand">
          <span class="brand-mark">IC</span>
          <div>
            <strong>InfCode CRM</strong>
            <p>客户运营工作台</p>
          </div>
        </div>
        <nav class="sidebar-nav">
          <a class="nav-item" href="#/" data-active="false">运营概览</a>
          <a class="nav-item nav-item-active" href="#/customers">客户主数据</a>
          <span class="nav-item nav-item-disabled">合同台账</span>
          <span class="nav-item nav-item-disabled">回款计划</span>
          <span class="nav-item nav-item-disabled">接口任务</span>
          <span class="nav-item nav-item-disabled">系统设置</span>
        </nav>
        <div class="sidebar-note">
          <span class="tag">training demo</span>
          <p>这是一套用于演练真实增量需求的业务系统骨架，不是从零搭页面的空白工程。</p>
        </div>
      </aside>
      <main class="workspace">
        <header class="workspace-header">
          <div>
            <div class="breadcrumb">客户中心 / 主数据管理 / 客户档案列表</div>
            <h1>客户主数据列表</h1>
            <p>面向销售运营和交付支持团队，用于筛选客户、查看状态，并同步外部客户中心档案。</p>
          </div>
          <div class="header-metrics">
            <div class="metric-chip">
              <span>当前模块</span>
              <strong>客户主数据</strong>
            </div>
            <div class="metric-chip">
              <span>今日同步窗口</span>
              <strong>09:00 - 18:00</strong>
            </div>
          </div>
        </header>

        <section class="filter-card">
          <div class="filter-card-header">
            <div>
              <h2>筛选条件</h2>
              <p>这是一个已有的业务页面骨架，练习需求是在这个页面里增量实现外部档案同步能力。</p>
            </div>
            <div class="scenario-chip">演练重点：列表页增量开发 + 外部集成</div>
          </div>
          <form id="customer-filter-form" class="filter-form">
            <label class="field">
              <span>客户关键字</span>
              <input id="keyword-input" name="keyword" placeholder="客户编号 / 客户名称 / 负责人">
            </label>
            <label class="field">
              <span>客户状态</span>
              <select id="status-select" name="customerStatus">
                <option value="">全部</option>
                <option value="ACTIVE">ACTIVE</option>
                <option value="INACTIVE">INACTIVE</option>
              </select>
            </label>
            <label class="field">
              <span>同步状态</span>
              <select id="sync-select" name="syncStatus">
                <option value="">全部</option>
                <option value="SUCCESS">SUCCESS</option>
                <option value="PENDING">PENDING</option>
                <option value="FAILED">FAILED</option>
              </select>
            </label>
            <div class="filter-actions">
              <button type="submit" class="primary-button">查询</button>
              <button type="button" class="ghost-button" id="reset-filters-button">重置</button>
            </div>
          </form>
        </section>

        <div id="customer-directory-state"></div>
      </main>
    </div>
  `;
}

export function bindCustomerDirectoryPage(root) {
  const stateRoot = root.querySelector("#customer-directory-state");
  const form = root.querySelector("#customer-filter-form");
  const resetButton = root.querySelector("#reset-filters-button");

  const state = {
    filters: { ...DEFAULT_FILTERS },
    customers: [],
    syncingCustomerCode: "",
    alertMessage: "",
    alertType: "success",
    loading: false
  };

  function renderState() {
    if (!stateRoot) return;
    stateRoot.innerHTML = renderPageState(state);
  }

  async function loadCustomers() {
    state.loading = true;
    renderState();

    try {
      state.customers = await fetchCustomerDirectory(state.filters);
    } catch (error) {
      state.alertMessage = error.message || "客户列表加载失败，请稍后重试。";
      state.alertType = "error";
    } finally {
      state.loading = false;
      renderState();
    }
  }

  if (form) {
    form.addEventListener("submit", (event) => {
      event.preventDefault();
      const formData = new FormData(form);
      state.filters = {
        keyword: String(formData.get("keyword") || "").trim(),
        customerStatus: String(formData.get("customerStatus") || "").trim(),
        syncStatus: String(formData.get("syncStatus") || "").trim()
      };
      state.alertMessage = "";
      loadCustomers();
    });
  }

  if (resetButton) {
    resetButton.addEventListener("click", () => {
      if (form) {
        form.reset();
      }
      state.filters = { ...DEFAULT_FILTERS };
      state.alertMessage = "";
      loadCustomers();
    });
  }

  if (stateRoot) {
    stateRoot.addEventListener("click", async (event) => {
      const target = event.target;
      if (!(target instanceof HTMLButtonElement)) return;

      const action = target.dataset.action;
      const customerCode = target.dataset.code;
      if (!action || !customerCode) return;

      if (action === "view" || action === "edit") {
        state.alertType = "success";
        state.alertMessage = `${customerCode} 的${action === "view" ? "查看" : "编辑"}页面已预留，本次演练先聚焦外部档案同步。`;
        renderState();
        return;
      }

      if (action !== "sync") return;

      state.syncingCustomerCode = customerCode;
      state.alertMessage = "";
      renderState();

      try {
        const updatedCustomer = await syncCustomerProfile(customerCode);
        state.customers = state.customers.map((item) => (
          item.customerCode === customerCode ? updatedCustomer : item
        ));
        state.alertType = "success";
        state.alertMessage = `${customerCode} 已完成外部档案同步，列表数据已刷新。`;
      } catch (error) {
        state.alertType = "error";
        state.alertMessage = error.message || "同步失败，请稍后再试。";
      } finally {
        state.syncingCustomerCode = "";
        renderState();
      }
    });
  }

  renderState();
  loadCustomers();
}
