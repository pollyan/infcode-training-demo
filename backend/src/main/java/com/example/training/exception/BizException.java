package com.example.training.exception;

/**
 * 业务异常类
 * 
 * 用于表示业务逻辑中的异常情况。与系统异常不同，业务异常通常是由用户输入、业务规则验证失败等原因引起的，
 * 可以通过修正用户行为或提供友好的提示信息来解决。
 * 
 * 该异常类提供了一个用户友好的错误消息字段，用于向前端或用户展示清晰的错误提示。
 * 
 * @author InfCode Team
 * @since 1.0.0
 */
public class BizException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 用户友好的错误提示消息
     * 该消息用于向用户展示，不应包含技术细节
     */
    private final String userMessage;

    /**
     * 构造函数 - 仅提供用户友好消息
     * 
     * @param userMessage 用户友好的错误提示消息
     */
    public BizException(String userMessage) {
        super(userMessage);
        this.userMessage = userMessage;
    }

    /**
     * 构造函数 - 提供用户友好消息和原始异常原因
     * 
     * @param userMessage 用户友好的错误提示消息
     * @param cause 原始异常，用于保留完整的异常链
     */
    public BizException(String userMessage, Throwable cause) {
        super(userMessage, cause);
        this.userMessage = userMessage;
    }

    /**
     * 获取用户友好的错误提示消息
     * 
     * @return 用户友好的错误提示消息
     */
    public String getUserMessage() {
        return userMessage;
    }
}
