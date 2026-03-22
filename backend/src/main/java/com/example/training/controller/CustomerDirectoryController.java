package com.example.training.controller;

import com.example.training.dto.CommonResponse;
import com.example.training.dto.CustomerDirectoryItemResponse;
import com.example.training.service.CustomerDirectoryService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin(origins = {"http://127.0.0.1:5173", "http://localhost:5173"})
@RequestMapping("/api/customers")
public class CustomerDirectoryController {

    private final CustomerDirectoryService customerDirectoryService;

    public CustomerDirectoryController(CustomerDirectoryService customerDirectoryService) {
        this.customerDirectoryService = customerDirectoryService;
    }

    @GetMapping
    public CommonResponse<List<CustomerDirectoryItemResponse>> listCustomers(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String customerStatus,
        @RequestParam(required = false) String syncStatus
    ) {
        return CommonResponse.success(customerDirectoryService.listCustomers(keyword, customerStatus, syncStatus));
    }

    @PostMapping("/{customerCode}/sync-profile")
    public CommonResponse<CustomerDirectoryItemResponse> syncCustomerProfile(@PathVariable String customerCode) {
        return CommonResponse.success(customerDirectoryService.syncCustomerProfile(customerCode));
    }
}
