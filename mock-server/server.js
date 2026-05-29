import http from "node:http";

const port = 9090;
const host = "127.0.0.1";

const customerData = {
  C202503001: {
    code: 200,
    message: "success",
    data: {
      cust_code: "C202503001",
      cust_name: "上海星河建设有限公司",
      cust_status_cd: "ACTIVE",
      contact_phone: "13800000000",
      last_modified_date: "2026-03-23T10:00:00"
    }
  },
  C202503002: {
    code: 200,
    message: "success",
    data: {
      cust_code: "C202503002",
      cust_name: "杭州云启信息科技有限公司",
      cust_status_cd: "ACTIVE",
      contact_phone: null,
      last_modified_date: "2026-03-22T11:05:00"
    }
  },
  C202503500: {
    code: 5000,
    message: "upstream circuit breaker",
    data: null
  },
  C202503998: {
    code: 200,
    message: "success",
    data: null
  },
  C202504021: {
    code: 200,
    message: "success",
    data: {
      cust_code: "C202504021",
      cust_name: "嘉兴明德劳务服务有限公司",
      cust_status_cd: "ACTIVE",
      contact_phone: "13700001111",
      last_modified_date: "2026-03-19T14:12:00"
    }
  },
  C2025034001: {
    code: 4001,
    message: "参数校验失败",
    data: null
  },
  C2025035002: {
    code: 5002,
    message: "请求触发并发限流",
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
      // 未知客户编号：HTTP 200 + 业务错误码 4004
      writeJson(res, 200, {
        code: 4004,
        message: "客户档案不存在",
        data: null
      });
      return;
    }

    // 业务错误码在 JSON body 中表示，HTTP 响应始终返回 200
    writeJson(res, 200, payload);
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
