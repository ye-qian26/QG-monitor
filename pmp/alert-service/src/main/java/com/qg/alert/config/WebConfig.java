package com.qg.alert.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private com.qg.common.config.TokenInterceptor tokenInterceptor;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:/www/project/2025/backend/final/uploads/");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tokenInterceptor)
                .addPathPatterns("/**") // 拦截所有路径
                .excludePathPatterns("/users/password",
                        "/users/code",
                        "/users/register",
                        "/users/sendCodeByEmail",
                        "/backend/*",
                        "/frontend/*",
                        "/mobile/*",
                        "/users/findPassword"
                ); // 排除路径
    }
}



