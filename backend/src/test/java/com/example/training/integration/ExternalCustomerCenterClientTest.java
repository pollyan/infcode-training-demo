package com.example.training.integration;

import com.example.training.dto.ExternalCustomerInfoDTO;
import com.example.training.exception.BizException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

/**
 * ExternalCustomerCenterClient 测试类
 * 
 * 使用 MockRestServiceServer 模拟外部客户中心服务，测试各种正常和异常场景
 * 
 * 测试流程：
 * 1. 模拟外部服务响应
 * 2. 调用客户端方法
 * 3. 验证返回结果或异常
 * 4. 验证请求参数和请求头
 */
@SpringBootTest
@DisplayName("外部客户中心集成客户端测试")
public class ExternalCustomerCenterClientTest {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ExternalCustomerCenterClient client;

    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        // 为 RestTemplate 创建 Mock 服务器
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    @DisplayName("测试成功调用外部客户中心获取客户档案")
    void testFetchCustomerProfile_Success() {
        // Given: 准备模拟成功响应
        String customerCode = "C202503001";
        String responseJson = """
            {
              "code": 200,
              "message": "success",
              "data": {
                "cust_code": "C202503001",
                "cust_name": "北京星辰科技有限公司",
                "cust_status_cd": "ACTIVE",
                "contact_phone": "13800138000",
                "last_modified_date": "2026-03-23T10:00:00Z"
              }
            }
            """;

        mockServer.expect(once(), requestTo("http://localhost:9090/mock/customer-center/customers/" + customerCode))
                .andExpect(method(org.springframework.http.HttpMethod.GET))
                .andExpect(header("X-App-Key", "xingchen-crm-local"))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        // When: 调用客户端方法
        ExternalCustomerInfoDTO result = client.fetchCustomerProfile(customerCode);

        // Then: 验证返回结果
        assertThat(result).isNotNull();
        assertThat(result.getCustomerCode()).isEqualTo("C202503001");
        assertThat(result.getCustomerName()).isEqualTo("北京星辰科技有限公司");
        assertThat(result.getCustomerStatus()).isEqualTo("ACTIVE");
        assertThat(result.getContactPhone()).isEqualTo("13800138000");
        assertThat(result.getUpdatedTime()).isEqualTo("2026-03-23T10:00:00Z");

        mockServer.verify();
    }

    @Test
    @DisplayName("测试外部客户中心返回 4001 错误码时抛出 BizException")
    void testFetchCustomerProfile_4001Error() {
        // Given: 准备模拟 4001 参数校验失败响应
        String customerCode = "C202503001";
        String responseJson = """
            {
              "code": 4001,
              "message": "参数校验失败",
              "data": null
            }
            """;

        mockServer.expect(once(), requestTo("http://localhost:9090/mock/customer-center/customers/" + customerCode))
                .andExpect(method(org.springframework.http.HttpMethod.GET))
                .andExpect(header("X-App-Key", "xingchen-crm-local"))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        // When & Then: 调用客户端方法，验证抛出 BizException
        assertThatThrownBy(() -> client.fetchCustomerProfile(customerCode))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("参数校验失败");

        mockServer.verify();
    }

    @Test
    @DisplayName("测试外部客户中心返回 4004 错误码时抛出 BizException")
    void testFetchCustomerProfile_4004Error() {
        // Given: 准备模拟 4004 客户档案不存在响应
        String customerCode = "INVALID_CODE";
        String responseJson = """
            {
              "code": 4004,
              "message": "客户档案不存在",
              "data": null
            }
            """;

        mockServer.expect(once(), requestTo("http://localhost:9090/mock/customer-center/customers/" + customerCode))
                .andExpect(method(org.springframework.http.HttpMethod.GET))
                .andExpect(header("X-App-Key", "xingchen-crm-local"))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        // When & Then: 调用客户端方法，验证抛出 BizException
        assertThatThrownBy(() -> client.fetchCustomerProfile(customerCode))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("客户档案不存在");

        mockServer.verify();
    }

    @Test
    @DisplayName("测试外部客户中心返回 5000 错误码时抛出 BizException")
    void testFetchCustomerProfile_5000Error() {
        // Given: 准备模拟 5000 上游核心链路熔断响应
        String customerCode = "C202503001";
        String responseJson = """
            {
              "code": 5000,
              "message": "上游核心链路熔断",
              "data": null
            }
            """;

        mockServer.expect(once(), requestTo("http://localhost:9090/mock/customer-center/customers/" + customerCode))
                .andExpect(method(org.springframework.http.HttpMethod.GET))
                .andExpect(header("X-App-Key", "xingchen-crm-local"))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        // When & Then: 调用客户端方法，验证抛出 BizException
        assertThatThrownBy(() -> client.fetchCustomerProfile(customerCode))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("服务暂时不可用");

        mockServer.verify();
    }

    @Test
    @DisplayName("测试外部客户中心返回 5002 错误码时抛出 BizException")
    void testFetchCustomerProfile_5002Error() {
        // Given: 准备模拟 5002 并发限流响应
        String customerCode = "C202503001";
        String responseJson = """
            {
              "code": 5002,
              "message": "请求触发并发限流",
              "data": null
            }
            """;

        mockServer.expect(once(), requestTo("http://localhost:9090/mock/customer-center/customers/" + customerCode))
                .andExpect(method(org.springframework.http.HttpMethod.GET))
                .andExpect(header("X-App-Key", "xingchen-crm-local"))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        // When & Then: 调用客户端方法，验证抛出 BizException
        assertThatThrownBy(() -> client.fetchCustomerProfile(customerCode))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("请求过于频繁");

        mockServer.verify();
    }

    @Test
    @DisplayName("测试外部客户中心返回数据为空时抛出 BizException")
    void testFetchCustomerProfile_DataIsNull() {
        // Given: 准备模拟 data 为 null 的响应
        String customerCode = "C202503001";
        String responseJson = """
            {
              "code": 200,
              "message": "success",
              "data": null
            }
            """;

        mockServer.expect(once(), requestTo("http://localhost:9090/mock/customer-center/customers/" + customerCode))
                .andExpect(method(org.springframework.http.HttpMethod.GET))
                .andExpect(header("X-App-Key", "xingchen-crm-local"))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        // When & Then: 调用客户端方法，验证抛出 BizException
        assertThatThrownBy(() -> client.fetchCustomerProfile(customerCode))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("客户档案不存在");

        mockServer.verify();
    }

    @Test
    @DisplayName("测试网络连接失败时抛出 BizException")
    void testFetchCustomerProfile_NetworkError() {
        // Given: 模拟网络连接失败
        String customerCode = "C202503001";

        mockServer.expect(once(), requestTo("http://localhost:9090/mock/customer-center/customers/" + customerCode))
                .andExpect(method(org.springframework.http.HttpMethod.GET))
                .andExpect(header("X-App-Key", "xingchen-crm-local"))
                .andRespond(withResourceNotFound());

        // When & Then: 调用客户端方法，验证抛出 BizException
        assertThatThrownBy(() -> client.fetchCustomerProfile(customerCode))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("获取客户档案失败");

        mockServer.verify();
    }

    @Test
    @DisplayName("测试请求头包含正确的 X-App-Key")
    void testFetchCustomerProfile_CheckHeader() {
        // Given: 准备模拟成功响应
        String customerCode = "C202503001";
        String responseJson = """
            {
              "code": 200,
              "message": "success",
              "data": {
                "cust_code": "C202503001",
                "cust_name": "北京星辰科技有限公司",
                "cust_status_cd": "ACTIVE",
                "contact_phone": "13800138000",
                "last_modified_date": "2026-03-23T10:00:00Z"
              }
            }
            """;

        mockServer.expect(once(), requestTo("http://localhost:9090/mock/customer-center/customers/" + customerCode))
                .andExpect(method(org.springframework.http.HttpMethod.GET))
                .andExpect(header("X-App-Key", "xingchen-crm-local"))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        // When: 调用客户端方法
        client.fetchCustomerProfile(customerCode);

        // Then: 验证请求头（已在 mockServer 期望中验证）
        mockServer.verify();
    }
}
