package com.example.training.controller;

import com.example.training.dto.CommonResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public CommonResponse<String> health() {
        return CommonResponse.success("ok");
    }
}
