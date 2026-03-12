package com.example.oauth.order.entity; // 定义包路径，用于组织和管理 Java 订单实体类

import lombok.Data; // 导入 Lombok 的 Data 注解，自动生成 getter、setter、toString 等方法

import java.io.Serializable; // 导入 Serializable 接口，使对象可以被序列化，支持网络传输和持久化
import java.math.BigDecimal; // 导入 BigDecimal 类，用于精确的金额计算
import java.util.Date; // 导入 Date 类，表示日期时间

/**
 * 订单实体类
 * 对应数据库中的订单表，存储订单信息
 */
@Data // Lombok 注解，自动生成所有字段的 getter、setter、toString、equals、hashCode 方法
public class Order implements Serializable { // 定义订单实体类，实现 Serializable 接口以支持序列化

    private static final long serialVersionUID = 1L; // 序列化版本号，用于在反序列化时验证版本兼容性

    /**
     * 订单 ID 字段
     * 主键，唯一标识一个订单
     */
    private Long id; // 订单 ID 字段

    /**
     * 订单编号字段
     * 订单的唯一业务标识符
     */
    private String orderNo; // 订单编号字段

    /**
     * 用户 ID 字段
     * 下单用户的 ID，关联用户表
     */
    private Long userId; // 用户 ID 字段

    /**
     * 金额字段
     * 订单的交易金额，使用 BigDecimal 确保精度
     */
    private BigDecimal amount; // 金额字段

    /**
     * 状态字段
     * 1:待支付 2:已支付 3:已取消
     */
    private Integer status; // 状态字段 (1:待支付 2:已支付 3:已取消)

    /**
     * 描述字段
     * 订单的详细描述说明
     */
    private String description; // 描述字段

    /**
     * 创建时间字段
     * 记录订单创建的时间
     */
    private Date createTime; // 创建时间字段

    /**
     * 更新时间字段
     * 记录订单信息最后更新的时间
     */
    private Date updateTime; // 更新时间字段
}
