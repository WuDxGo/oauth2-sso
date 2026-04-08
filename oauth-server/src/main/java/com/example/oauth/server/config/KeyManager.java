package com.example.oauth.server.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * RSA密钥管理组件
 * 负责RSA密钥对的生成、加载、缓存和持久化管理
 * 将密钥保存到文件系统,避免应用重启后旧Token无法验证的问题
 * 使用单例模式+同步方法保证线程安全和性能
 * 通过@Component注解注册为Spring Bean,由Spring管理生命周期
 */
@Slf4j
@Component
public class KeyManager {

    /**
     * 密钥存储根路径
     * 从application.yml配置文件中读取,支持自定义路径
     * 如果配置文件中未设置,则使用默认值"./keystore"(项目根目录下)
     * 使用@Value注解实现配置文件属性绑定
     */
    @Value("${oauth2.key-store.path:./keystore}")
    private String keyStorePath;

    /**
     * 私钥文件名常量
     * 定义私钥文件的标准文件名,统一命名规范
     * 私钥必须严格保密,不得泄露给任何第三方
     */
    private static final String PRIVATE_KEY_FILE = "rsa_private.key";

    /**
     * 公钥文件名常量
     * 定义公钥文件的标准文件名
     * 公钥可以公开,用于验证JWT Token的签名
     */
    private static final String PUBLIC_KEY_FILE = "rsa_public.key";

    /**
     * 密钥算法常量
     * 使用RSA非对称加密算法,安全性高且性能良好
     * RSA算法广泛应用于JWT签名和验证场景
     */
    private static final String ALGORITHM = "RSA";

    /**
     * 密钥长度常量
     * 设置为2048位,符合当前信息安全标准
     * 1024位已不够安全,4096位性能开销较大,2048位是平衡点
     */
    private static final int KEY_SIZE = 2048;

    /**
     * 内存缓存的密钥对对象
     * 使用实例变量缓存,避免重复从文件加载或生成密钥
     * 首次加载后密钥对会一直存在于内存中,直到应用关闭
     */
    private KeyPair keyPair;

    /**
     * 获取RSA密钥对的公共方法(线程安全)
     * 如果内存中尚未加载密钥对,则自动从文件加载或生成新密钥
     * 使用synchronized关键字保证多线程环境下的安全性
     *
     * @return RSA密钥对对象,包含公钥和私钥
     */
    public synchronized KeyPair getKeyPair() {
        // 检查内存中是否已有密钥对,避免重复加载影响性能
        if (keyPair == null) {
            // 密钥对尚未加载,调用加载或创建方法
            keyPair = loadOrCreateKeyPair();
        }
        // 返回缓存的密钥对对象
        return keyPair;
    }

    /**
     * 加载或创建密钥对的核心逻辑方法
     * 优先从文件系统加载已存在的密钥,保证Token签名的连续性
     * 若密钥文件不存在或加载失败,则生成新密钥并保存到文件
     *
     * @return RSA密钥对对象,要么从文件加载的,要么新生成的
     */
    private KeyPair loadOrCreateKeyPair() {
        // 构建密钥存储的完整路径对象,将配置路径转换为Path对象
        Path storePath = Paths.get(keyStorePath);

        // 检查公钥和私钥文件是否都已存在于文件系统中
        // 两个文件必须同时存在才算完整的密钥对
        if (Files.exists(storePath.resolve(PRIVATE_KEY_FILE)) && Files.exists(storePath.resolve(PUBLIC_KEY_FILE))) {
            try {
                // 尝试从文件加载已存在的密钥对
                return loadKeyPair(storePath);
            } catch (Exception e) {
                // 若加载失败(可能是文件损坏或格式错误),记录警告日志
                // 后续会生成新密钥覆盖旧文件
                log.warn("加载已存在的密钥失败，将生成新的密钥: {}", e.getMessage());
            }
        }

        // 执行到这里有两种情况:
        // 1. 密钥文件不存在(首次启动)
        // 2. 密钥文件存在但加载失败(文件损坏)
        // 无论哪种情况,都生成新密钥并持久化到文件
        return generateAndSaveKeyPair(storePath);
    }

    /**
     * 从文件系统加载已存在的密钥对
     * 读取PKCS8格式的私钥文件和X.509格式的公钥文件
     * 并将字节码转换为Java安全对象的密钥对
     *
     * @param storePath 密钥存储目录的Path对象
     * @return RSA密钥对对象,包含公钥和私钥
     * @throws IOException 当文件读取失败或文件不存在时抛出
     * @throws NoSuchAlgorithmException 当RSA算法在当前JVM不可用时抛出
     * @throws InvalidKeySpecException 当密钥文件格式不正确或已损坏时抛出
     */
    private KeyPair loadKeyPair(Path storePath) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        // 读取私钥文件的全部字节到数组(私钥文件存储的是PKCS8格式)
        // PKCS8是私钥信息的标准语法,广泛用于存储和传输私钥
        byte[] privateKeyBytes = Files.readAllBytes(storePath.resolve(PRIVATE_KEY_FILE));

        // 读取公钥文件的全部字节到数组(公钥文件存储的是X.509格式)
        // X.509是公钥证书的标准格式,包含公钥和元数据
        byte[] publicKeyBytes = Files.readAllBytes(storePath.resolve(PUBLIC_KEY_FILE));

        // 获取RSA算法的密钥工厂对象,用于将字节转换为密钥对象
        // KeyFactory是Java Security API提供的工厂类,负责密钥和规格之间的转换
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);

        // 使用PKCS8编码规范从字节数组生成私钥对象
        // PKCS8EncodedKeySpec封装了PKCS8格式的私钥字节,KeyFactory可识别并转换
        PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));

        // 使用X.509编码规范从字节数组生成公钥对象
        // X509EncodedKeySpec封装了X.509格式的公钥字节,KeyFactory可识别并转换
        PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBytes));

        // 记录成功加载密钥的日志信息,方便运维追踪密钥状态
        log.info("成功加载已存在的密钥对");

        // 使用公钥和私钥构建KeyPair对象并返回
        // KeyPair是Java标准库的类,封装了公钥和私钥的配对关系
        return new KeyPair(publicKey, privateKey);
    }

    /**
     * 生成新的RSA密钥对并保存到文件系统
     * 会先创建存储目录(若不存在),然后使用SecureRandom生成安全随机数
     * 生成后将密钥编码并写入到指定的文件中,保证持久化
     *
     * @param storePath 密钥存储目录的Path对象
     * @return 新生成的RSA密钥对对象
     * @throws IllegalStateException 当密钥生成或文件保存失败时抛出
     */
    private KeyPair generateAndSaveKeyPair(Path storePath) {
        try {
            // 检查密钥存储目录是否存在,不存在则创建所有必需的父目录
            // createDirectories会递归创建所有不存在的目录,类似mkdir -p
            if (!Files.exists(storePath)) {
                Files.createDirectories(storePath);
            }

            // 获取RSA算法的密钥对生成器对象
            // KeyPairGenerator是Java Security API提供的密钥对生成工具
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);

            // 初始化密钥生成器,设置密钥长度为2048位
            // 长度越大安全性越高,但生成和签名性能会降低
            keyPairGenerator.initialize(KEY_SIZE);

            // 生成新的RSA密钥对,此操作会使用安全随机数生成密钥
            // 生成的过程可能需要几十到几百毫秒,取决于密钥长度
            KeyPair newKeyPair = keyPairGenerator.generateKeyPair();

            // 将私钥进行编码(转换为字节数组)后写入私钥文件
            // getEncoded()返回PKCS8格式的编码字节,可以直接写入文件
            Files.write(storePath.resolve(PRIVATE_KEY_FILE), newKeyPair.getPrivate().getEncoded());

            // 将公钥进行编码(转换为字节数组)后写入公钥文件
            // getEncoded()返回X.509格式的编码字节,可以直接写入文件
            Files.write(storePath.resolve(PUBLIC_KEY_FILE), newKeyPair.getPublic().getEncoded());

            // 记录生成并保存密钥的日志信息,包含绝对路径,方便定位文件
            log.info("生成并保存新的RSA密钥对，路径: {}", storePath.toAbsolutePath());

            // 返回新生成的密钥对,供调用方使用
            return newKeyPair;

        } catch (Exception e) {
            // 若生成或保存密钥失败(如权限不足、磁盘满等),包装为运行时异常
            // 抛出IllegalStateException表示系统状态异常,无法继续运行
            throw new IllegalStateException("密钥对生成或保存失败", e);
        }
    }
}
