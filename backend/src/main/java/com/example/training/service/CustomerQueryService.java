package com.example.training.service;

import com.example.training.dto.CustomerQueryRequest;
import com.example.training.dto.CustomerQueryResponse;
import com.example.training.dto.ExternalCustomerInfoDTO;
import com.example.training.integration.CustomerCenterClient;
import org.springframework.stereotype.Service;

@Service
public class CustomerQueryService {

    private final CustomerCenterClient customerCenterClient;

    public CustomerQueryService(CustomerCenterClient customerCenterClient) {
        this.customerCenterClient = customerCenterClient;
    }

    public CustomerQueryResponse queryCustomer(CustomerQueryRequest request) {
        if (request == null || request.getCustomerCode() == null || request.getCustomerCode().isBlank()) {
            throw new IllegalArgumentException("customerCode is required");
        }

        ExternalCustomerInfoDTO externalInfo = customerCenterClient.queryCustomer(request.getCustomerCode().trim());

        CustomerQueryResponse response = new CustomerQueryResponse();
        response.setCustomerCode(externalInfo.getCustomerCode());
        response.setCustomerName(externalInfo.getCustomerName());
        response.setCustomerStatus(externalInfo.getCustomerStatus());
        response.setContactPhone(
            externalInfo.getContactPhone() == null || externalInfo.getContactPhone().isBlank()
                ? "未提供"
                : externalInfo.getContactPhone()
        );
        response.setUpdatedTime(
            externalInfo.getUpdatedTime() == null || externalInfo.getUpdatedTime().isBlank()
                ? "-"
                : externalInfo.getUpdatedTime()
        );
        return response;
    }
}
