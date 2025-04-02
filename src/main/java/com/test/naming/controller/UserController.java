package com.test.naming.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.naming.dto.UserDTO;
import com.test.naming.entity.User;
import com.test.naming.service.FileService;
import com.test.naming.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class UserController {
	
	private final UserService userService;
	private final FileService fileService;
	private final ObjectMapper objectMapper; // 주입 추가
	
	// 이 매핑은 유지하되, 실제로는 모달로 처리되기 때문에 직접 접근할 일은 없습니다.
	// 모달 외부에서 회원가입 페이지를 직접 보여주고 싶다면 사용할 수 있습니다.
	@GetMapping("/signup-page")
	public String signupPage(Model model) {
		model.addAttribute("pageName", "signup"); // signup.html에 적용될 CSS 파일명
		model.addAttribute("userDTO", new UserDTO());
		return "user/signup";
	}
	
	// API 엔드포인트로 변경 - 회원가입 처리
	@PostMapping(value = "/api/signup", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
	@ResponseBody
	public ResponseEntity<Map<String, String>> processSignup(
			@RequestPart(value = "userDTO") String userDTOString,
			@RequestPart(value = "profileFile", required = false) MultipartFile profileFile) {
		
		try {
			// 문자열로 받은 JSON을 객체로 변환
			UserDTO userDTO = objectMapper.readValue(userDTOString, UserDTO.class);
			
			log.info("회원가입 요청 데이터: {}", userDTO);
			
			// 이메일 중복 확인
			if (userService.isEmailExists(userDTO.getEmail())) {
				return ResponseEntity.badRequest()
						.body(Map.of(
							"message", "이미 등록된 이메일입니다.",
							"success", "false"
						));
			}
			
			// 기본값 설정 (생성일시, 상태, 역할)
			userDTO.setDefaultValues();
			
			// 프로필 파일이 있는 경우 처리 - FileService 사용
			if (profileFile != null && !profileFile.isEmpty()) {
				try {
					// 파일 저장 로직
					String savedFileName = fileService.saveProfileImage(profileFile);
					userDTO.setProfile(savedFileName);
					log.info("프로필 이미지 저장됨: {}", savedFileName);
				} catch (Exception e) {
					log.error("프로필 이미지 저장 중 오류: ", e);
					userDTO.setProfile("images/default-profile.jpg");
				}
			} else {
				userDTO.setProfile("images/default-profile.jpg");
			}
			
			// 사용자 서비스 호출 > 사용자 저장
			User savedUser = userService.registerUser(userDTO);
			log.info("회원가입 완료: {}", savedUser.getId());
			
			// 성공 응답 반환
			return ResponseEntity.ok(Map.of(
					"message", "회원가입이 완료되었습니다.",
					"userId", savedUser.getId().toString(),
					"success", "true"
			));
			
		} catch (Exception e) {
			log.error("회원가입 처리 중 오류: ", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of(
					    "message", "회원가입 중 오류가 발생했습니다: " + e.getMessage(),
					    "success", "false"
					));
		}
	}
	
	// 기존 회원가입 처리 엔드포인트 - 이전 버전과의 호환성을 위해 유지
	// 새 코드에서는 /api/signup을 사용하세요
	@PostMapping(value = "/signok", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
	@ResponseBody
	public ResponseEntity<Map<String, String>> signupok(
			@RequestPart(value = "userDTO") String userDTOString,
			@RequestPart(value = "profileFile", required = false) MultipartFile profileFile) {
		// /api/signup으로 요청 전달
		return processSignup(userDTOString, profileFile);
	}
	
	// 이 매핑은 유지하되, 실제로는 모달로 처리되기 때문에 직접 접근할 일은 없습니다.
	// 모달 외부에서 로그인 페이지를 직접 보여주고 싶다면 사용할 수 있습니다.
	@GetMapping("/login-page")
	public String loginPage(Model model) {
		model.addAttribute("pageName", "login"); // login.html에 적용될 CSS 파일명
		return "user/login";
	}
	
	@GetMapping("/logout")
	public String logout(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		// Spring Security의 SecurityContextLogoutHandler를 사용하여 로그아웃 처리
	    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	    if (auth != null) {
	        new SecurityContextLogoutHandler().logout(request, response, auth);
	    }
		
		return "redirect:/";
	}
	
	@GetMapping("/mypage")
	public String mypage(Model model, Authentication authentication) {
		model.addAttribute("pageName", "mypage"); // mypage.html에 적용될 CSS 파일명
		
		// 현재 인증된 사용자 정보 가져오기
        if (authentication != null && authentication.isAuthenticated()) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String email = userDetails.getUsername(); // 이메일이 username으로 사용됨
            
            // 이메일로 사용자 정보 조회
            UserDTO userDTO = userService.findUserByEmail(email);
            if (userDTO != null) {
                model.addAttribute("user", userDTO);
            }
        }
		
		return "user/mypage";
	}
}