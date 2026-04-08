package com.example.oauth.server.controller;

import com.example.oauth.server.config.JwtProperties;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

/**
 * 认证控制器
 * 提供基于RESTful风格的用户认证接口
 * 支持JWT Token的生成、验证和用户信息获取
 * 包含标准登录接口和OAuth2密码模式兼容接口
 * 使用@RestController注解,所有接口返回JSON格式数据
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthController {

    /**
     * Spring Security认证管理器
     * 负责协调认证流程,委托给具体的AuthenticationProvider处理认证
     * 通过构造器注入,由@RequiredArgsConstructor自动生成构造函数
     */
    private final AuthenticationManager authenticationManager;

    /**
     * JWT令牌编码器
     * 用于创建和签名JWT Token,确保Token的安全性和完整性
     * 使用配置的密钥对JWT进行数字签名
     */
    private final JwtEncoder jwtEncoder;

    /**
     * JWT配置属性对象
     * 包含Token的颁发者URL、有效期等配置参数
     * 从application.yml配置文件中自动加载
     */
    private final JwtProperties jwtProperties;

    /**
     * 登录请求数据传输对象
     * 用于接收前端提交的JSON格式登录凭据
     * 使用Lombok的@Data注解自动生成getter/setter/toString等方法
     */
    @Data
    public static class LoginRequest {
        /** 用户登录账号,可以是用户名、邮箱或手机号 */
        private String username;
        /** 用户登录密码,明文传输但在服务端会进行加密比对 */
        private String password;
    }

    /**
     * 登录响应数据传输对象
     * 封装JWT Token及其元信息返回给客户端
     * 符合OAuth2标准的Token响应格式
     */
    @Data
    public static class LoginResponse {
        /** JWT访问令牌字符串,客户端需保存并在后续请求中携带 */
        private String access_token;
        /** Token类型,固定值为"Bearer",表示持有者令牌 */
        private String token_type = "Bearer";
        /** Token有效期,单位为秒,从Token颁发时间开始计算 */
        private long expires_in;
        /** Token授权范围,空格分隔的权限列表,用于控制访问权限 */
        private String scope;

        /**
         * 构造登录响应对象
         * 初始化Token基本信息,scope使用默认值
         *
         * @param accessToken 已签名的JWT Token字符串
         * @param expiresIn Token有效期限,单位为秒
         */
        public LoginResponse(String accessToken, long expiresIn) {
            // 设置JWT访问令牌
            this.access_token = accessToken;
            // 设置Token有效期
            this.expires_in = expiresIn;
            // 设置默认授权范围为"read write",表示读写权限
            this.scope = "read write";
        }
    }

    /**
     * 前后端分离的登录接口
     * 接收JSON格式的用户名和密码,验证成功后返回JWT Token
     * 适用于现代前端应用,如Vue、React等
     *
     * @param request 登录请求体,包含username和password字段
     * @return 登录响应对象,包含JWT Token、类型、有效期等信息
     */
    @PostMapping("/api/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        // 步骤1:创建认证令牌对象,封装用户名和密码
        // UsernamePasswordAuthenticationToken是Spring Security的标准认证令牌
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // 步骤2:获取当前UTC时间戳,作为Token的颁发时间
        // Instant.now()返回当前时刻的精确时间
        Instant now = Instant.now();

        // 从配置文件中读取Token的有效期设置(秒)
        long ttl = jwtProperties.getAccessTokenTtl();

        // 计算Token的过期时间 = 颁发时间 + 有效期
        Instant expiry = now.plus(ttl, ChronoUnit.SECONDS);

        // 从认证对象中提取用户的权限列表
        // 将权限对象流转换为权限名称字符串流,再用空格拼接成单个字符串
        String scope = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority) // 提取每个权限的名称
            .collect(Collectors.joining(" "));   // 用空格连接所有权限名称

        // 构建JWT的Claims集合,即Token的声明信息(载荷部分)
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer(jwtProperties.getIssuer())      // 设置Token颁发者URL,用于标识Token来源
            .issuedAt(now)                          // 设置Token颁发时间戳
            .expiresAt(expiry)                      // 设置Token过期时间戳
            .subject(authentication.getName())      // 设置Token主题,通常是用户名或用户ID
            .claim("scope", scope)                  // 添加自定义声明,存储用户权限列表
            .build();                               // 构建不可变的Claims对象

        // 使用JwtEncoder对Claims进行数字签名,生成JWT Token字符串
        // JwtEncoderParameters封装了Claims,编码器会使用配置的私钥进行签名
        String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        // 创建并返回登录响应对象,包含Token和有效期
        return new LoginResponse(token, ttl);
    }

    /**
     * OAuth2密码模式兼容的登录接口
     * 支持标准OAuth2密码授权模式(Password Grant Type)
     * 接收application/x-www-form-urlencoded格式的参数
     * 适用于传统OAuth2客户端或需要兼容OAuth2标准的场景
     *
     * @param username 用户名,必填参数
     * @param password 密码,必填参数
     * @param grant_type 授权类型,默认为"password",符合OAuth2规范
     * @param scope 授权范围,默认为"read write",可自定义
     * @return 登录响应对象,包含JWT Token及相关元信息
     */
    @PostMapping("/oauth2/token")
    public LoginResponse oauth2Login(@RequestParam String username,
                                      @RequestParam String password,
                                      @RequestParam(defaultValue = "password") String grant_type,
                                      @RequestParam(defaultValue = "read write") String scope) {
        // 步骤1:使用认证管理器验证用户名和密码的正确性
        // 认证管理器会调用UserDetailsService加载用户信息并比对密码
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(username, password)
        );

        // 步骤2:获取当前UTC时间戳,用于Token时间戳设置
        Instant now = Instant.now();

        // 从配置中获取Token的有效期(秒),如7200秒=2小时
        long ttl = jwtProperties.getAccessTokenTtl();

        // 计算Token过期时间戳 = 当前时间 + 有效期
        Instant expiry = now.plus(ttl, ChronoUnit.SECONDS);

        // 构建JWT Claims集合,定义Token的各项声明
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer(jwtProperties.getIssuer())      // 颁发者URL,标识Token由哪个服务签发
            .issuedAt(now)                          // 颁发时间,Token的生效时间
            .expiresAt(expiry)                      // 过期时间,Token的失效时间
            .subject(authentication.getName())      // 主题字段,存储用户名或用户唯一标识
            .claim("scope", scope)                  // 自定义scope声明,定义Token的权限范围
            .build();                               // 构建不可变的Claims对象

        // 使用JwtEncoder对Claims进行签名生成JWT Token
        // 签名算法由配置决定,通常使用RS256非对称加密
        String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        // 构建并返回登录响应,客户端获得Token后可用于后续请求认证
        return new LoginResponse(token, ttl);
    }

    /**
     * 获取当前登录用户信息的接口
     * 这是一个OAuth2资源服务器兼容的受保护端点
     * 请求头中必须携带有效的JWT Token才能访问
     * 返回当前认证用户的详细信息
     *
     * @param authentication Spring Security自动注入的当前认证对象
     *                      由Security过滤器链从JWT Token中解析并设置
     * @return 用户信息对象,包含用户名、昵称和权限列表
     */
    @GetMapping("/api/users/me")
    public UserInfo getCurrentUser(Authentication authentication) {
        // 创建用户信息数据传输对象
        UserInfo info = new UserInfo();

        // 设置用户名,从认证对象中获取主题(用户名)
        info.setUsername(authentication.getName());

        // 设置用户昵称,暂时与用户名保持一致
        // 后续可从数据库或用户服务中获取更详细的用户资料
        info.setNickname(authentication.getName());

        // 提取用户的权限列表,将GrantedAuthority对象转换为权限名称字符串列表
        info.setAuthorities(authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)  // 提取每个权限的名称
            .collect(Collectors.toList()));       // 收集为List集合

        // 返回填充完整的用户信息对象,会被Spring MVC序列化为JSON
        return info;
    }

    /**
     * 用户信息数据传输对象
     * 封装当前登录用户的基本信息和权限数据
     * 用于响应获取用户信息的请求
     */
    @Data
    public static class UserInfo {
        /** 用户登录账号,唯一标识用户身份 */
        private String username;
        /** 用户显示名称,用于界面展示 */
        private String nickname;
        /** 用户拥有的权限列表,用于前端权限控制和菜单显示 */
        private java.util.List<String> authorities;
    }
}
