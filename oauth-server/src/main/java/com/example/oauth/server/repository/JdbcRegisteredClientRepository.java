package com.example.oauth.server.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.jackson2.OAuth2AuthorizationServerJackson2Module;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 基于JDBC的OAuth2注册客户端仓库实现类
 * 实现Spring Security OAuth2的RegisteredClientRepository接口
 * 负责将OAuth2客户端信息持久化到MySQL数据库的oauth2_registered_client表
 * 提供客户端的增删改查功能,支持OAuth2授权服务器的客户端管理
 * 使用JdbcTemplate执行SQL操作,使用ObjectMapper序列化/反序列化复杂配置
 */
@Slf4j
public class JdbcRegisteredClientRepository implements RegisteredClientRepository {

    /**
     * Spring JDBC模板对象
     * 用于执行SQL语句,简化数据库操作
     * 自动处理连接的获取和释放,避免资源泄露
     */
    private final JdbcTemplate jdbcTemplate;

    /**
     * Jackson JSON序列化/反序列化器
     * 用于将ClientSettings和TokenSettings对象转换为JSON字符串存储
     * 以及从JSON字符串还原为Java对象
     * 注册了OAuth2专用的Jackson模块,支持OAuth2特有的数据类型
     */
    private final ObjectMapper objectMapper;

    /**
     * 构造函数,注入JdbcTemplate依赖并初始化ObjectMapper
     * 初始化时注册OAuth2专用的Jackson模块,支持OAuth2数据类型的序列化
     *
     * @param jdbcTemplate Spring JDBC模板对象,用于执行SQL操作
     */
    public JdbcRegisteredClientRepository(JdbcTemplate jdbcTemplate) {
        // 验证jdbcTemplate参数不为null,避免后续出现空指针异常
        Assert.notNull(jdbcTemplate, "jdbcTemplate cannot be null");
        // 将注入的JdbcTemplate赋值给实例变量
        this.jdbcTemplate = jdbcTemplate;
        // 创建新的ObjectMapper实例,用于JSON序列化和反序列化
        this.objectMapper = new ObjectMapper();
        // 注册OAuth2授权服务器专用的Jackson模块
        // 该模块包含OAuth2数据类型的序列化器和反序列化器
        this.objectMapper.registerModule(new OAuth2AuthorizationServerJackson2Module());
        // 配置ObjectMapper在反序列化时忽略未知的JSON字段
        // 避免数据库中的旧数据缺少某些字段导致反序列化失败
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * 保存OAuth2客户端到数据库
     * 实现RegisteredClientRepository接口的方法
     * 如果客户端已存在(根据clientId判断),则先删除旧数据再插入新数据
     * 实现客户端配置的更新效果
     *
     * @param registeredClient OAuth2注册客户端对象,包含完整的客户端配置信息
     */
    @Override
    public void save(RegisteredClient registeredClient) {
        // 验证registeredClient参数不为null
        Assert.notNull(registeredClient, "registeredClient cannot be null");

        // 步骤1:先删除数据库中已存在的同clientId的客户端记录
        // 使用DELETE + INSERT的方式实现更新,避免复杂的主键冲突
        // WHERE条件使用client_id字段,这是客户端的唯一业务标识
        jdbcTemplate.update("DELETE FROM oauth2_registered_client WHERE client_id = ?",
            registeredClient.getClientId());

        // 步骤2:提取客户端认证方式集合,转换为逗号分隔的字符串
        // 遍历ClientAuthenticationMethod对象集合,提取每个对象的value字段(字符串值)
        // 使用逗号连接,如"client_secret_basic,client_secret_post"
        String authMethodsStr = registeredClient.getClientAuthenticationMethods().stream()
            .map(m -> m.getValue())  // 提取认证方式的字符串值
            .collect(Collectors.joining(","));  // 用逗号连接所有值

        // 步骤3:提取OAuth2授权模式集合,转换为逗号分隔的字符串
        // 遍历AuthorizationGrantType对象集合,提取每个对象的value字段(字符串值)
        // 使用逗号连接,如"authorization_code,password,refresh_token"
        String grantTypesStr = registeredClient.getAuthorizationGrantTypes().stream()
            .map(g -> g.getValue())  // 提取授权模式的字符串值
            .collect(Collectors.joining(","));  // 用逗号连接所有值

        // 步骤4:提取重定向URI集合,转换为逗号分隔的字符串
        // 直接使用Spring的StringUtils工具方法,将Set转换为逗号分隔字符串
        // 如"https://example.com/callback,https://example.com/callback2"
        String redirectUrisStr = StringUtils.collectionToDelimitedString(registeredClient.getRedirectUris(), ",");

        // 步骤5:提取授权范围(scope)集合,转换为逗号分隔的字符串
        // 如"read,write,openid,profile"
        String scopesStr = StringUtils.collectionToDelimitedString(registeredClient.getScopes(), ",");

        // 步骤6:将客户端信息插入到oauth2_registered_client表
        // 使用预编译SQL语句,防止SQL注入攻击
        // 按顺序传入参数,与SQL中的?占位符一一对应
        jdbcTemplate.update(
            // 定义INSERT语句,包含客户端的所有配置字段
            "INSERT INTO oauth2_registered_client (" +
            "id, client_id, client_name, client_secret, " +           // 基本信息:主键、客户端ID、名称、密钥
            "client_authentication_methods, authorization_grant_types, " +  // 认证方式和授权模式
            "redirect_uris, scopes, client_settings, token_settings) " +    // 重定向URI、scope、客户端设置、Token设置
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",  // 10个占位符,对应10个参数
            // 传入参数值,按顺序与?占位符对应
            registeredClient.getId(),                                      // 参数1:数据库主键ID(UUID)
            registeredClient.getClientId(),                                // 参数2:客户端业务标识
            registeredClient.getClientName(),                              // 参数3:客户端显示名称
            registeredClient.getClientSecret(),                            // 参数4:客户端密钥(已加密)
            authMethodsStr,                                                // 参数5:认证方式字符串(逗号分隔)
            grantTypesStr,                                                 // 参数6:授权模式字符串(逗号分隔)
            redirectUrisStr,                                               // 参数7:重定向URI字符串(逗号分隔)
            scopesStr,                                                     // 参数8:授权范围字符串(逗号分隔)
            serializeClientSettings(registeredClient.getClientSettings()), // 参数9:客户端设置的JSON字符串
            serializeTokenSettings(registeredClient.getTokenSettings())    // 参数10:Token设置的JSON字符串
        );
    }

    /**
     * 根据数据库主键ID查询OAuth2客户端
     * 实现RegisteredClientRepository接口的方法
     * 用于通过UUID查询客户端,通常用于内部管理场景
     *
     * @param id 数据库主键ID,UUID格式的字符串
     * @return RegisteredClient对象,包含完整的客户端配置;如果客户端不存在则返回null
     */
    @Override
    public RegisteredClient findById(String id) {
        // 验证id参数不为null且不是空字符串
        Assert.hasText(id, "id cannot be empty");
        // 定义查询SQL语句,根据主键ID查询客户端记录
        String sql = "SELECT * FROM oauth2_registered_client WHERE id = ?";
        try {
            // 执行查询SQL,使用mapRowToRegisteredClient方法将ResultSet行映射为RegisteredClient对象
            // queryForObject方法在查询结果为空时会抛出异常,因此在catch块中处理
            return jdbcTemplate.queryForObject(sql, this::mapRowToRegisteredClient, id);
        } catch (Exception e) {
            // 查询失败(客户端不存在或数据库异常),记录警告日志
            // 日志包含查询的ID和错误信息,方便排查问题
            log.warn("根据ID查询客户端失败，ID: {}, 错误: {}", id, e.getMessage());
            // 返回null表示客户端不存在,而不是抛出异常
            return null;
        }
    }

    /**
     * 根据客户端业务标识(clientId)查询OAuth2客户端
     * 实现RegisteredClientRepository接口的方法
     * 用于OAuth2授权流程中通过clientId识别客户端身份
     *
     * @param clientId 客户端唯一业务标识,如"order-service"、"gateway-client"
     * @return RegisteredClient对象,包含完整的客户端配置;如果客户端不存在则返回null
     */
    @Override
    public RegisteredClient findByClientId(String clientId) {
        // 验证clientId参数不为null且不是空字符串
        Assert.hasText(clientId, "clientId cannot be empty");
        // 定义查询SQL语句,根据客户端业务标识查询客户端记录
        String sql = "SELECT * FROM oauth2_registered_client WHERE client_id = ?";
        try {
            // 执行查询SQL,使用mapRowToRegisteredClient方法将ResultSet行映射为RegisteredClient对象
            return jdbcTemplate.queryForObject(sql, this::mapRowToRegisteredClient, clientId);
        } catch (Exception e) {
            // 查询失败(客户端不存在或数据库异常),记录警告日志
            // 日志包含查询的ClientId和错误信息,方便排查问题
            log.warn("根据ClientId查询客户端失败，ClientId: {}, 错误: {}", clientId, e.getMessage());
            // 返回null表示客户端不存在,而不是抛出异常
            return null;
        }
    }

    /**
     * 查询数据库中所有的OAuth2客户端
     * 自定义方法,不在RegisteredClientRepository接口中定义
     * 用于客户端管理界面展示所有客户端列表
     *
     * @return RegisteredClient列表,包含所有客户端的配置信息;如果查询失败则抛出RuntimeException
     */
    public List<RegisteredClient> findAll() {
        // 定义查询SQL语句,查询oauth2_registered_client表的所有记录
        String sql = "SELECT * FROM oauth2_registered_client";
        try {
            // 执行查询SQL,使用mapRowToRegisteredClient方法将每行ResultSet映射为RegisteredClient对象
            // query方法返回List集合,包含所有查询结果
            return jdbcTemplate.query(sql, this::mapRowToRegisteredClient);
        } catch (Exception e) {
            // 查询失败(数据库异常),记录错误日志并包含堆栈信息
            log.error("查询所有客户端失败: {}", e.getMessage(), e);
            // 抛出RuntimeException,包装原始异常,方便上层处理
            throw new RuntimeException("查询所有客户端失败", e);
        }
    }

    /**
     * 根据客户端业务标识(clientId)删除OAuth2客户端
     * 自定义方法,不在RegisteredClientRepository接口中定义
     * 用于客户端管理界面的删除功能
     * 删除后该客户端将无法再进行OAuth2认证和获取Token
     *
     * @param clientId 客户端唯一业务标识,指定要删除的客户端
     */
    public void deleteByClientId(String clientId) {
        // 验证clientId参数不为null且不是空字符串
        Assert.hasText(clientId, "clientId cannot be empty");
        // 定义删除SQL语句,根据客户端业务标识删除客户端记录
        String sql = "DELETE FROM oauth2_registered_client WHERE client_id = ?";
        // 执行删除SQL,传入clientId参数
        // 即使客户端不存在也不会抛出异常,影响行数为0
        jdbcTemplate.update(sql, clientId);
    }

    /**
     * 将数据库ResultSet的一行记录映射为RegisteredClient对象
     * 由JdbcTemplate在查询时自动调用,每行调用一次
     * 负责从数据库字段值还原为完整的RegisteredClient对象
     *
     * @param rs ResultSet对象,包含数据库查询结果
     * @param rowNum 当前行号,从0开始,通常不需要使用
     * @return RegisteredClient对象,包含还原后的完整客户端配置
     * @throws java.sql.SQLException 当读取ResultSet字段失败时抛出
     */
    private RegisteredClient mapRowToRegisteredClient(java.sql.ResultSet rs, int rowNum)
            throws java.sql.SQLException {

        // 步骤1:从ResultSet中提取基本字段值
        // 使用getString方法读取VARCHAR类型的字段
        String id = rs.getString("id");                          // 读取数据库主键ID(UUID)
        String clientId = rs.getString("client_id");             // 读取客户端业务标识
        String clientName = rs.getString("client_name");         // 读取客户端显示名称
        String clientSecret = rs.getString("client_secret");     // 读取客户端密钥(已加密)

        // 步骤2:解析客户端认证方式字符串,转换为ClientAuthenticationMethod对象集合
        // 从数据库读取逗号分隔的字符串,如"client_secret_basic,client_secret_post"
        // 需要解析为Set<ClientAuthenticationMethod>对象集合
        Set<ClientAuthenticationMethod> clientAuthenticationMethods = parseStringValue(rs.getString("client_authentication_methods"));

        // 步骤3:解析OAuth2授权模式字符串,转换为AuthorizationGrantType对象集合
        // 从数据库读取逗号分隔的字符串,如"authorization_code,password,refresh_token"
        // 需要解析为Set<AuthorizationGrantType>对象集合
        Set<AuthorizationGrantType> authorizationGrantTypes = parseAuthorizationGrantTypes(rs.getString("authorization_grant_types"));

        // 步骤4:解析重定向URI字符串,转换为Set集合
        // 使用Spring的StringUtils工具方法,将逗号分隔的字符串转换为Set集合
        // 自动处理空字符串和null的情况
        Set<String> redirectUris = StringUtils.commaDelimitedListToSet(rs.getString("redirect_uris"));

        // 步骤5:解析授权范围(scope)字符串,转换为Set集合
        // 使用Spring的StringUtils工具方法,将逗号分隔的字符串转换为Set集合
        Set<String> scopes = StringUtils.commaDelimitedListToSet(rs.getString("scopes"));

        // 步骤6:反序列化客户端配置对象
        // 从数据库读取JSON字符串,还原为ClientSettings和TokenSettings对象
        ClientSettings clientSettings = deserializeClientSettings(rs);
        TokenSettings tokenSettings = deserializeTokenSettings(rs);

        // 步骤7:使用构建器模式重建RegisteredClient对象
        // 将所有解析后的字段逐个设置到构建器中
        RegisteredClient.Builder builder = RegisteredClient.withId(id)  // 设置数据库主键ID
            .clientId(clientId)                                        // 设置客户端业务标识
            .clientName(clientName)                                    // 设置客户端显示名称
            .clientSecret(clientSecret)                                // 设置客户端密钥
            // 添加客户端认证方式集合
            .clientAuthenticationMethods(authMethods -> authMethods.addAll(clientAuthenticationMethods))
            // 添加OAuth2授权模式集合
            .authorizationGrantTypes(grantTypes -> grantTypes.addAll(authorizationGrantTypes))
            // 添加重定向URI集合
            .redirectUris(uris -> uris.addAll(redirectUris))
            // 添加授权范围集合
            .scopes(s -> s.addAll(scopes))
            // 设置客户端配置对象
            .clientSettings(clientSettings)
            // 设置Token配置对象
            .tokenSettings(tokenSettings);

        // 步骤8:完成构建,返回不可变的RegisteredClient对象
        return builder.build();
    }

    /**
     * 解析客户端认证方式字符串,转换为ClientAuthenticationMethod对象集合
     * 处理逗号分隔的字符串,如"client_secret_basic,client_secret_post"
     * 兼容旧数据格式,处理对象toString()格式的情况
     *
     * @param value 逗号分隔的认证方式字符串,可能包含简单值或对象toString格式
     * @return ClientAuthenticationMethod对象集合
     */
    private Set<ClientAuthenticationMethod> parseStringValue(String value) {
        // 创建空的HashSet,用于存储解析后的ClientAuthenticationMethod对象
        Set<ClientAuthenticationMethod> result = new HashSet<>();
        // 检查输入字符串是否为null或空字符串
        if (!StringUtils.hasText(value)) {
            // 如果为空,返回空集合,避免后续处理出现空指针
            return result;
        }

        // 将逗号分隔的字符串拆分为多个子串,逐个进行解析
        for (String item : StringUtils.commaDelimitedListToSet(value)) {
            // 情况1:处理对象toString()格式,如"ClientAuthenticationMethod{value='client_secret_basic'}"
            // 这种格式可能是旧版本序列化产生的数据
            if (item.contains("{value='")) {
                // 提取单引号中的实际值部分
                // 找到"{value='"的起始位置,加上8个字符长度跳过前缀
                int start = item.indexOf("{value='") + 8;
                // 找到下一个单引号的位置,作为值的结束位置
                int end = item.indexOf("'", start);
                // 验证起始和结束位置有效
                if (end > start) {
                    // 截取实际的认证方式字符串,并创建ClientAuthenticationMethod对象
                    result.add(new ClientAuthenticationMethod(item.substring(start, end)));
                }
            } else {
                // 情况2:普通格式,直接创建ClientAuthenticationMethod对象
                // 如"client_secret_basic"、"client_secret_post"
                result.add(new ClientAuthenticationMethod(item));
            }
        }
        // 返回解析后的ClientAuthenticationMethod对象集合
        return result;
    }

    /**
     * 解析OAuth2授权模式字符串,转换为AuthorizationGrantType对象集合
     * 处理逗号分隔的字符串,如"authorization_code,password,refresh_token"
     * 兼容旧数据格式,处理对象toString()格式的情况
     *
     * @param value 逗号分隔的授权模式字符串,可能包含简单值或对象toString格式
     * @return AuthorizationGrantType对象集合
     */
    private Set<AuthorizationGrantType> parseAuthorizationGrantTypes(String value) {
        // 创建空的HashSet,用于存储解析后的AuthorizationGrantType对象
        Set<AuthorizationGrantType> result = new HashSet<>();
        // 检查输入字符串是否为null或空字符串
        if (!StringUtils.hasText(value)) {
            // 如果为空,返回空集合,避免后续处理出现空指针
            return result;
        }

        // 将逗号分隔的字符串拆分为多个子串,逐个进行解析
        for (String item : StringUtils.commaDelimitedListToSet(value)) {
            // 情况1:处理对象toString()格式,如"AuthorizationGrantType{value='authorization_code'}"
            // 这种格式可能是旧版本序列化产生的数据
            if (item.contains("{value='")) {
                // 提取单引号中的实际值部分
                // 找到"{value='"的起始位置,加上8个字符长度跳过前缀
                int start = item.indexOf("{value='") + 8;
                // 找到下一个单引号的位置,作为值的结束位置
                int end = item.indexOf("'", start);
                // 验证起始和结束位置有效
                if (end > start) {
                    // 截取实际的授权模式字符串,并创建AuthorizationGrantType对象
                    result.add(new AuthorizationGrantType(item.substring(start, end)));
                }
            } else {
                // 情况2:普通格式,直接创建AuthorizationGrantType对象
                // 如"authorization_code"、"password"、"client_credentials"
                result.add(new AuthorizationGrantType(item));
            }
        }
        // 返回解析后的AuthorizationGrantType对象集合
        return result;
    }

    /**
     * 序列化ClientSettings对象为JSON字符串
     * 用于将客户端配置对象存储到数据库的VARCHAR字段
     * 如果序列化失败,返回空JSON对象"{}"作为默认值
     *
     * @param settings ClientSettings客户端配置对象
     * @return JSON格式的字符串,表示客户端配置;如果为null或失败则返回"{}"
     */
    private String serializeClientSettings(ClientSettings settings) {
        // 检查settings对象是否为null
        if (settings == null) {
            // 为null时返回空JSON对象字符串,避免数据库存储NULL
            return "{}";
        }
        try {
            // 使用ObjectMapper将ClientSettings对象序列化为JSON字符串
            // 如{"require-authorization-consent":true,"require-proof-key":false}
            return objectMapper.writeValueAsString(settings);
        } catch (Exception e) {
            // 序列化失败(如对象格式异常),记录错误日志并包含堆栈信息
            log.error("序列化 ClientSettings 失败", e);
            // 返回空JSON对象字符串作为默认值,避免存储NULL
            return "{}";
        }
    }

    /**
     * 序列化TokenSettings对象为JSON字符串
     * 用于将Token配置对象存储到数据库的VARCHAR字段
     * 如果序列化失败,返回空JSON对象"{}"作为默认值
     *
     * @param settings TokenSettings的Token配置对象
     * @return JSON格式的字符串,表示Token配置;如果为null或失败则返回"{}"
     */
    private String serializeTokenSettings(TokenSettings settings) {
        // 检查settings对象是否为null
        if (settings == null) {
            // 为null时返回空JSON对象字符串,避免数据库存储NULL
            return "{}";
        }
        try {
            // 使用ObjectMapper将TokenSettings对象序列化为JSON字符串
            // 如{"access-token-time-to-live":7200,"refresh-token-time-to-live":604800}
            return objectMapper.writeValueAsString(settings);
        } catch (Exception e) {
            // 序列化失败(如对象格式异常),记录错误日志并包含堆栈信息
            log.error("序列化 TokenSettings 失败", e);
            // 返回空JSON对象字符串作为默认值,避免存储NULL
            return "{}";
        }
    }

    /**
     * 从数据库ResultSet行反序列化ClientSettings对象
     * 读取client_settings字段的JSON字符串,还原为ClientSettings对象
     * 如果解析失败,返回具有默认值的ClientSettings对象
     *
     * @param rs ResultSet对象,包含数据库查询结果
     * @return ClientSettings客户端配置对象;如果解析失败则返回默认配置
     */
    private ClientSettings deserializeClientSettings(java.sql.ResultSet rs) {
        try {
            // 初始化客户端配置的默认值
            // requireConsent:是否需要用户授权同意,默认false(不需要)
            boolean requireConsent = false;
            // requireProofKey:是否需要PKCE(Proof Key for Code Exchange),默认false
            boolean requireProofKey = false;

            // 从ResultSet中读取client_settings字段的JSON字符串
            String clientSettingsStr = rs.getString("client_settings");
            // 检查JSON字符串是否非空且不等于空对象"{}"
            if (StringUtils.hasText(clientSettingsStr) && !clientSettingsStr.trim().equals("{}")) {
                // 尝试从JSON字符串中提取require-authorization-consent的值
                // 通过字符串匹配判断是否包含"true",简化解析逻辑
                if (clientSettingsStr.contains("require-authorization-consent")) {
                    // 如果JSON中包含"true",则设置为true,否则为false
                    requireConsent = clientSettingsStr.contains("true");
                }
                // 尝试从JSON字符串中提取require-proof-key的值
                if (clientSettingsStr.contains("require-proof-key")) {
                    // 如果JSON中包含"true",则设置为true,否则为false
                    requireProofKey = clientSettingsStr.contains("true");
                }
            }

            // 使用构建器模式创建ClientSettings对象
            return ClientSettings.builder()
                // 设置是否需要用户授权同意
                .requireAuthorizationConsent(requireConsent)
                // 设置是否需要PKCE验证
                .requireProofKey(requireProofKey)
                // 完成构建,返回不可变的ClientSettings对象
                .build();
        } catch (Exception e) {
            // 反序列化失败(如JSON格式错误),记录警告日志
            log.warn("反序列化 ClientSettings 失败，使用默认值：{}", e.getMessage());
            // 返回具有默认值的ClientSettings对象,避免查询失败
            return ClientSettings.builder().build();
        }
    }

    /**
     * 从数据库ResultSet行反序列化TokenSettings对象
     * 读取token_settings字段的JSON字符串,还原为TokenSettings对象
     * 如果解析失败,返回具有默认值的TokenSettings对象
     *
     * @param rs ResultSet对象,包含数据库查询结果
     * @return TokenSettings的Token配置对象;如果解析失败则返回默认配置
     */
    private TokenSettings deserializeTokenSettings(java.sql.ResultSet rs) {
        try {
            // 从ResultSet中读取token_settings字段的JSON字符串
            String tokenSettingsStr = rs.getString("token_settings");

            // 创建TokenSettings的构建器,用于逐步设置Token配置
            TokenSettings.Builder builder = TokenSettings.builder();

            // 检查JSON字符串是否非空且不等于空对象"{}"
            if (StringUtils.hasText(tokenSettingsStr) && !tokenSettingsStr.trim().equals("{}")) {
                // 解析accessTokenTimeToLive(访问令牌有效期)
                // 默认值为7200秒(2小时),通过字符串匹配判断
                if (tokenSettingsStr.contains("7200")) {
                    // 设置访问令牌有效期为7200秒
                    builder.accessTokenTimeToLive(Duration.ofSeconds(7200));
                }
                // 解析refreshTokenTimeToLive(刷新令牌有效期)
                // 默认值为604800秒(7天),通过字符串匹配判断
                if (tokenSettingsStr.contains("604800")) {
                    // 设置刷新令牌有效期为604800秒
                    builder.refreshTokenTimeToLive(Duration.ofSeconds(604800));
                }
                // 解析authorizationCodeTimeToLive(授权码有效期)
                // 默认值为300秒(5分钟),通过字符串匹配判断
                if (tokenSettingsStr.contains("300")) {
                    // 设置授权码有效期为300秒
                    builder.authorizationCodeTimeToLive(Duration.ofSeconds(300));
                }
                // 解析idTokenSignatureAlgorithm(ID Token签名算法)
                // 默认值为RS256(RSA + SHA256),通过字符串匹配判断
                if (tokenSettingsStr.contains("RS256")) {
                    // 设置签名算法为RS256
                    builder.idTokenSignatureAlgorithm(SignatureAlgorithm.RS256);
                }
            }

            // 完成构建,返回不可变的TokenSettings对象
            return builder.build();
        } catch (Exception e) {
            // 反序列化失败(如JSON格式错误),记录警告日志
            log.warn("反序列化 TokenSettings 失败，使用默认值：{}", e.getMessage());
            // 返回具有默认值的TokenSettings对象,避免查询失败
            return TokenSettings.builder().build();
        }
    }
}
