package com.inside.ddf.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    registry.addViewController("/").setViewName("forward:/index.html");
    // /api 제외 1뎁스
    registry.addViewController("/{path:^(?!api$).*$}")
            .setViewName("forward:/index.html");
    // 하위 전부 (⚠️ **는 반드시 "맨 끝")
    registry.addViewController("/{path:^(?!api$).*$}/**")
            .setViewName("forward:/index.html");
  }
}
