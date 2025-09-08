package com.inside.ddf.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer{

   @Override
   public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000") // 허용할 Origin 설정
                .allowedOrigins("https://101.79.9.165:3000") // 허용할 Origin 설정
                .allowedOrigins("https://dangdangfit.smhrd.com/")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
		        .allowedHeaders("*")
		        .allowCredentials(true); // ✅ 세션/쿠키 주고받을 수 있도록 허용
    }
   
}
