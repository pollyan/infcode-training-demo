package com.example.training.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 外部客户中心响应 DTO 测试类
 * 
 * 使用 TDD 原则，先编写测试再实现代码
 */
@DisplayName("外部客户中心响应 DTO 测试")
class ExternalCustomerResponseTest {

    @Test
    @DisplayName("测试能够设置和获取响应的 code 字段")
    void testCodeFieldSetterAndGetter() {
        // Given
        ExternalCustomerResponse response = new ExternalCustomerResponse();
        Integer expectedCode = 200;

        // When
        response.setCode(expectedCode);

        // Then
        assertThat(response.getCode()).isEqualTo(expectedCode);
    }

    @Test
    @DisplayName("测试能够设置和获取响应的 message 字段")
    void testMessageFieldSetterAndGetter() {
        // Given
        ExternalCustomerResponse response = new ExternalCustomerResponse();
        String expectedMessage = "success";

        // When
        response.setMessage(expectedMessage);

        // Then
        assertThat(response.getMessage()).isEqualTo(expectedMessage);
    }

    @Test
    @DisplayName("测试能够设置和获取响应的 data 字段")
    void testDataFieldSetterAndGetter() {
        // Given
        ExternalCustomerResponse response = new ExternalCustomerResponse();
        ExternalCustomerResponse.ExternalCustomerData expectedData = 
            new ExternalCustomerResponse.ExternalCustomerData();

        // When
        response.setData(expectedData);

        // Then
        assertThat(response.getData()).isNotNull();
    }

    @Test
    @DisplayName("测试 ExternalCustomerData 的字段映射（cust_code → customerCode）")
    void testCustomerCodeFieldMapping() {
        // Given
        ExternalCustomerResponse.ExternalCustomerData data = 
            new ExternalCustomerResponse.ExternalCustomerData();
        String expectedCustomerCode = "C202503001";

        // When
        data.setCustomerCode(expectedCustomerCode);

        // Then
        assertThat(data.getCustomerCode()).isEqualTo(expectedCustomerCode);
    }

    @Test
    @DisplayName("测试 ExternalCustomerData 的字段映射（cust_name → customerName）")
    void testCustomerNameFieldMapping() {
        // Given
        ExternalCustomerResponse.ExternalCustomerData data = 
            new ExternalCustomerResponse.ExternalCustomerData();
        String expectedCustomerName = "北京星辰科技有限公司";

        // When
        data.setCustomerName(expectedCustomerName);

        // Then
        assertThat(data.getCustomerName()).isEqualTo(expectedCustomerName);
    }

    @Test
    @DisplayName("测试 ExternalCustomerData 的字段映射（cust_status_cd → customerStatus）")
    void testCustomerStatusFieldMapping() {
        // Given
        ExternalCustomerResponse.ExternalCustomerData data = 
            new ExternalCustomerResponse.ExternalCustomerData();
        String expectedCustomerStatus = "ACTIVE";

        // When
        data.setCustomerStatus(expectedCustomerStatus);

        // Then
        assertThat(data.getCustomerStatus()).isEqualTo(expectedCustomerStatus);
    }

    @Test
    @DisplayName("测试 ExternalCustomerData 的字段映射（contact_phone → contactPhone）")
    void testContactPhoneFieldMapping() {
        // Given
        ExternalCustomerResponse.ExternalCustomerData data = 
            new ExternalCustomerResponse.ExternalCustomerData();
        String expectedContactPhone = "13800138000";

        // When
        data.setContactPhone(expectedContactPhone);

        // Then
        assertThat(data.getContactPhone()).isEqualTo(expectedContactPhone);
    }

    @Test
    @DisplayName("测试 ExternalCustomerData 的字段映射（last_modified_date → lastModifiedDate）")
    void testLastModifiedDateFieldMapping() {
        // Given
        ExternalCustomerResponse.ExternalCustomerData data = 
            new ExternalCustomerResponse.ExternalCustomerData();
        String expectedLastModifiedDate = "2026-03-23T10:00:00Z";

        // When
        data.setLastModifiedDate(expectedLastModifiedDate);

        // Then
        assertThat(data.getLastModifiedDate()).isEqualTo(expectedLastModifiedDate);
    }
}
