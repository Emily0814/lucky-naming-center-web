package com.test.naming.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
	
	@GetMapping("/signup")
	public String signup(Model model) {
		model.addAttribute("pageName", "signup"); // signup.html에 적용될 CSS 파일명
		model.addAttribute("userDTO", new UserDTO());
		return "user/signup";
	}
	
	@PostMapping("/signok")
	@ResponseBody
	public ResponseEntity<Map<String, String>> signupok(
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
					"userId", savedUser.getId().toString()
			));
			
		} catch (Exception e) {
			log.error("회원가입 처리 중 오류: ", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("message", "회원가입 중 오류가 발생했습니다: " + e.getMessage()));
		}
	}
	
	@GetMapping("/login")
	public String login(Model model) {
		model.addAttribute("pageName", "login"); // login.html에 적용될 CSS 파일명
		
		return "user/login";
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
