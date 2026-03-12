package com.example.oauth.common.result; // 定义包路径，用于组织和管理 Java 结果类

import lombok.Data; // 导入 Lombok 的 Data 注解，自动生成 getter、setter、toString 等方法

import java.io.Serializable; // 导入 Serializable 接口，使对象可以被序列化，支持网络传输和持久化

/**
 * 统一返回结果泛型类
 * 用于封装所有 Controller 方法的返回值，提供统一的响应格式
 * @param <T> 数据类型泛型，表示返回的数据的具体类型
 */
@Data // Lombok 注解，自动生成所有字段的 getter、setter、toString、equals、hashCode 方法
public class Result<T> implements Serializable { // 定义统一的返回结果泛型类，实现 Serializable 接口以支持序列化

    private static final long serialVersionUID = 1L; // 序列化版本号，用于在反序列化时验证版本兼容性

    /**
     * 状态码字段
     * 表示请求处理的最终状态（如 200 成功，500 错误等）
     */
    private Integer code; // HTTP 状态码或业务状态码字段

    /**
     * 消息字段
     * 描述请求处理的结果信息（如"操作成功"、"参数错误"等）
     */
    private String message; // 响应消息字段

    /**
     * 数据字段
     * 存储实际返回的业务数据，类型为泛型 T
     */
    private T data; // 响应数据字段，可以是任意类型

    /**
     * 时间戳字段
     * 记录响应生成的 Unix 时间戳（毫秒）
     */
    private Long timestamp; // 响应时间戳字段

    /**
     * 默认构造函数
     * 初始化时间戳为当前系统时间
     */
    public Result() { // 无参构造函数
        this.timestamp = System.currentTimeMillis(); // 设置时间戳为当前系统时间的毫秒值
    }

    /**
     * 全参数构造函数
     * @param code 状态码
     * @param message 消息
     * @param data 数据
     */
    public Result(Integer code, String message, T data) { // 三参数构造函数
        this.code = code; // 设置状态码字段
        this.message = message; // 设置消息字段
        this.data = data; // 设置数据字段
        this.timestamp = System.currentTimeMillis(); // 设置时间戳为当前系统时间的毫秒值
    }

    /**
     * 创建成功的响应（无数据）
     * @param <T> 数据类型泛型
     * @return Result<T> 成功响应结果
     */
    public static <T> Result<T> success() { // 静态工厂方法，创建成功的响应
        return new Result<>(200, "操作成功", null); // 返回状态码 200，消息"操作成功"，无数据的响应
    }

    /**
     * 创建成功的响应（带数据）
     * @param data 返回的数据
     * @param <T> 数据类型泛型
     * @return Result<T> 成功响应结果
     */
    public static <T> Result<T> success(T data) { // 静态工厂方法，创建带数据的成功响应
        return new Result<>(200, "操作成功", data); // 返回状态码 200，消息"操作成功"，包含指定数据的响应
    }

    /**
     * 创建成功的响应（自定义消息和数据）
     * @param message 自定义消息
     * @param data 返回的数据
     * @param <T> 数据类型泛型
     * @return Result<T> 成功响应结果
     */
    public static <T> Result<T> success(String message, T data) { // 静态工厂方法，创建自定义消息的成功响应
        return new Result<>(200, message, data); // 返回状态码 200，指定消息和数据的响应
    }

    /**
     * 创建错误的响应（默认错误码 500）
     * @param message 错误消息
     * @param <T> 数据类型泛型
     * @return Result<T> 错误响应结果
     */
    public static <T> Result<T> error(String message) { // 静态工厂方法，创建错误响应
        return new Result<>(500, message, null); // 返回状态码 500，指定错误消息，无数据的响应
    }

    /**
     * 创建错误的响应（可指定错误码）
     * @param code 错误码
     * @param message 错误消息
     * @param <T> 数据类型泛型
     * @return Result<T> 错误响应结果
     */
    public static <T> Result<T> error(Integer code, String message) { // 静态工厂方法，创建带错误码的错误响应
        return new Result<>(code, message, null); // 返回指定错误码和错误消息，无数据的响应
    }

    /**
     * 创建未授权的响应（401）
     * @param message 未授权消息
     * @param <T> 数据类型泛型
     * @return Result<T> 未授权响应结果
     */
    public static <T> Result<T> unauthorized(String message) { // 静态工厂方法，创建未授权响应
        return new Result<>(401, message, null); // 返回状态码 401，指定消息，无数据的响应
    }

    /**
     * 创建禁止访问的响应（403）
     * @param message 禁止访问消息
     * @param <T> 数据类型泛型
     * @return Result<T> 禁止访问响应结果
     */
    public static <T> Result<T> forbidden(String message) { // 静态工厂方法，创建禁止访问响应
        return new Result<>(403, message, null); // 返回状态码 403，指定消息，无数据的响应
    }
}
