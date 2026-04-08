package com.example.oauth.server.service;

import com.example.oauth.server.repository.JdbcRegisteredClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * OAuth2客户端自动注册服务
 * 实现ApplicationRunner接口,在Spring Boot应用启动完成后自动执行
 * 负责读取配置文件中的客户端配置,并自动注册到数据库中
 * 避免每次部署都手动创建客户端,提高运维效率
 * 使用@Transactional保证客户端注册的原子性
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2ClientAutoRegisterService implements ApplicationRunner {

    /**
     * OAuth2客户端配置属性对象
     * 从application.yml配置文件中加载客户端配置信息
     * 包含是否启用自动注册、默认客户端列表等参数
     */
    private final OAuth2ClientProperties clientProperties;

    /**
     * 客户端数据访问仓库
     * 负责执行客户端信息的数据库操作(查询、创建、更新、删除)
     */
    private final JdbcRegisteredClientRepository clientRepository;

    /**
     * 密码编码器
     * 用于对客户端密钥进行加密存储,防止明文密码泄露
     * 通常使用BCrypt强哈希算法
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * Spring Boot应用启动完成后的回调方法
     * 由ApplicationRunner接口定义,在应用上下文完全初始化后自动调用
     * 检查是否启用自动注册,如果启用则创建默认客户端
     *
     * @param args 应用启动参数,通常不需要使用
     */
    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // 检查配置文件中是否启用了自动注册功能
        // 如果autoRegister为false,则直接返回,不执行后续注册逻辑
        if (!clientProperties.isAutoRegister()) {
            // 自动注册已禁用,跳过客户端注册流程
            return;
        }

        // 启用自动注册,开始创建默认客户端
        // 调用创建方法,从配置中读取客户端列表并注册
        createDefaultClients();
    }

    /**
     * 创建配置文件定义的默认客户端
     * 遍历配置中的defaults列表,逐个注册客户端
     * 如果客户端已存在则会先删除再重新创建(相当于更新)
     * 使用@Transactional保证所有客户端要么全部创建成功,要么全部回滚
     */
    @Transactional
    public void createDefaultClients() {
        // 从配置属性中获取默认客户端列表
        List<OAuth2ClientProperties.ClientConfig> defaultClients = clientProperties.getDefaults();

        // 检查默认客户端列表是否为空或未配置
        if (defaultClients == null || defaultClients.isEmpty()) {
            // 记录警告日志,提醒开发者未配置默认客户端
            log.warn("未配置默认客户端，跳过自动注册");
            // 没有需要注册的客户端,直接返回
            return;
        }

        // 遍历所有默认客户端配置,逐个进行注册
        for (OAuth2ClientProperties.ClientConfig config : defaultClients) {
            // 调用单个客户端注册方法
            registerClient(config);
        }
    }

    /**
     * 注册单个OAuth2客户端到数据库
     * 如果客户端已存在则先删除再创建(实现更新效果)
     * 使用构建器模式逐步构建RegisteredClient对象
     *
     * @param config 客户端配置对象,包含客户端的所有配置参数
     */
    @Transactional
    public void registerClient(OAuth2ClientProperties.ClientConfig config) {
        // 步骤1:检查该客户端是否已在数据库中存在
        // 根据客户端ID查询,如果存在则返回完整的RegisteredClient对象
        RegisteredClient existingClient = clientRepository.findByClientId(config.getClientId());

        // 步骤2:如果客户端已存在,先删除旧数据再创建新的
        // 这样可以实现更新客户端配置的效果
        if (existingClient != null) {
            // 根据客户端ID从数据库删除现有客户端及其所有配置
            clientRepository.deleteByClientId(config.getClientId());
        }

        // 步骤3:使用构建器模式创建新的RegisteredClient对象
        // 生成新的UUID作为数据库主键,确保唯一性
        RegisteredClient.Builder builder = RegisteredClient.withId(java.util.UUID.randomUUID().toString())
                // 设置客户端唯一业务标识,第三方应用通过此识别自己
                .clientId(config.getClientId())
                // 设置客户端显示名称,如果未配置则使用clientId作为名称
                .clientName(config.getClientName() != null ? config.getClientName() : config.getClientId())
                // 设置客户端密钥,使用PasswordEncoder进行加密存储(BCrypt)
                .clientSecret(passwordEncoder.encode(config.getClientSecret()));

        // 步骤4:解析并添加客户端认证方式
        // 从配置中读取逗号分隔的认证方式字符串,转换为Set集合
        Set<ClientAuthenticationMethod> authMethods = parseCommaSeparatedValues(config.getAuthenticationMethods())
                .stream()
                .map(ClientAuthenticationMethod::new)  // 将每个字符串转换为ClientAuthenticationMethod对象
                .collect(Collectors.toSet());          // 收集为Set集合
        // 将所有认证方式添加到构建器
        builder.clientAuthenticationMethods(methods -> methods.addAll(authMethods));

        // 步骤5:解析并添加OAuth2授权模式
        // 从配置中读取逗号分隔的授权模式字符串,转换为Set集合
        Set<AuthorizationGrantType> grantTypes = parseCommaSeparatedValues(config.getGrantTypes())
                .stream()
                .map(AuthorizationGrantType::new)      // 将每个字符串转换为AuthorizationGrantType对象
                .collect(Collectors.toSet());          // 收集为Set集合
        // 将所有授权模式添加到构建器
        builder.authorizationGrantTypes(types -> types.addAll(grantTypes));

        // 步骤6:解析并添加重定向URI列表
        // 仅当配置了重定向URI且不为空字符串时才处理
        if (StringUtils.hasText(config.getRedirectUris())) {
            // 解析逗号分隔的重定向URI字符串,逐个添加到构建器
            parseCommaSeparatedValues(config.getRedirectUris())
                    .forEach(builder::redirectUri);
        }

        // 步骤7:解析并添加授权范围列表
        // 从配置中读取逗号分隔的scope字符串,逐个添加到构建器
        parseCommaSeparatedValues(config.getScopes())
                .forEach(builder::scope);

        // 步骤8:配置Token相关设置,控制Token的生命周期
        TokenSettings tokenSettings = TokenSettings.builder()
                // 设置访问令牌(Access Token)的有效期,从配置中读取(秒)
                .accessTokenTimeToLive(Duration.ofSeconds(config.getAccessTokenTtl()))
                // 设置刷新令牌(Refresh Token)的有效期,从配置中读取(秒)
                .refreshTokenTimeToLive(Duration.ofSeconds(config.getRefreshTokenTtl()))
                .build();
        // 将Token设置应用到客户端构建器
        builder.tokenSettings(tokenSettings);

        // 步骤9:配置客户端相关设置,控制客户端的授权行为
        ClientSettings clientSettings = ClientSettings.builder()
                // 设置是否需要用户授权同意(首次登录是否弹出确认页)
                .requireAuthorizationConsent(config.getRequireConsent())
                .build();
        // 将客户端设置应用到客户端构建器
        builder.clientSettings(clientSettings);

        // 步骤10:完成构建,生成不可变的RegisteredClient对象
        RegisteredClient registeredClient = builder.build();
        // 将客户端信息保存到数据库,完成注册流程
        clientRepository.save(registeredClient);
    }

    /**
     * 解析逗号分隔的字符串为Set集合
     * 用于处理配置文件中的列表字段,如授权模式、认证方式等
     * 会自动去除空格并过滤空字符串
     *
     * @param values 逗号分隔的字符串,如"read,write,openid"
     * @return 解析后的Set集合,如["read", "write", "openid"]
     */
    private Set<String> parseCommaSeparatedValues(String values) {
        // 检查输入字符串是否为null或空字符串
        if (!StringUtils.hasText(values)) {
            // 如果为空,返回空Set,避免后续处理出现空指针
            return Set.of();
        }
        // 按逗号拆分字符串,得到字符串数组
        // 对数组进行流式处理:去除每个元素的首尾空格,过滤掉空字符串
        // 最后收集为Set集合,自动去重
        return Arrays.stream(values.split(","))
                .map(String::trim)                   // 去除每个元素的首尾空格
                .filter(StringUtils::hasText)        // 过滤掉空字符串
                .collect(Collectors.toSet());        // 收集为Set集合并返回
    }
}
