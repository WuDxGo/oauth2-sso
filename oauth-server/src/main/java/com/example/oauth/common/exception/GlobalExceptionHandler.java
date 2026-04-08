package com.example.oauth.common.exception;

import com.example.oauth.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器类
 * 使用@RestControllerAdvice注解标识,自动拦截并处理所有Controller抛出的异常
 * 统一异常处理,避免每个Controller都编写try-catch代码
 * 将异常转换为统一的Result格式返回给前端
 * 使用@Slf4j注解自动生成日志记录器,方便记录异常信息
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理自定义的业务异常
     * 当业务逻辑中主动抛出BusinessException时调用此方法
     * 通常用于业务校验失败,如用户不存在、密码错误、余额不足等
     * 返回HTTP状态码200,由Result的code字段区分成功或失败
     *
     * @param e BusinessException业务异常对象,包含错误码和错误消息
     * @return Result统一响应对象,code为错误码,message为错误消息,data为null
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> handleBusinessException(BusinessException e) {
        // 记录业务异常日志,使用warn级别,因为这是预期的业务校验失败
        // 日志包含错误消息,方便排查问题
        log.warn("业务异常：{}", e.getMessage());
        // 返回统一的错误响应,错误码和消息都从BusinessException对象中提取
        // Result.error方法会自动创建code和message, data为null
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理方法参数校验异常
     * 当使用@Valid或@Validated注解验证@RequestBody参数失败时调用此方法
     * 常见场景:前端传入的参数不符合校验规则,如@NotBlank、@Email、@Size等校验失败
     * 返回HTTP状态码400,表示客户端请求参数有误
     *
     * @param e MethodArgumentNotValidException方法参数校验异常对象,包含所有字段错误信息
     * @return Result统一响应对象,code=400,message为所有字段错误信息的拼接,data为null
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        // 从异常对象中获取所有字段错误列表,并转换为Stream流
        // 使用Stream API对错误信息进行处理和拼接
        String message = e.getBindingResult().getFieldErrors().stream()
                // 提取每个字段错误的默认消息(如"不能为空"、"格式不正确")
                .map(FieldError::getDefaultMessage)
                // 使用逗号和空格将所有错误消息连接成一个字符串
                // 如:"用户名不能为空, 邮箱格式不正确"
                .collect(Collectors.joining(", "));
        // 记录参数校验异常日志,使用warn级别,因为这是客户端参数错误
        // 日志包含拼接后的错误消息,方便查看哪些字段校验失败
        log.warn("参数校验异常：{}", message);
        // 返回统一的错误响应,错误码为400(参数错误),消息为拼接后的字段错误信息
        return Result.error(400, message);
    }

    /**
     * 处理参数绑定异常
     * 当@RequestParam或@PathVariable注解的参数类型不匹配或格式错误时调用此方法
     * 常见场景:前端传入的参数类型与后端方法参数类型不一致,如字符串转Long失败
     * 返回HTTP状态码400,表示客户端请求参数格式有误
     *
     * @param e BindException参数绑定异常对象,包含所有字段错误信息
     * @return Result统一响应对象,code=400,message为所有字段错误信息的拼接,data为null
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBindException(BindException e) {
        // 从异常对象中获取所有字段错误列表,并转换为Stream流
        // 使用Stream API对错误信息进行处理和拼接
        String message = e.getBindingResult().getFieldErrors().stream()
                // 提取每个字段错误的默认消息(如"类型转换失败"、"必须是数字")
                .map(FieldError::getDefaultMessage)
                // 使用逗号和空格将所有错误消息连接成一个字符串
                .collect(Collectors.joining(", "));
        // 记录参数绑定异常日志,使用warn级别,因为这是客户端参数错误
        // 日志包含拼接后的错误消息,方便查看哪些参数绑定失败
        log.warn("参数绑定异常：{}", message);
        // 返回统一的错误响应,错误码为400(参数错误),消息为拼接后的绑定错误信息
        return Result.error(400, message);
    }

    /**
     * 处理非法参数异常
     * 当方法接收到不合法的参数值时调用此方法
     * 常见场景:方法内部使用Assert工具类校验参数,校验失败抛出IllegalArgumentException
     * 返回HTTP状态码400,表示客户端传入的参数值不合法
     *
     * @param e IllegalArgumentException非法参数异常对象,包含错误消息
     * @return Result统一响应对象,code=400,message为异常消息,data为null
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        // 记录非法参数异常日志,使用warn级别,因为这是客户端参数错误
        // 日志包含异常消息,通常是参数校验失败的具体描述
        log.warn("非法参数异常：{}", e.getMessage());
        // 返回统一的错误响应,错误码为400(参数错误),消息为异常自带的消息
        // getMessage()方法返回创建异常时传入的描述,如"用户ID不能为空"
        return Result.error(400, e.getMessage());
    }

    /**
     * 处理其他未知异常(兜底异常处理器)
     * 当发生未被上述方法捕获的Exception类型异常时调用此方法
     * 作为全局异常处理的最后一道防线,避免异常信息泄露给前端
     * 捕获所有Exception及其子类,包括NullPointerException、SQLException等
     * 返回HTTP状态码500,表示服务器内部错误
     *
     * @param e Exception系统异常对象,包含异常堆栈信息
     * @return Result统一响应对象,code=500,message为通用提示"data=null
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e) {
        // 记录系统异常日志,使用error级别,因为这是服务器内部错误
        // 记录完整的异常堆栈信息(传入e对象而非e.getMessage()),方便开发人员排查问题
        // 堆栈信息包含异常类型、错误消息、代码行号、调用链等
        log.error("系统异常：", e);
        // 返回统一的错误响应,使用通用提示语"系统繁忙，请稍后再试"
        // 不返回具体的异常信息,避免泄露系统内部实现细节给恶意用户
        // 引导用户稍后重试,而不是立即放弃
        return Result.error("系统繁忙，请稍后再试");
    }
}
