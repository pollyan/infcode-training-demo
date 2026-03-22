package com.example.training.service;

import com.example.training.dto.CustomerDirectoryItemResponse;
import com.example.training.dto.ExternalCustomerInfoDTO;
import com.example.training.integration.CustomerCenterClient;
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

@Service
public class CustomerDirectoryService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final CustomerCenterClient customerCenterClient;
    private final Map<String, CustomerDirectoryItemResponse> customerStore = new ConcurrentHashMap<>();

    public CustomerDirectoryService(CustomerCenterClient customerCenterClient) {
        this.customerCenterClient = customerCenterClient;
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

    public CustomerDirectoryItemResponse syncCustomerProfile(String customerCode) {
        if (customerCode == null || customerCode.isBlank()) {
            throw new IllegalArgumentException("customerCode is required");
        }

        CustomerDirectoryItemResponse current = customerStore.get(customerCode.trim());
        if (current == null) {
            throw new IllegalArgumentException("未找到客户：" + customerCode);
        }

        ExternalCustomerInfoDTO externalInfo = customerCenterClient.queryCustomer(customerCode.trim());
        current.setCustomerName(defaultValue(externalInfo.getCustomerName(), current.getCustomerName()));
        current.setCustomerStatus(defaultValue(externalInfo.getCustomerStatus(), current.getCustomerStatus()));
        current.setContactPhone(defaultValue(externalInfo.getContactPhone(), "未提供"));
        current.setUpdatedTime(defaultValue(externalInfo.getUpdatedTime(), current.getUpdatedTime()));
        current.setLastSyncTime(LocalDateTime.now().format(DATE_TIME_FORMATTER));
        current.setSyncStatus("SUCCESS");
        current.setSyncMessage("外部客户中心同步成功");

        return copyItem(current);
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
