package com.acgforum.acgbackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射逻辑：/files/** -> 这里的 uploads 文件夹
        // file:./uploads/ 表示项目根目录下的 uploads 文件夹
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:./uploads/");
    }
}