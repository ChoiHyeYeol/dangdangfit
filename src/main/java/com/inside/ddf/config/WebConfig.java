package com.inside.ddf.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 1) 루트는 index.html
        registry.addViewController("/")
                .setViewName("forward:/index.html");

        // 2) 1-뎁스: 첫 세그먼트가 api가 아닐 때만 포워드
        //   예) /my, /recipe, /signin 등 → index.html
        //       /api  → 매칭 안 함(컨트롤러/필터로 감)
        registry.addViewController("/{first:^(?!api$)[a-zA-Z0-9-_]+}")
                .setViewName("forward:/index.html");

        // 3) 다중 뎁스: "첫 세그먼트 != api" 이고, 마지막 세그먼트에 점(.)이 없는 것만
        //   (정적 파일 main.123.js 처럼 확장자 있는 건 제외)
        //   예) /my/edit/info → index.html
        //       /static/js/main.js → 제외(정적 리소스)
        registry.addViewController("/{first:^(?!api$)[a-zA-Z0-9-_]+}/**/{last:[a-zA-Z0-9-_]+}")
                .setViewName("forward:/index.html");
    }
}
