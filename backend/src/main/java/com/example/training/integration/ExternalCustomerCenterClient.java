package com.example.training.integration;

import com.example.training.dto.ExternalCustomerInfoDTO;
import com.example.training.dto.ExternalCustomerResponse;
import com.example.training.exception.BizException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;

import java.net.URI;

/**
 * 外部客户中心集成客户端
 * 
 * 负责与星辰集团统一客户中心进行交互，获取企业客户全量档案信息。
 * 该客户端实现了完整的错误处理、日志记录和超时控制机制。
 * 
 * <p>主要功能：</p>
 * <ul>
 *   <li>通过客户编号查询企业客户全量档案</li>
 *   <li>自动处理鉴权请求头</li>
 *   <li>统一错误码映射和异常转换</li>
 *   <li>完整的日志记录</li>
 * </ul>
 * 
 * <p>技术特性：</p>
 * <ul>
 *   <li>使用 RestTemplate 进行 HTTP 调用</li>
 *   <li>所有异常转换为 BizException，避免暴露底层异常</li>
 *   <li>关键操作记录日志，包含 customerCode 标识</li>
 *   <li>遵循安全规范，不透传外部错误信息</li>
 * </ul>
 * 
 * @author InfCode Team
 * @since 1.0.0
 */
@Component
public class ExternalCustomerCenterClient {

    private static final Logger log = LoggerFactory.getLogger(ExternalCustomerCenterClient.class);
    
    // 鉴权请求头
    private static final String APP_KEY_HEADER = "X-App-Key";
    private static final String APP_KEY_VALUE = "xingchen-crm-local";

    // 错误码常量
    private static final int CODE_SUCCESS = 200;
    private static final int CODE_PARAM_INVALID = 4001;
    private static final int CODE_CUSTOMER_NOT_FOUND = 4004;
    private static final int CODE_UPSTREAM_CIRCUIT_BREAKER = 5000;
    private static final int CODE_RATE_LIMIT_EXCEEDED = 5002;

    // 外部服务基础 URL（可通过配置覆盖）
    @Value("${external.customer-center.url:http://localhost:9090/mock/customer-center}")
    private String customerCenterBaseUrl;

    // RestTemplate 实例
    private final RestTemplate restTemplate;

    /**
     * 构造函数
     * 
     * @param restTemplate RestTemplate 实例，用于 HTTP 调用
     */
    public ExternalCustomerCenterClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * 根据客户编号获取客户档案
     * 
     * 该方法调用外部客户中心服务，获取指定客户的完整档案信息。
     * 
     * <p>请求流程：</p>
     * <ol>
     *   <li>构建 REST 请求，包含鉴权请求头</li>
     *   <li>发送 GET 请求到外部服务</li>
     *   <li>解析响应并检查业务状态码</li>
     *   <li>转换外部数据结构为内部 DTO</li>
     * </ol>
     * 
     * <p>错误处理：</p>
     * <ul>
     *   <li>4001: 参数校验失败 → BizException</li>
     *   <li>4004: 客户档案不存在 → BizException</li>
     *   <li>5000: 上游核心链路熔断 → BizException</li>
     *   <li>5002: 并发限流 → BizException</li>
     *   <li>网络异常: 统一转换为 BizException</li>
     * </ul>
     * 
     * @param customerCode 客户编号，不能为空
     * @return 客户档案信息 DTO
     * @throws BizException 当获取客户档案失败时抛出
     */
    public ExternalCustomerInfoDTO fetchCustomerProfile(String customerCode) {
        log.info("开始获取客户档案，customerCode: {}", customerCode);

        try {
            // 构建请求 URL
            String requestUrl = customerCenterBaseUrl + "/customers/" + customerCode;
            
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.set(APP_KEY_HEADER, APP_KEY_VALUE);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // 创建请求实体
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
            
            // 发送 GET 请求
            log.debug("发送请求到外部客户中心，URL: {}, customerCode: {}", requestUrl, customerCode);
            ResponseEntity<ExternalCustomerResponse> response = restTemplate.exchange(
                    URI.create(requestUrl),
                    HttpMethod.GET,
                    requestEntity,
                    ExternalCustomerResponse.class
            );
            
            // 检查响应状态
            if (response.getStatusCode() != HttpStatus.OK) {
                log.error("外部客户中心返回非 200 状态码，statusCode: {}, customerCode: {}", 
                        response.getStatusCode(), customerCode);
                throw new BizException("获取客户档案失败，外部服务返回异常状态码");
            }
            
            // 获取响应体
            ExternalCustomerResponse externalResponse = response.getBody();
            if (externalResponse == null) {
                log.error("外部客户中心响应体为空，customerCode: {}", customerCode);
                throw new BizException("获取客户档案失败，外部服务返回空响应");
            }
            
            // 检查业务状态码
            int businessCode = externalResponse.getCode();
            log.debug("外部客户中心返回业务状态码: {}, customerCode: {}", businessCode, customerCode);
            
            switch (businessCode) {
                case CODE_SUCCESS:
                    // 成功，继续处理数据
                    break;
                case CODE_PARAM_INVALID:
                    log.warn("参数校验失败，customerCode: {}", customerCode);
                    throw new BizException("参数校验失败，请检查客户编号格式");
                case CODE_CUSTOMER_NOT_FOUND:
                    log.warn("客户档案不存在，customerCode: {}", customerCode);
                    throw new BizException("客户档案不存在");
                case CODE_UPSTREAM_CIRCUIT_BREAKER:
                    log.error("上游核心链路熔断，customerCode: {}", customerCode);
                    throw new BizException("服务暂时不可用，请稍后重试");
                case CODE_RATE_LIMIT_EXCEEDED:
                    log.warn("触发并发限流，customerCode: {}", customerCode);
                    throw new BizException("请求过于频繁，请稍后重试");
                default:
                    log.error("未知业务错误码: {}, customerCode: {}", businessCode, customerCode);
                    throw new BizException("获取客户档案失败");
            }
            
            // 检查数据是否为空
            ExternalCustomerResponse.ExternalCustomerData data = externalResponse.getData();
            if (data == null) {
                log.error("外部客户中心返回数据为空，customerCode: {}", customerCode);
                throw new BizException("客户档案不存在");
            }
            
            // 转换为内部 DTO
            ExternalCustomerInfoDTO dto = convertToInternalDTO(data);
            
            log.info("成功获取客户档案，customerCode: {}, customerName: {}", 
                    customerCode, dto.getCustomerName());
            
            return dto;
            
        } catch (BizException e) {
            // BizException 直接抛出
            throw e;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            // HTTP 错误
            log.error("HTTP 请求失败，statusCode: {}, customerCode: {}, error: {}", 
                    e.getStatusCode(), customerCode, e.getMessage());
            throw new BizException("获取客户档案失败，网络请求异常");
        } catch (RestClientException e) {
            // 其他客户端异常（网络超时、连接失败等）
            log.error("调用外部客户中心失败，customerCode: {}, error: {}", customerCode, e.getMessage());
            throw new BizException("获取客户档案失败，请稍后重试");
        } catch (Exception e) {
            // 其他未知异常
            log.error("未知异常，customerCode: {}, error: {}", customerCode, e.getMessage());
            throw new BizException("获取客户档案失败");
        }
    }

    /**
     * 将外部客户数据转换为内部 DTO
     * 
     * 该方法执行字段映射和数据转换，确保内部系统使用统一的命名规范。
     * 
     * @param data 外部客户数据
     * @return 内部 DTO
     */
    private ExternalCustomerInfoDTO convertToInternalDTO(ExternalCustomerResponse.ExternalCustomerData data) {
        ExternalCustomerInfoDTO dto = new ExternalCustomerInfoDTO();
        
        dto.setCustomerCode(data.getCustomerCode());
        dto.setCustomerName(data.getCustomerName());
        dto.setCustomerStatus(data.getCustomerStatus());
        dto.setContactPhone(data.getContactPhone());
        dto.setUpdatedTime(data.getLastModifiedDate());
        
        return dto;
    }
}
