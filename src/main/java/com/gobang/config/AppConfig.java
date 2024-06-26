package com.gobang.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AppConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/**/gobang/login.html")
                .excludePathPatterns("/**/gobang/reg.html")
                .excludePathPatterns("/**/gobang/TestAPI.html")
                .excludePathPatterns("/**/css/**.css")
                .excludePathPatterns("/**/img/**")
                .excludePathPatterns("/**/js/**.js")
                .excludePathPatterns("/**/gobang/login")
                .excludePathPatterns("/**/gobang/reg")
                .excludePathPatterns("/**/gobang/logout");
    }
}
