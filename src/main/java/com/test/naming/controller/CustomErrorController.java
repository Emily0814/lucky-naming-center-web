package com.test.naming.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @GetMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        // 에러 상태 코드 가져오기
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        
        // 페이지 이름 설정 (CSS 파일과 연결하기 위한 속성)
        model.addAttribute("pageName", "error");
        
        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            
            // 상태 코드별 다른 에러 페이지 반환
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                return "error/404";
            } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
                return "error/403";
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                return "error/500";
            }
        }
        
        // 기본 에러 페이지
        return "error/500";
    }

    @GetMapping("/error/403")
    public String handleForbidden(Model model) {
        model.addAttribute("pageName", "error");
        return "error/403";
    }
}