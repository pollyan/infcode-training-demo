package com.example.training.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * BizException 测试类
 */
@DisplayName("BizException 测试")
class BizExceptionTest {

    /**
     * 测试创建异常时可以存储用户友好消息
     */
    @Test
    @DisplayName("应该能够存储用户友好消息")
    void shouldStoreUserFriendlyMessage() {
        // Given
        String userMessage = "操作失败，请稍后重试";
        
        // When
        BizException exception = new BizException(userMessage);
        
        // Then
        assertThat(exception.getUserMessage()).isEqualTo(userMessage);
    }

    /**
     * 测试获取用户友好消息返回正确的消息内容
     */
    @Test
    @DisplayName("获取用户友好消息应该返回正确的内容")
    void shouldReturnCorrectUserFriendlyMessage() {
        // Given
        String userMessage = "客户不存在";
        BizException exception = new BizException(userMessage);
        
        // When
        String actualMessage = exception.getUserMessage();
        
        // Then
        assertThat(actualMessage).isEqualTo(userMessage);
    }

    /**
     * 测试创建异常时可以携带原始异常作为原因
     */
    @Test
    @DisplayName("应该能够携带原始异常作为原因")
    void shouldCarryOriginalExceptionAsCause() {
        // Given
        String userMessage = "系统错误";
        Throwable cause = new RuntimeException("底层异常");
        
        // When
        BizException exception = new BizException(userMessage, cause);
        
        // Then
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getUserMessage()).isEqualTo(userMessage);
    }

    /**
     * 测试异常消息应该包含传入的消息
     */
    @Test
    @DisplayName("异常消息应该包含传入的用户消息")
    void shouldContainUserMessageInExceptionMessage() {
        // Given
        String userMessage = "参数校验失败";
        
        // When
        BizException exception = new BizException(userMessage);
        
        // Then
        assertThat(exception.getMessage()).contains(userMessage);
    }
}
