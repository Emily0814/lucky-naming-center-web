package com.test.naming;	

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@SpringBootApplication
@Controller	//또는 @RestController
public class NamingCenterApplication {	// 메인 애플리케이션 클래스

	public static void main(String[] args) {
		SpringApplication.run(NamingCenterApplication.class, args);
	}
	
	@GetMapping("/")
    public String home(Model model) {
		model.addAttribute("pageName", "index");	//"index" 값을 pageName으로 Thymeleaf에 전달
        return "index";  // templates/index.html을 가리킴
    }

}
