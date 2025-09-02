package com.inside.ddf.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class MainController {

	@GetMapping("/")
	public String index() {
		return "forward:/index.html";
	}
//	@GetMapping("/test")
//	public String reindex() {
//		return "redirect:/index";
//	}
	// 정적 파일이 아닌 모든 경로는 index.html로 전달
    @GetMapping("/{path:^(?!api|static|css|js|images|fonts|favicon\\.ico).*$}")
    public String redirect(@PathVariable String path) {
        return "forward:/index.html";
    }
}
