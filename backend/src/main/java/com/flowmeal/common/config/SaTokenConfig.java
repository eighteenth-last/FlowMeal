package com.flowmeal.common.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 拦截器路由配置
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
            // 公开接口（无需登录）
            SaRouter.match("/auth/**").stop();
            SaRouter.match("/doc.html", "/v3/**", "/swagger-ui/**", "/webjars/**").stop();
            SaRouter.match("/payment/alipay/notify", "/payment/alipay/return").stop();
            SaRouter.match("/payment/callback/**").stop();  // 支付回调无需登录
            SaRouter.match("/shop/**").stop();               // 公开店铺商品浏览

            // 管理员专属接口
            SaRouter.match("/admin/**").check(r -> StpUtil.checkRole("ADMIN"));

            // 商家专属接口
            SaRouter.match("/merchant/**").check(r -> StpUtil.checkRole("MERCHANT"));

            // 外卖员专属接口
            SaRouter.match("/rider/**").check(r -> StpUtil.checkRole("RIDER"));

            // 用户端接口（登录即可）
            SaRouter.match("/**").check(r -> StpUtil.checkLogin());
        })).addPathPatterns("/**");
    }
}
