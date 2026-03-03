package com.flowmeal.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信支付 V3 配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "wechat-pay")
public class WechatPayProperties {

    /** 微信商户号 */
    private String mchId;

    /** 公众号/小程序 AppId */
    private String appId;

    /** APIv3 密钥 */
    private String apiV3Key;

    /** 商户私钥（PKCS8 PEM 格式，含 BEGIN/END 头） */
    private String privateKey;

    /** 商户证书序列号 */
    private String certSerialNo;

    /** 支付结果回调地址 */
    private String notifyUrl;
}
