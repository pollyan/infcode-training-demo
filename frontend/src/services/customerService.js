import { httpGet, httpPost } from "./http.js";

const API_BASE = "http://127.0.0.1:8080";

export async function fetchCustomerDirectory(filters = {}) {
  const params = new URLSearchParams();

  if (filters.keyword) {
    params.set("keyword", filters.keyword);
  }

  if (filters.customerStatus) {
    params.set("customerStatus", filters.customerStatus);
  }

  if (filters.syncStatus) {
    params.set("syncStatus", filters.syncStatus);
  }

  const queryString = params.toString();
  const response = await httpGet(`${API_BASE}/api/customers${queryString ? `?${queryString}` : ""}`);

  if (!response.success) {
    throw new Error(response.message || "客户列表加载失败");
  }

  return Array.isArray(response.data) ? response.data : [];
}
