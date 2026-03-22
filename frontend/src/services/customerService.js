import { httpGet } from "./http.js";

const API_BASE = "http://127.0.0.1:8080";

export async function queryCustomerByCode(customerCode) {
  const response = await httpGet(`${API_BASE}/api/customers/query?customerCode=${encodeURIComponent(customerCode)}`);

  if (!response.success) {
    throw new Error(response.message || "查询失败");
  }

  return response.data;
}
