package com.example.training.integration;

import com.example.training.dto.ExternalCustomerInfoDTO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Component
public class CustomerCenterClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String mockBaseUrl;

    public CustomerCenterClient(
        ObjectMapper objectMapper,
        @Value("${training.mock.base-url:http://127.0.0.1:9090}") String mockBaseUrl
    ) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = objectMapper;
        this.mockBaseUrl = mockBaseUrl;
    }

    public ExternalCustomerInfoDTO queryCustomer(String customerCode) {
        String encodedCode = URLEncoder.encode(customerCode, StandardCharsets.UTF_8);
        String requestUrl = mockBaseUrl + "/mock/customer-center/customers/" + encodedCode;

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(requestUrl))
            .GET()
            .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            CustomerCenterResponse payload = objectMapper.readValue(response.body(), CustomerCenterResponse.class);

            if (response.statusCode() >= 400) {
                throw new IllegalArgumentException(payload.message == null ? "customer query failed" : payload.message);
            }

            if (payload.data == null) {
                throw new IllegalArgumentException(payload.message == null ? "customer data is empty" : payload.message);
            }

            ExternalCustomerInfoDTO result = new ExternalCustomerInfoDTO();
            result.setCustomerCode(payload.data.custCode);
            result.setCustomerName(payload.data.custName);
            result.setCustomerStatus(payload.data.custStatus);
            result.setContactPhone(payload.data.contactPhone);
            result.setUpdatedTime(payload.data.updatedAt);
            return result;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("failed to call customer center", exception);
        } catch (IOException exception) {
            throw new IllegalStateException("failed to call customer center", exception);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class CustomerCenterResponse {
        public String code;
        public String message;
        public CustomerCenterData data;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class CustomerCenterData {
        @com.fasterxml.jackson.annotation.JsonProperty("cust_code")
        public String custCode;

        @com.fasterxml.jackson.annotation.JsonProperty("cust_name")
        public String custName;

        @com.fasterxml.jackson.annotation.JsonProperty("cust_status")
        public String custStatus;

        @com.fasterxml.jackson.annotation.JsonProperty("contact_phone")
        public String contactPhone;

        @com.fasterxml.jackson.annotation.JsonProperty("updated_at")
        public String updatedAt;
    }
}
