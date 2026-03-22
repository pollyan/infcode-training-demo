package com.example.training.controller;

import com.example.training.dto.CommonResponse;
import com.example.training.dto.CustomerQueryRequest;
import com.example.training.dto.CustomerQueryResponse;
import com.example.training.service.CustomerQueryService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = {"http://127.0.0.1:5173", "http://localhost:5173"})
@RequestMapping("/api/customers")
public class CustomerQueryController {

    private final CustomerQueryService customerQueryService;

    public CustomerQueryController(CustomerQueryService customerQueryService) {
        this.customerQueryService = customerQueryService;
    }

    @GetMapping("/query")
    public CommonResponse<CustomerQueryResponse> query(@RequestParam String customerCode) {
        CustomerQueryRequest request = new CustomerQueryRequest();
        request.setCustomerCode(customerCode);
        return CommonResponse.success(customerQueryService.queryCustomer(request));
    }
}
