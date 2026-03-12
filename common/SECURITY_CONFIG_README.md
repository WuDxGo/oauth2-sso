# 通用安全配置使用说明

## 📦 模块位置
`common` 模块中的 `ResourceServerSecurityConfig`

## ✅ 适用服务
所有需要 JWT 认证的资源服务（如：user-service, order-service, product-service 等）

## 🚀 使用方式

### 方式 1：自动生效（推荐）
**不需要任何配置！** 只要满足以下条件即可自动生效：

1. 在 `pom.xml` 中依赖 `common` 模块：
```xml
<dependency>
    <groupId>com.example.oauth</groupId>
    <artifactId>common</artifactId>
</dependency>
```

2. 确保依赖了 Security 相关包（通常已在父 POM 管理）：
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
```

3. 在 `application.yml` 中配置 OAuth2 资源服务器：
```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:9000  # OAuth2 服务器地址
          jwk-set-uri: http://localhost:9000/oauth2/jwks  # JWK 密钥集地址
```

### 方式 2：自定义公开路径
如果某个服务需要额外的公开路径，创建自己的 SecurityConfig：

```java
package com.example.oauth.product.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorize -> authorize
            // 默认公开路径 + 自定义公开路径
            .requestMatchers("/actuator/**", "/health", "/api/products/public/**").permitAll()
            // 其他请求需要认证
            .anyRequest().authenticated()
        )
        // 启用 JWT 资源服务器
        .oauth2ResourceServer(oauth2 -> oauth2.jwt());
        
        return http.build();
    }
}
```

**注意**：Spring Security 会自动选择优先级更高的配置（同一名名下后定义的 Bean 优先）。

### 方式 3：完全禁用自动配置
如果某个服务不需要自动安全配置，可以在 `application.yml` 中禁用：

```yaml
common:
  security:
    enabled: false
```

## 🔧 配置说明

### 默认配置内容
1. **公开路径**：`/actuator/**`, `/health`
2. **认证要求**：其他所有请求需要认证
3. **会话管理**：无状态（STATELESS）
4. **CSRF**：已禁用（JWT 不需要）
5. **JWT 转换器**：
   - 从 `scope` claim 提取权限
   - 添加 `ROLE_` 前缀

### JWT Token 权限映射示例
```json
{
  "sub": "user123",
  "scope": "read write",
  "roles": ["USER", "ADMIN"]
}
```

转换为 Spring Security 权限：
- `ROLE_read`
- `ROLE_write`

## 📝 新增服务步骤

当添加新的微服务时：

1. **创建服务模块**（已有模板可复制）

2. **在 pom.xml 中添加依赖**：
```xml
<dependency>
    <groupId>com.example.oauth</groupId>
    <artifactId>common</artifactId>
</dependency>
```

3. **在 application.yml 中配置 OAuth2**：
```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${OAUTH_SERVER_URI:http://localhost:9000}
          jwk-set-uri: ${OAUTH_SERVER_URI:http://localhost:9000/oauth2/jwks}
```

4. **启动类添加注解**（可选，用于扫描 common 包）：
```java
@SpringBootApplication
@ComponentScan(basePackages = {"com.example.oauth.yourservice", "com.example.oauth.common"})
public class YourServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(YourServiceApplication.class, args);
    }
}
```

**完成！不需要再创建 SecurityConfig！** 🎉

## ⚠️ 注意事项

1. **Gateway 服务不使用此配置**
   - Gateway 使用 WebFlux（响应式），需要单独的安全配置
   - Gateway 负责登录认证，资源服务负责 token 验证

2. **OAuth2 Server 本身不使用此配置**
   - OAuth2 Server 是认证服务器，不是资源服务器

3. **权限控制**
   - 如需方法级权限控制，使用 `@PreAuthorize("hasRole('ADMIN')")`
   - 自动配置的 `@EnableMethodSecurity` 已启用此功能

## 🎯 最佳实践

1. **优先使用自动配置**：90% 的场景不需要自定义
2. **统一公开路径**：所有服务保持 `/actuator/**`, `/health` 公开
3. **使用环境变量**：生产环境通过环境变量配置 OAuth2 服务器地址
4. **方法级权限**：在 Service 层使用 `@PreAuthorize` 进行细粒度控制

## 📖 示例代码

完整的服务配置示例：

**pom.xml**:
```xml
<dependencies>
    <!-- Common Module -->
    <dependency>
        <groupId>com.example.oauth</groupId>
        <artifactId>common</artifactId>
    </dependency>
    
    <!-- Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- MyBatis, MySQL, Redis 等其他依赖 -->
</dependencies>
```

**application.yml**:
```yaml
server:
  port: 8081

spring:
  application:
    name: product-service
  
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:9000
          jwk-set-uri: http://localhost:9000/oauth2/jwks

# 不需要配置 common.security.* 
# 自动配置会生效
```

**Controller 示例**:
```java
@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    @GetMapping
    @PreAuthorize("hasRole('read')")
    public List<Product> listProducts() {
        // ...
    }
    
    @PostMapping
    @PreAuthorize("hasRole('write')")
    public Product createProduct(@RequestBody Product product) {
        // ...
    }
}
```
