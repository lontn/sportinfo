package com.wbc.analytics.sportinfo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置類別
 * 設定 CORS (Cross-Origin Resource Sharing) 以允許 React 前端存取
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 設定允許跨域請求的路徑
        registry.addMapping("/**")
                // 允許 React 前端網域 (http://localhost:3000) 與 Vite/Vue 等前端 (http://localhost:5173)
                // 若有多個網域，可使用 String[] {"http://localhost:3000", "https://mydomain.com"}
                .allowedOrigins("http://localhost:3000", "http://localhost:5173")
                // 允許的 HTTP 方法
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                // 允許的請求標頭
                .allowedHeaders("*")
                // 是否允許傳送憑證 (Cookie, HTTP 認證等)
                .allowCredentials(true)
                // 預檢請求的快取時間 (秒)
                .maxAge(3600);
    }
}

