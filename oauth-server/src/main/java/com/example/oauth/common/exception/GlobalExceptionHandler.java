package com.example.oauth.common.exception; // 定义包路径，用于组织和管理 Java 异常处理类

import com.example.oauth.common.result.Result; // 导入统一返回结果类，用于封装异常处理后的响应
import lombok.extern.slf4j.Slf4j; // 导入 Lombok 的 Slf4j 注解，自动生成日志记录器
import org.springframework.http.HttpStatus; // 导入 HTTP 状态码枚举，用于设置响应状态
import org.springframework.validation.BindException; // 导入参数绑定异常类，处理表单绑定错误
import org.springframework.validation.FieldError; // 导入字段错误类，获取具体字段的验证错误信息
import org.springframework.web.bind.MethodArgumentNotValidException; // 导入方法参数校验异常类，处理@RequestBody 参数验证失败
import org.springframework.web.bind.annotation.ExceptionHandler; // 导入异常处理器注解，指定处理的异常类型
import org.springframework.web.bind.annotation.ResponseStatus; // 导入响应状态注解，设置返回的 HTTP 状态码
import org.springframework.web.bind.annotation.RestControllerAdvice; // 导入 REST 控制器增强注解，标识全局异常处理器

import java.util.stream.Collectors; // 导入 Stream API 的 Collectors 工具类，用于收集流中的元素

/**
 * 全局异常处理器类
 * 使用@RestControllerAdvice 注解标识，自动拦截并处理所有 Controller 抛出的异常
 */
@Slf4j // Lombok 注解，自动生成名为"log"的静态日志记录器字段
@RestControllerAdvice // Spring Boot 注解，标识此类为全局异常处理器，拦截所有 Controller 的异常
public class GlobalExceptionHandler { // 定义全局异常处理器类

    /**
     * 处理自定义业务异常
     * 当业务逻辑中抛出 BusinessException 时调用此方法
     * @param e BusinessException 业务异常实例
     * @return Result<Void> 统一的错误响应结果
     */
    @ExceptionHandler(BusinessException.class) // 指定此方法处理 BusinessException 类型的异常
    @ResponseStatus(HttpStatus.OK) // 设置响应状态码为 200 OK（业务异常通常返回 OK，由前端根据 code 判断）
    public Result<Void> handleBusinessException(BusinessException e) { // 处理业务异常的方法
        log.warn("业务异常：{}", e.getMessage()); // 使用日志记录器记录警告日志，输出异常消息
        return Result.error(e.getCode(), e.getMessage()); // 返回统一的错误响应结果，包含错误码和错误消息
    }

    /**
     * 处理参数校验异常（针对@RequestBody 注解的参数）
     * 当使用@Valid 或@Validated 验证@RequestBody 参数失败时调用此方法
     * @param e MethodArgumentNotValidException 方法参数校验异常实例
     * @return Result<Void> 统一的错误响应结果
     */
    @ExceptionHandler(MethodArgumentNotValidException.class) // 指定此方法处理 MethodArgumentNotValidException 类型的异常
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 设置响应状态码为 400 Bad Request（参数校验失败）
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) { // 处理方法参数校验异常的方法
        String message = e.getBindingResult().getFieldErrors().stream() // 从异常对象中获取所有字段错误列表，并转换为流
                .map(FieldError::getDefaultMessage) // 提取每个字段的默认错误消息
                .collect(Collectors.joining(", ")); // 使用分号将所有错误消息连接成一个字符串
        log.warn("参数校验异常：{}", message); // 使用日志记录器记录警告日志，输出校验失败的详细信息
        return Result.error(400, message); // 返回统一的错误响应结果，错误码为 400，消息为校验失败详情
    }

    /**
     * 处理参数绑定异常（针对@RequestParam 或@PathVariable 注解的参数）
     * 当请求参数类型不匹配或格式错误时调用此方法
     * @param e BindException 参数绑定异常实例
     * @return Result<Void> 统一的错误响应结果
     */
    @ExceptionHandler(BindException.class) // 指定此方法处理 BindException 类型的异常
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 设置响应状态码为 400 Bad Request（参数绑定失败）
    public Result<Void> handleBindException(BindException e) { // 处理参数绑定异常的方法
        String message = e.getBindingResult().getFieldErrors().stream() // 从异常对象中获取所有字段错误列表，并转换为流
                .map(FieldError::getDefaultMessage) // 提取每个字段的默认错误消息
                .collect(Collectors.joining(", ")); // 使用分号将所有错误消息连接成一个字符串
        log.warn("参数绑定异常：{}", message); // 使用日志记录器记录警告日志，输出绑定失败的详细信息
        return Result.error(400, message); // 返回统一的错误响应结果，错误码为 400，消息为绑定失败详情
    }

    /**
     * 处理 IllegalArgumentException 非法参数异常
     * 当方法接收到不合法的参数值时调用此方法
     * @param e IllegalArgumentException 非法参数异常实例
     * @return Result<Void> 统一的错误响应结果
     */
    @ExceptionHandler(IllegalArgumentException.class) // 指定此方法处理 IllegalArgumentException 类型的异常
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 设置响应状态码为 400 Bad Request（非法参数）
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException e) { // 处理非法参数异常的方法
        log.warn("非法参数异常：{}", e.getMessage()); // 使用日志记录器记录警告日志，输出非法参数的详细信息
        return Result.error(400, e.getMessage()); // 返回统一的错误响应结果，错误码为 400，消息为异常详情
    }

    /**
     * 处理其他未知异常（兜底异常处理器）
     * 当发生未被上述方法捕获的 Exception 类型异常时调用此方法
     * @param e Exception 系统异常实例
     * @return Result<Void> 统一的错误响应结果
     */
    @ExceptionHandler(Exception.class) // 指定此方法处理 Exception 类型的异常（兜底处理）
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // 设置响应状态码为 500 Internal Server Error（服务器内部错误）
    public Result<Void> handleException(Exception e) { // 处理系统异常的方法
        log.error("系统异常：", e); // 使用日志记录器记录错误日志，输出完整的异常堆栈信息
        return Result.error("系统繁忙，请稍后再试"); // 返回统一的错误响应结果，使用通用提示语避免泄露系统细节
    }
}
