import { renderHomePage } from "./pages/home/HomePage.js";

const app = document.getElementById("app");

if (app) {
  app.innerHTML = renderHomePage();
}
