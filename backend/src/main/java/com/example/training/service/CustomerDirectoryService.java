package com.example.training.service;

import com.example.training.dto.CustomerDirectoryItemResponse;
import com.example.training.dto.ExternalCustomerInfoDTO;
import com.example.training.exception.BizException;
import com.example.training.integration.ExternalCustomerCenterClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 客户目录服务
 * 
 * 负责客户档案的业务逻辑处理，包括查询和同步功能
 */
@Service
public class CustomerDirectoryService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerDirectoryService.class);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter ISO_DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final Map<String, CustomerDirectoryItemResponse> customerStore = new ConcurrentHashMap<>();
    private final ExternalCustomerCenterClient externalCustomerCenterClient;

    /**
     * 构造函数（用于依赖注入）
     * 
     * @param externalCustomerCenterClient 外部客户中心客户端
     */
    public CustomerDirectoryService(ExternalCustomerCenterClient externalCustomerCenterClient) {
        this.externalCustomerCenterClient = externalCustomerCenterClient;
        initializeCustomerStore();
    }

    /**
     * 无参构造函数（用于测试）
     */
    public CustomerDirectoryService() {
        this.externalCustomerCenterClient = null;
        initializeCustomerStore();
    }

    public List<CustomerDirectoryItemResponse> listCustomers(String keyword, String customerStatus, String syncStatus) {
        String normalizedKeyword = normalize(keyword);
        String normalizedStatus = normalize(customerStatus);
        String normalizedSyncStatus = normalize(syncStatus);

        return customerStore.values().stream()
            .filter(item -> normalizedKeyword.isEmpty() || matchesKeyword(item, normalizedKeyword))
            .filter(item -> normalizedStatus.isEmpty() || normalizedStatus.equalsIgnoreCase(item.getCustomerStatus()))
            .filter(item -> normalizedSyncStatus.isEmpty() || normalizedSyncStatus.equalsIgnoreCase(item.getSyncStatus()))
            .sorted(Comparator.comparing(CustomerDirectoryItemResponse::getUpdatedTime, Comparator.nullsLast(String::compareTo)).reversed())
            .map(this::copyItem)
            .toList();
    }

    private boolean matchesKeyword(CustomerDirectoryItemResponse item, String normalizedKeyword) {
        return contains(item.getCustomerCode(), normalizedKeyword)
            || contains(item.getCustomerName(), normalizedKeyword)
            || contains(item.getOwnerName(), normalizedKeyword);
    }

    private boolean contains(String value, String normalizedKeyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(normalizedKeyword);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private String defaultValue(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private CustomerDirectoryItemResponse copyItem(CustomerDirectoryItemResponse source) {
        CustomerDirectoryItemResponse target = new CustomerDirectoryItemResponse();
        target.setCustomerCode(source.getCustomerCode());
        target.setCustomerName(source.getCustomerName());
        target.setCustomerType(source.getCustomerType());
        target.setOwnerName(source.getOwnerName());
        target.setIndustry(source.getIndustry());
        target.setRiskLevel(source.getRiskLevel());
        target.setCustomerStatus(source.getCustomerStatus());
        target.setSyncStatus(source.getSyncStatus());
        target.setSyncMessage(source.getSyncMessage());
        target.setContactPhone(source.getContactPhone());
        target.setUpdatedTime(source.getUpdatedTime());
        target.setLastSyncTime(source.getLastSyncTime());
        return target;
    }

    private void initializeCustomerStore() {
        for (CustomerDirectoryItemResponse item : buildSeedCustomers()) {
            customerStore.put(item.getCustomerCode(), item);
        }
    }

    private List<CustomerDirectoryItemResponse> buildSeedCustomers() {
        List<CustomerDirectoryItemResponse> items = new ArrayList<>();
        items.add(buildItem(seed("C202503001", "上海星河建设有限公司", "核心客户", "王雪", "建筑施工", "中", "ACTIVE", "SUCCESS", "最近一次同步成功", "13800000000", "2026-03-22 10:30:00", "2026-03-22 10:35:00")));
        items.add(buildItem(seed("C202503002", "杭州云启信息科技有限公司", "渠道客户", "李成", "软件服务", "低", "ACTIVE", "PENDING", "待补全联系电话字段", "-", "2026-03-22 11:05:00", "2026-03-22 09:40:00")));
        items.add(buildItem(seed("C202503500", "苏州启辰智能设备有限公司", "重点项目", "赵宁", "智能制造", "高", "ACTIVE", "FAILED", "上次同步被外部服务拒绝", "18600000000", "2026-03-21 17:40:00", "2026-03-21 17:42:00")));
        items.add(buildItem(seed("C202503998", "宁波远望工程咨询有限公司", "普通客户", "陈璐", "工程咨询", "中", "INACTIVE", "PENDING", "外部档案为空，待排查", "-", "2026-03-20 15:20:00", "2026-03-20 15:25:00")));
        items.add(buildItem(seed("C202504021", "嘉兴明德劳务服务有限公司", "普通客户", "韩涛", "劳务服务", "低", "ACTIVE", "SUCCESS", "最近一次同步成功", "13700001111", "2026-03-19 14:12:00", "2026-03-19 14:20:00")));
        return items;
    }

    /**
     * 同步客户档案
     * 
     * 从外部客户中心获取最新客户资料，完成字段映射后更新本地数据
     * 
     * @param customerCode 客户编号
     * @return 同步后的客户档案信息
     * @throws BizException 当客户不存在或外部服务调用失败时抛出
     */
    public CustomerDirectoryItemResponse syncCustomerProfile(String customerCode) {
        logger.info("[客户档案同步] 开始同步客户档案, customerCode={}", customerCode);

        // 调用外部客户中心获取最新数据
        ExternalCustomerInfoDTO externalData = externalCustomerCenterClient.fetchCustomerProfile(customerCode);

        // 获取本地客户数据
        CustomerDirectoryItemResponse customer = customerStore.get(customerCode);
        if (customer == null) {
            logger.warn("[客户档案同步] 客户不存在, customerCode={}", customerCode);
            throw new BizException("客户不存在");
        }

        // 字段映射
        customer.setCustomerName(externalData.getCustomerName());
        customer.setCustomerStatus(externalData.getCustomerStatus());
        
        // 联系电话脱敏处理
        customer.setContactPhone(maskPhoneNumber(externalData.getContactPhone()));
        
        // 更新时间格式转换
        customer.setUpdatedTime(formatUpdateTime(externalData.getUpdatedTime()));
        
        // 更新同步状态
        customer.setSyncStatus("SUCCESS");
        customer.setSyncMessage("同步成功");
        
        // 记录同步时间
        String nowTime = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        customer.setLastSyncTime(nowTime);

        logger.info("[客户档案同步] 同步完成, customerCode={}", customerCode);

        return copyItem(customer);
    }

    /**
     * 联系电话脱敏处理
     * 
     * 保留前3后4，中间4位用星号屏蔽
     * 如果电话号码短于7位，不进行脱敏
     * 
     * @param phoneNumber 原始电话号码
     * @return 脱敏后的电话号码
     */
    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 7) {
            return phoneNumber;
        }
        
        int length = phoneNumber.length();
        String first3 = phoneNumber.substring(0, 3);
        String last4 = phoneNumber.substring(length - 4);
        return first3 + "****" + last4;
    }

    /**
     * 更新时间格式转换
     * 
     * 将 ISO 8601 格式的时间转换为 yyyy-MM-dd HH:mm:ss 格式
     * 
     * @param lastModifiedDate ISO 8601 格式的时间
     * @return 转换后的时间字符串
     */
    private String formatUpdateTime(String lastModifiedDate) {
        if (lastModifiedDate == null) {
            return null;
        }
        
        try {
            LocalDateTime dateTime = LocalDateTime.parse(lastModifiedDate, ISO_DATE_TIME_FORMATTER);
            return dateTime.format(DATE_TIME_FORMATTER);
        } catch (Exception e) {
            logger.warn("[客户档案同步] 时间格式转换失败, lastModifiedDate={}", lastModifiedDate, e);
            return lastModifiedDate;
        }
    }

    private Map<String, String> seed(
        String customerCode,
        String customerName,
        String customerType,
        String ownerName,
        String industry,
        String riskLevel,
        String customerStatus,
        String syncStatus,
        String syncMessage,
        String contactPhone,
        String updatedTime,
        String lastSyncTime
    ) {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("customerCode", customerCode);
        values.put("customerName", customerName);
        values.put("customerType", customerType);
        values.put("ownerName", ownerName);
        values.put("industry", industry);
        values.put("riskLevel", riskLevel);
        values.put("customerStatus", customerStatus);
        values.put("syncStatus", syncStatus);
        values.put("syncMessage", syncMessage);
        values.put("contactPhone", contactPhone);
        values.put("updatedTime", updatedTime);
        values.put("lastSyncTime", lastSyncTime);
        return values;
    }

    private CustomerDirectoryItemResponse buildItem(Map<String, String> values) {
        CustomerDirectoryItemResponse item = new CustomerDirectoryItemResponse();
        item.setCustomerCode(values.get("customerCode"));
        item.setCustomerName(values.get("customerName"));
        item.setCustomerType(values.get("customerType"));
        item.setOwnerName(values.get("ownerName"));
        item.setIndustry(values.get("industry"));
        item.setRiskLevel(values.get("riskLevel"));
        item.setCustomerStatus(values.get("customerStatus"));
        item.setSyncStatus(values.get("syncStatus"));
        item.setSyncMessage(values.get("syncMessage"));
        item.setContactPhone(values.get("contactPhone"));
        item.setUpdatedTime(values.get("updatedTime"));
        item.setLastSyncTime(values.get("lastSyncTime"));
        return item;
    }
}
