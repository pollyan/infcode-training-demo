package com.example.training.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 外部客户中心响应 DTO
 * 
 * 用于接收来自外部客户中心的响应数据，包含响应状态码、消息和客户数据
 * 
 * 响应结构示例：
 * {
 *   "code": 200,
 *   "message": "success",
 *   "data": {
 *     "cust_code": "C202503001",
 *     "cust_name": "北京星辰科技有限公司",
 *     "cust_status_cd": "ACTIVE",
 *     "contact_phone": "13800138000",
 *     "last_modified_date": "2026-03-23T10:00:00Z"
 *   }
 * }
 */
public class ExternalCustomerResponse {

    /**
     * 响应状态码
     */
    @JsonProperty("code")
    private Integer code;

    /**
     * 响应消息
     */
    @JsonProperty("message")
    private String message;

    /**
     * 客户数据
     */
    @JsonProperty("data")
    private ExternalCustomerData data;

    // Getter and Setter methods

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ExternalCustomerData getData() {
        return data;
    }

    public void setData(ExternalCustomerData data) {
        this.data = data;
    }

    /**
     * 外部客户数据（静态内部类）
     * 
     * 包含客户档案的核心信息，字段映射使用下划线风格
     */
    public static class ExternalCustomerData {

        /**
         * 客户编码（映射自 cust_code）
         */
        @JsonProperty("cust_code")
        private String customerCode;

        /**
         * 客户名称（映射自 cust_name）
         */
        @JsonProperty("cust_name")
        private String customerName;

        /**
         * 客户状态码（映射自 cust_status_cd）
         */
        @JsonProperty("cust_status_cd")
        private String customerStatus;

        /**
         * 联系电话（映射自 contact_phone）
         */
        @JsonProperty("contact_phone")
        private String contactPhone;

        /**
         * 最后修改日期（映射自 last_modified_date）
         */
        @JsonProperty("last_modified_date")
        private String lastModifiedDate;

        // Getter and Setter methods

        public String getCustomerCode() {
            return customerCode;
        }

        public void setCustomerCode(String customerCode) {
            this.customerCode = customerCode;
        }

        public String getCustomerName() {
            return customerName;
        }

        public void setCustomerName(String customerName) {
            this.customerName = customerName;
        }

        public String getCustomerStatus() {
            return customerStatus;
        }

        public void setCustomerStatus(String customerStatus) {
            this.customerStatus = customerStatus;
        }

        public String getContactPhone() {
            return contactPhone;
        }

        public void setContactPhone(String contactPhone) {
            this.contactPhone = contactPhone;
        }

        public String getLastModifiedDate() {
            return lastModifiedDate;
        }

        public void setLastModifiedDate(String lastModifiedDate) {
            this.lastModifiedDate = lastModifiedDate;
        }
    }
}
