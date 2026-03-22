import http from "node:http";

const port = 9090;
const host = "127.0.0.1";

const customerData = {
  C202503001: {
    code: "200",
    message: "success",
    data: {
      cust_code: "C202503001",
      cust_name: "上海星河建设有限公司",
      cust_status: "ACTIVE",
      contact_phone: "13800000000",
      updated_at: "2026-03-22 10:30:00"
    }
  },
  C202503002: {
    code: "200",
    message: "success",
    data: {
      cust_code: "C202503002",
      cust_name: "杭州云启信息科技有限公司",
      cust_status: "ACTIVE",
      updated_at: "2026-03-22 11:05:00"
    }
  },
  C202503500: {
    code: "500",
    message: "upstream service unavailable",
    data: null
  }
};

function writeJson(res, statusCode, payload) {
  res.writeHead(statusCode, { "Content-Type": "application/json; charset=utf-8" });
  res.end(JSON.stringify(payload));
}

const server = http.createServer((req, res) => {
  const { method, url } = req;

  if (method === "GET" && url?.startsWith("/mock/customer-center/customers/")) {
    const customerCode = url.split("/").pop();
    const payload = customerData[customerCode];

    if (!payload) {
      writeJson(res, 404, {
        code: "404",
        message: "customer not found",
        data: null
      });
      return;
    }

    const httpStatus = payload.code === "200" ? 200 : 500;
    writeJson(res, httpStatus, payload);
    return;
  }

  if (method === "GET" && url === "/health") {
    writeJson(res, 200, { status: "ok" });
    return;
  }

  writeJson(res, 404, { code: "404", message: "not found", data: null });
});

server.listen(port, host, () => {
  console.log(`Mock server running at http://${host}:${port}`);
});
