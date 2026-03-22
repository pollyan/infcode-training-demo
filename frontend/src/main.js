import { bindCustomerDirectoryPage, renderCustomerDirectoryPage } from "./pages/customer/CustomerDirectoryPage.js";

const app = document.getElementById("app");

function getRoute() {
  return window.location.hash || "#/customers";
}

function renderApp() {
  if (!app) return;

  const route = getRoute();
  if (route === "#/" || route === "#/customers") {
    app.innerHTML = renderCustomerDirectoryPage();
    bindCustomerDirectoryPage(app);
    return;
  }

  app.innerHTML = renderCustomerDirectoryPage();
  bindCustomerDirectoryPage(app);
}

window.addEventListener("hashchange", renderApp);
renderApp();
