import { bindCustomerSearchPage, renderCustomerSearchPage } from "./pages/customer/CustomerSearchPage.js";
import { renderHomePage } from "./pages/home/HomePage.js";

const app = document.getElementById("app");

function getRoute() {
  return window.location.hash || "#/";
}

function renderApp() {
  if (!app) return;

  const route = getRoute();
  if (route === "#/customer-search") {
    app.innerHTML = renderCustomerSearchPage();
    bindCustomerSearchPage(app);
    return;
  }

  app.innerHTML = renderHomePage();
}

window.addEventListener("hashchange", renderApp);
renderApp();
