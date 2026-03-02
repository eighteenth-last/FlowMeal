package com.flowmeal.common.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Cache 配置（使用 Redis 作为缓存）
 * TTL 等高级配置可通过 application.yml spring.cache.redis.* 进行扩展
 */
@EnableCaching
@Configuration
public class CacheConfig {
}
