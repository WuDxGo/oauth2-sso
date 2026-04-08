package com.example.oauth.common.result;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一API响应结果封装类
 * 使用泛型T支持任意类型的返回数据
 * 所有Controller接口都返回此格式,保证API响应的一致性
 * 前端可以根据code字段判断成功或失败,从data字段获取业务数据
 * 实现Serializable接口支持序列化和网络传输
 */
@Data
public class Result<T> implements Serializable {

    /**
     * 序列化版本UID
     * 用于反序列化时验证版本兼容性,防止类结构变化导致失败
     */
    private static final long serialVersionUID = 1L;

    /**
     * 业务状态码
     * 用于表示请求处理的结果状态
     * 常见值:200成功,401未授权,403禁止访问,500服务器错误
     * 前端根据此字段判断成功还是失败
     */
    private Integer code;

    /**
     * 响应消息
     * 描述请求处理的详细结果信息
     * 成功时如:"操作成功"、"查询成功"
     * 失败时如:"用户不存在"、"密码错误"、"参数校验失败"
     * 用于前端展示或日志记录
     */
    private String message;

    /**
     * 响应数据
     * 存储实际的业务数据,类型为泛型T
     * 成功时包含查询结果、创建的对象等
     * 失败时通常为null
     * 可以是任意类型:User、List<User>、Map等
     */
    private T data;

    /**
     * 响应时间戳
     * 记录响应生成的Unix时间戳(毫秒级)
     * 由构造函数自动设置为当前系统时间
     * 用于前端调试、日志追踪、接口性能分析
     */
    private Long timestamp;

    /**
     * 无参构造函数
     * 自动将timestamp字段初始化为当前系统时间戳
     * 由Java编译器在创建Result对象时自动调用
     */
    public Result() {
        // 设置时间戳为当前系统时间的毫秒值
        // System.currentTimeMillis()返回从1970年1月1日00:00:00 UTC到现在的毫秒数
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 全参数构造函数
     * 允许在创建对象时一次性设置所有字段
     * timestamp字段会自动设置为当前时间,无需手动传入
     *
     * @param code 业务状态码,如200、401、500等
     * @param message 响应消息,描述处理结果
     * @param data 业务数据,泛型T可以是任意类型
     */
    public Result(Integer code, String message, T data) {
        // 设置业务状态码
        this.code = code;
        // 设置响应消息
        this.message = message;
        // 设置业务数据
        this.data = data;
        // 自动设置时间戳为当前系统时间
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 创建成功的响应对象(无业务数据)
     * 静态工厂方法,简化成功响应的创建
     * 默认状态码200,消息为"操作成功",数据为null
     * 适用于删除、更新等无需返回数据的操作
     *
     * @param <T> 数据类型泛型,通常为Void或Object
     * @return Result对象,code=200, message="操作成功", data=null
     */
    public static <T> Result<T> success() {
        // 返回成功响应,状态码200,消息"操作成功",无数据
        return new Result<>(200, "操作成功", null);
    }

    /**
     * 创建成功的响应对象(带业务数据)
     * 静态工厂方法,简化带数据成功响应的创建
     * 默认状态码200,消息为"操作成功",数据为传入的data
     * 适用于查询、创建等需要返回数据的操作
     *
     * @param data 业务数据对象,如查询到的User、创建的订单等
     * @param <T> 数据类型泛型,与data的类型一致
     * @return Result对象,code=200, message="操作成功", data=传入的数据
     */
    public static <T> Result<T> success(T data) {
        // 返回成功响应,状态码200,消息"操作成功",包含指定的业务数据
        return new Result<>(200, "操作成功", data);
    }

    /**
     * 创建成功的响应对象(自定义消息和业务数据)
     * 静态工厂方法,允许自定义成功消息
     * 状态码固定为200,消息使用传入的message,数据为传入的data
     * 适用于需要给前端展示特定成功提示的场景
     *
     * @param message 自定义的成功消息,如"登录成功"、"创建成功"
     * @param data 业务数据对象,如查询到的User、创建的订单等
     * @param <T> 数据类型泛型,与data的类型一致
     * @return Result对象,code=200, message=自定义消息, data=传入的数据
     */
    public static <T> Result<T> success(String message, T data) {
        // 返回成功响应,状态码200,使用自定义消息,包含指定的业务数据
        return new Result<>(200, message, data);
    }

    /**
     * 创建错误的响应对象(默认服务器错误码500)
     * 静态工厂方法,简化错误响应的创建
     * 默认状态码500,消息为传入的message,数据为null
     * 适用于服务器内部异常、数据库错误等场景
     *
     * @param message 错误消息,描述失败原因,如"系统繁忙"、"数据库连接失败"
     * @param <T> 数据类型泛型,通常为Void或Object
     * @return Result对象,code=500, message=错误消息, data=null
     */
    public static <T> Result<T> error(String message) {
        // 返回错误响应,状态码500(服务器错误),使用指定的错误消息,无数据
        return new Result<>(500, message, null);
    }

    /**
     * 创建错误的响应对象(可自定义错误码)
     * 静态工厂方法,允许自定义错误状态码
     * 状态码和消息都使用传入的参数,数据为null
     * 适用于需要返回特定错误码的场景,如400参数错误、409冲突等
     *
     * @param code 自定义的业务状态码,如400、409、500等
     * @param message 错误消息,描述失败原因
     * @param <T> 数据类型泛型,通常为Void或Object
     * @return Result对象,code=自定义码, message=错误消息, data=null
     */
    public static <T> Result<T> error(Integer code, String message) {
        // 返回错误响应,使用自定义状态码和错误消息,无数据
        return new Result<>(code, message, null);
    }

    /**
     * 创建未授权的响应对象(状态码401)
     * 静态工厂方法,专门用于处理认证失败场景
     * 状态码固定为401,消息为传入的message,数据为null
     * 适用于Token过期、Token无效、未登录等场景
     *
     * @param message 未授权消息,如"未登录"、"Token已过期"、"Token无效"
     * @param <T> 数据类型泛型,通常为Void或Object
     * @return Result对象,code=401, message=未授权消息, data=null
     */
    public static <T> Result<T> unauthorized(String message) {
        // 返回未授权响应,状态码401,使用指定的消息,无数据
        return new Result<>(401, message, null);
    }

    /**
     * 创建禁止访问的响应对象(状态码403)
     * 静态工厂方法,专门用于处理权限不足场景
     * 状态码固定为403,消息为传入的message,数据为null
     * 适用于用户已登录但没有权限访问该资源的场景
     *
     * @param message 禁止访问消息,如"权限不足"、"无权访问"
     * @param <T> 数据类型泛型,通常为Void或Object
     * @return Result对象,code=403, message=禁止访问消息, data=null
     */
    public static <T> Result<T> forbidden(String message) {
        // 返回禁止访问响应,状态码403,使用指定的消息,无数据
        return new Result<>(403, message, null);
    }

    /**
     * 创建失败的响应对象(默认错误码500)
     * 静态工厂方法,与error(String)方法功能相同
     * 语义更清晰,表示请求处理失败
     * 适用于业务逻辑失败、参数校验失败等场景
     *
     * @param message 失败消息,描述失败原因
     * @param <T> 数据类型泛型,通常为Void或Object
     * @return Result对象,code=500, message=失败消息, data=null
     */
    public static <T> Result<T> failure(String message) {
        // 返回失败响应,状态码500,使用指定的失败消息,无数据
        return new Result<>(500, message, null);
    }

    /**
     * 创建失败的响应对象(可自定义错误码)
     * 静态工厂方法,与error(Integer, String)方法功能相同
     * 语义更清晰,允许自定义失败状态码
     * 适用于需要返回特定错误码的失败场景
     *
     * @param code 自定义的业务状态码,如400、422等
     * @param message 失败消息,描述失败原因
     * @param <T> 数据类型泛型,通常为Void或Object
     * @return Result对象,code=自定义码, message=失败消息, data=null
     */
    public static <T> Result<T> failure(Integer code, String message) {
        // 返回失败响应,使用自定义状态码和失败消息,无数据
        return new Result<>(code, message, null);
    }
}
