package com.inside.ddf.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

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
}
