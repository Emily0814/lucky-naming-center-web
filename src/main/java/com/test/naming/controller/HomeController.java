package com.test.naming.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

	@GetMapping("/")
    public String home(Model model) {
		model.addAttribute("pageName", "index");	//"index" 값을 pageName으로 Thymeleaf에 전달
        return "index";  // templates/index.html을 가리킴
    }
	
}
