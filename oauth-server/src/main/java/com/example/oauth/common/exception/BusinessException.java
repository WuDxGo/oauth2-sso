package com.example.oauth.common.exception; // 定义包路径，用于组织和管理 Java 异常类

import lombok.Getter; // 导入 Lombok 的 Getter 注解，自动生成 getter 方法

/**
 * 自定义业务异常类
 * 用于在业务逻辑中抛出特定的异常，携带错误码和错误消息
 */
@Getter // Lombok 注解，自动生成所有字段的 getter 方法
public class BusinessException extends RuntimeException { // 定义业务异常类，继承自 RuntimeException

    /**
     * 错误码字段
     * 用于标识具体的错误类型
     */
    private final Integer code; // 错误码字段，声明为 final 确保不可变

    /**
     * 错误消息字段
     * 用于描述错误的详细信息
     */
    private final String message; // 错误消息字段，声明为 final 确保不可变

    /**
     * 构造函数 - 默认错误码为 500
     * @param message 错误消息
     */
    public BusinessException(String message) { // 单参数构造函数，默认错误码为 500
        super(message); // 调用父类 RuntimeException 的构造函数，传递错误消息
        this.code = 500; // 设置默认错误码为 500（服务器内部错误）
        this.message = message; // 设置错误消息字段
    }

    /**
     * 构造函数 - 可指定错误码和消息
     * @param code 错误码
     * @param message 错误消息
     */
    public BusinessException(Integer code, String message) { // 双参数构造函数，可自定义错误码
        super(message); // 调用父类 RuntimeException 的构造函数，传递错误消息
        this.code = code; // 设置传入的错误码
        this.message = message; // 设置错误消息字段
    }

    /**
     * 构造函数 - 带异常原因的构造函数
     * @param message 错误消息
     * @param cause 异常原因
     */
    public BusinessException(String message, Throwable cause) { // 带异常原因的构造函数，默认错误码为 500
        super(message, cause); // 调用父类 RuntimeException 的构造函数，传递错误消息和原因
        this.code = 500; // 设置默认错误码为 500（服务器内部错误）
        this.message = message; // 设置错误消息字段
    }
}
