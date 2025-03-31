package com.test.naming.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

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
	
	// 이 매핑은 유지하되, 실제로는 모달로 처리되기 때문에 직접 접근할 일은 없습니다.
	// 모달 외부에서 회원가입 페이지를 직접 보여주고 싶다면 사용할 수 있습니다.
	@GetMapping("/signup-page")
	public String signupPage(Model model) {
		model.addAttribute("pageName", "signup"); // signup.html에 적용될 CSS 파일명
		model.addAttribute("userDTO", new UserDTO());
		return "user/signup";
	}
	
	// API 엔드포인트로 변경 - 회원가입 처리
	@PostMapping("/api/signup")
	@ResponseBody
	public ResponseEntity<Map<String, String>> processSignup(
			@RequestPart(name = "userDTO") UserDTO userDTO,
			@RequestPart(name = "profileFile",  required = false) MultipartFile profileFile) {
		
		try {
			log.info("회원가입 요청 데이터: {}", userDTO);
			
			userDTO.setDefaultValues();
			
			//프로필 파일이 있는 경우 처리 - FileService 사용
			if (profileFile != null && !profileFile.isEmpty()) {
				try {
					//파일 처리 로직(파일 저장 등)
					String savedFileName = fileService.saveProfileImage(profileFile);	//실제 저장 로직으로 대체
					userDTO.setProfileFile(profileFile);
				} catch (Exception e) {
					log.error("프로필 이미지 저장 중 오류: ", e);
					userDTO.setProfile("images/default-profile.jpg");
				}
			} else {
				userDTO.setProfile("images/default-profile.jpg");
			}
			
			//사용자 서비스 호출 > 사용자 저장
			User savedUser = userService.registerUser(userDTO);
			
			//성공 응답 반환
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
	@PostMapping("/signok")
	@ResponseBody
	public ResponseEntity<Map<String, String>> signupok(
			@RequestPart(name = "userDTO") UserDTO userDTO,
			@RequestPart(name = "profileFile",  required = false) MultipartFile profileFile) {
		// /api/signup으로 요청 전달
		return processSignup(userDTO, profileFile);
	}
	
	// 이 매핑은 유지하되, 실제로는 모달로 처리되기 때문에 직접 접근할 일은 없습니다.
	// 모달 외부에서 로그인 페이지를 직접 보여주고 싶다면 사용할 수 있습니다.
	@GetMapping("/login-page")
	public String loginPage(Model model) {
		
		model.addAttribute("pageName", "login"); // login.html에 적용될 CSS 파일명
		return "user/login";
	}
	
	// API 엔드포인트 추가 - 로그인 처리
	@PostMapping("/api/login")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> processLogin(@RequestBody Map<String, String> loginData) {
	    try {
	        String email = loginData.get("email");
	        String password = loginData.get("password");
	        
	        log.info("로그인 시도: {}", email);
	        
	        // 여기에 실제 로그인 로직을 구현하세요
	        // 예: boolean isAuthenticated = userService.authenticate(email, password);
	        
	        // 임시 성공 응답 (실제 로직으로 대체해야 함)
	        return ResponseEntity.ok(Map.of(
	            "message", "로그인 성공",
	            "success", true,
	            "user", Map.of("email", email)
	        ));
	        
	    } catch (Exception e) {
	        log.error("로그인 처리 중 오류: ", e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(Map.of(
	                    "message", "로그인 중 오류가 발생했습니다: " + e.getMessage(),
	                    "success", false
	                ));
	    }
	}
	
	@GetMapping("/logout")
	public String logout(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		//여기에 넣어야 함
		
		return "redirect:/";
	}
	
	@GetMapping("/mypage")
	public String mypage(Model model) {
		model.addAttribute("pageName", "mypage"); // mypage.html에 적용될 CSS 파일명
		//여기에 넣어야 함
		
		return "user/mypage";
	}
	
}