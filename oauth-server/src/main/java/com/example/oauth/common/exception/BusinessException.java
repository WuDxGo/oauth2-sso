package com.example.oauth.common.exception;

import lombok.Getter;

/**
 * 自定义业务异常类
 * 继承自RuntimeException,用于在业务逻辑中抛出特定的异常
 * 携带错误码和错误消息,方便全局异常处理器统一处理
 * 使用@Getter注解自动生成所有字段的getter方法
 * 异常类名以BusinessException结尾,语义清晰
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 错误码字段
     * 用于标识具体的错误类型和业务错误状态
     * 常见值:400参数错误,404资源不存在,409资源冲突等
     * 前端可以根据此字段判断错误类型并展示不同的提示
     * 使用final修饰,一旦赋值不可修改,保证异常对象的不可变性
     */
    private final Integer code;

    /**
     * 错误消息字段
     * 用于描述错误的详细信息,方便开发人员和用户理解错误原因
     * 如:"用户名已存在"、"密码错误"、"订单不存在"
     * 使用final修饰,一旦赋值不可修改,保证异常对象的不可变性
     * 重写RuntimeException的message字段,使其可通过getter方法访问
     */
    private final String message;

    /**
     * 单参数构造函数
     * 默认错误码为500(服务器内部错误),适用于未知业务异常
     * 仅需传入错误消息,使用简单方便
     *
     * @param message 错误消息,描述业务失败的原因
     */
    public BusinessException(String message) {
        // 调用父类RuntimeException的构造函数,传入错误消息
        // RuntimeException会将消息存储在内部的message字段中
        super(message);
        // 设置默认错误码为500,表示服务器内部业务错误
        this.code = 500;
        // 设置错误消息字段为重写后的message字段
        this.message = message;
    }

    /**
     * 双参数构造函数
     * 允许同时指定错误码和错误消息
     * 适用于需要返回特定业务错误码的场景
     * 如:400表示参数错误,404表示资源不存在,409表示冲突等
     *
     * @param code 错误码,表示具体的错误类型
     * @param message 错误消息,描述业务失败的原因
     */
    public BusinessException(Integer code, String message) {
        // 调用父类RuntimeException的构造函数,传入错误消息
        super(message);
        // 设置传入的错误码,允许调用方自定义错误类型
        this.code = code;
        // 设置错误消息字段
        this.message = message;
    }

    /**
     * 三参数构造函数(带异常原因)
     * 允许指定错误消息和底层异常原因,默认错误码为500
     * 适用于包装底层异常(如数据库异常、网络异常)为业务异常
     * 保留原始异常信息,方便问题排查
     *
     * @param message 错误消息,描述业务失败的原因
     * @param cause 底层异常原因,导致业务失败的原始异常对象
     */
    public BusinessException(String message, Throwable cause) {
        // 调用父类RuntimeException的三参数构造函数,传入错误消息和异常原因
        // RuntimeException会保留原始异常的堆栈信息,方便追踪问题
        super(message, cause);
        // 设置默认错误码为500,表示服务器内部业务错误
        this.code = 500;
        // 设置错误消息字段
        this.message = message;
    }
}
