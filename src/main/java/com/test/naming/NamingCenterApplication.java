package com.test.naming;	

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;

@SpringBootApplication
public class NamingCenterApplication {	// 메인 애플리케이션 클래스

	public static void main(String[] args) {
		SpringApplication.run(NamingCenterApplication.class, args);
	}
	
	@GetMapping("/")
    public String home() {
        return "index";  // templates/index.html을 가리킴
    }

}
