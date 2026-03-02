package com.flowmeal.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Data;

/**
 * 支付宝配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "alipay")
public class AlipayProperties {

    private String appId;
    private String gatewayUrl;
    private String privateKey;
    private String alipayPublicKey;
    private String notifyUrl;
    private String returnUrl;
}
