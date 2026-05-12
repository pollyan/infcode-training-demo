package com.example.training.service;

import com.example.training.dto.CustomerDirectoryItemResponse;
import com.example.training.dto.ExternalCustomerInfoDTO;
import com.example.training.exception.BizException;
import com.example.training.integration.ExternalCustomerCenterClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * CustomerDirectoryService 测试类
 * 
 * 测试客户档案同步功能，遵循 TDD 原则
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("客户目录服务测试")
public class CustomerDirectoryServiceTest {

    @Mock
    private ExternalCustomerCenterClient externalCustomerCenterClient;

    @InjectMocks
    private CustomerDirectoryService customerDirectoryService;

    @BeforeEach
    void setUp() {
        // 初始化测试用的客户数据
        CustomerDirectoryItemResponse customer = new CustomerDirectoryItemResponse();
        customer.setCustomerCode("C202503001");
        customer.setCustomerName("上海星河建设有限公司");
        customer.setCustomerType("核心客户");
        customer.setOwnerName("王雪");
        customer.setIndustry("建筑施工");
        customer.setRiskLevel("中");
        customer.setCustomerStatus("ACTIVE");
        customer.setSyncStatus("PENDING");
        customer.setSyncMessage("待同步");
        customer.setContactPhone("12345678901");
        customer.setUpdatedTime("2026-03-22 10:30:00");
        customer.setLastSyncTime(null);
    }

    @Test
    @DisplayName("成功同步客户档案，字段映射正确")
    void syncCustomerProfile_success_fieldMappingCorrect() {
        // 准备外部客户中心返回的数据
        ExternalCustomerInfoDTO externalData = new ExternalCustomerInfoDTO();
        externalData.setCustomerCode("C202503001");
        externalData.setCustomerName("上海星河建设有限公司");
        externalData.setCustomerStatus("ACTIVE");
        externalData.setContactPhone("13800138000");
        externalData.setUpdatedTime("2026-05-12T10:30:00");

        when(externalCustomerCenterClient.fetchCustomerProfile("C202503001"))
            .thenReturn(externalData);

        // 执行同步
        CustomerDirectoryItemResponse result = customerDirectoryService.syncCustomerProfile("C202503001");

        // 验证字段映射正确
        assertThat(result.getCustomerCode()).isEqualTo("C202503001");
        assertThat(result.getCustomerName()).isEqualTo("上海星河建设有限公司");
        assertThat(result.getCustomerStatus()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("联系电话脱敏处理：保留前3后4")
    void syncCustomerProfile_phoneNumberMasked_keepFirst3Last4() {
        // 准备外部数据，电话号码为 11 位
        ExternalCustomerInfoDTO externalData = new ExternalCustomerInfoDTO();
        externalData.setCustomerCode("C202503001");
        externalData.setCustomerName("上海星河建设有限公司");
        externalData.setCustomerStatus("ACTIVE");
        externalData.setContactPhone("13800138000");
        externalData.setUpdatedTime("2026-05-12T10:30:00");

        when(externalCustomerCenterClient.fetchCustomerProfile("C202503001"))
            .thenReturn(externalData);

        // 执行同步
        CustomerDirectoryItemResponse result = customerDirectoryService.syncCustomerProfile("C202503001");

        // 验证电话号码已脱敏（保留前3后4）
        assertThat(result.getContactPhone()).isEqualTo("138****8000");
    }

    @Test
    @DisplayName("联系电话脱敏处理：短于7位不脱敏")
    void syncCustomerProfile_phoneNumberNotMasked_whenShorterThan7Digits() {
        // 准备外部数据，电话号码为 6 位
        ExternalCustomerInfoDTO externalData = new ExternalCustomerInfoDTO();
        externalData.setCustomerCode("C202503001");
        externalData.setCustomerName("上海星河建设有限公司");
        externalData.setCustomerStatus("ACTIVE");
        externalData.setContactPhone("123456");
        externalData.setUpdatedTime("2026-05-12T10:30:00");

        when(externalCustomerCenterClient.fetchCustomerProfile("C202503001"))
            .thenReturn(externalData);

        // 执行同步
        CustomerDirectoryItemResponse result = customerDirectoryService.syncCustomerProfile("C202503001");

        // 验证电话号码未脱敏
        assertThat(result.getContactPhone()).isEqualTo("123456");
    }

    @Test
    @DisplayName("更新时间格式转换：ISO 8601 → yyyy-MM-dd HH:mm:ss")
    void syncCustomerProfile_updatedTimeFormatted_iso8601ToCustomFormat() {
        // 准备外部数据，使用 ISO 8601 格式
        ExternalCustomerInfoDTO externalData = new ExternalCustomerInfoDTO();
        externalData.setCustomerCode("C202503001");
        externalData.setCustomerName("上海星河建设有限公司");
        externalData.setCustomerStatus("ACTIVE");
        externalData.setContactPhone("13800138000");
        externalData.setUpdatedTime("2026-05-12T14:30:45");

        when(externalCustomerCenterClient.fetchCustomerProfile("C202503001"))
            .thenReturn(externalData);

        // 执行同步
        CustomerDirectoryItemResponse result = customerDirectoryService.syncCustomerProfile("C202503001");

        // 验证时间格式已转换
        assertThat(result.getUpdatedTime()).isEqualTo("2026-05-12 14:30:45");
    }

    @Test
    @DisplayName("同步状态更新为 SUCCESS")
    void syncCustomerProfile_syncStatusUpdatedToSuccess() {
        // 准备外部数据
        ExternalCustomerInfoDTO externalData = new ExternalCustomerInfoDTO();
        externalData.setCustomerCode("C202503001");
        externalData.setCustomerName("上海星河建设有限公司");
        externalData.setCustomerStatus("ACTIVE");
        externalData.setContactPhone("13800138000");
        externalData.setUpdatedTime("2026-05-12T10:30:00");

        when(externalCustomerCenterClient.fetchCustomerProfile("C202503001"))
            .thenReturn(externalData);

        // 执行同步
        CustomerDirectoryItemResponse result = customerDirectoryService.syncCustomerProfile("C202503001");

        // 验证同步状态为 SUCCESS
        assertThat(result.getSyncStatus()).isEqualTo("SUCCESS");
    }

    @Test
    @DisplayName("同步消息更新为'同步成功'")
    void syncCustomerProfile_syncMessageUpdated() {
        // 准备外部数据
        ExternalCustomerInfoDTO externalData = new ExternalCustomerInfoDTO();
        externalData.setCustomerCode("C202503001");
        externalData.setCustomerName("上海星河建设有限公司");
        externalData.setCustomerStatus("ACTIVE");
        externalData.setContactPhone("13800138000");
        externalData.setUpdatedTime("2026-05-12T10:30:00");

        when(externalCustomerCenterClient.fetchCustomerProfile("C202503001"))
            .thenReturn(externalData);

        // 执行同步
        CustomerDirectoryItemResponse result = customerDirectoryService.syncCustomerProfile("C202503001");

        // 验证同步消息
        assertThat(result.getSyncMessage()).isEqualTo("同步成功");
    }

    @Test
    @DisplayName("记录同步时间")
    void syncCustomerProfile_lastSyncTimeRecorded() {
        // 准备外部数据
        ExternalCustomerInfoDTO externalData = new ExternalCustomerInfoDTO();
        externalData.setCustomerCode("C202503001");
        externalData.setCustomerName("上海星河建设有限公司");
        externalData.setCustomerStatus("ACTIVE");
        externalData.setContactPhone("13800138000");
        externalData.setUpdatedTime("2026-05-12T10:30:00");

        when(externalCustomerCenterClient.fetchCustomerProfile("C202503001"))
            .thenReturn(externalData);

        // 执行同步
        CustomerDirectoryItemResponse result = customerDirectoryService.syncCustomerProfile("C202503001");

        // 验证同步时间已记录
        assertThat(result.getLastSyncTime()).isNotNull();
        assertThat(result.getLastSyncTime()).matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");
    }

    @Test
    @DisplayName("客户不存在时抛出 BizException")
    void syncCustomerProfile_customerNotFound_throwsBizException() {
        // 模拟外部客户中心返回数据
        ExternalCustomerInfoDTO externalData = new ExternalCustomerInfoDTO();
        externalData.setCustomerCode("C999999999"); // 不存在的客户
        externalData.setCustomerName("测试公司");
        externalData.setCustomerStatus("ACTIVE");
        externalData.setContactPhone("13800138000");
        externalData.setUpdatedTime("2026-05-12T10:30:00");

        when(externalCustomerCenterClient.fetchCustomerProfile("C999999999"))
            .thenReturn(externalData);

        // 验证抛出 BizException
        assertThatThrownBy(() -> customerDirectoryService.syncCustomerProfile("C999999999"))
            .isInstanceOf(BizException.class)
            .hasMessageContaining("客户不存在");
    }

    @Test
    @DisplayName("外部客户中心返回错误时抛出 BizException")
    void syncCustomerProfile_externalServiceError_throwsBizException() {
        // 模拟外部客户中心抛出异常
        when(externalCustomerCenterClient.fetchCustomerProfile("C202503001"))
            .thenThrow(new BizException("外部客户中心服务异常"));

        // 验证抛出 BizException
        assertThatThrownBy(() -> customerDirectoryService.syncCustomerProfile("C202503001"))
            .isInstanceOf(BizException.class)
            .hasMessageContaining("外部客户中心服务异常");
    }
}
