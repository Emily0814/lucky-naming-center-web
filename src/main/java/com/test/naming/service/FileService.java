package com.test.naming.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FileService {	//FileService 인터페이스와 구현체 만듦
	
	@Value("${file.upload.dir:src/main/resources/static/uploads}")
	private String uploadDir;
	
	/**
	 * 서비스 초기화 시 업로드 디렉토리 생성
	 */
	@PostConstruct
	public void init() {
	    try {
	        // 루트 업로드 디렉토리
	        Path rootPath = Paths.get(uploadDir);
	        if (!Files.exists(rootPath)) {
	            Files.createDirectories(rootPath);
	            log.info("업로드 루트 디렉토리 생성: {}", rootPath);
	        }
	        
	        // 프로필 이미지 폴더 생성
	        Path profilesPath = Paths.get(uploadDir, "profiles");
	        if (!Files.exists(profilesPath)) {
	            Files.createDirectories(profilesPath);
	            log.info("프로필 이미지 디렉토리 생성: {}", profilesPath);
	        }
	    } catch (IOException e) {
	        log.error("업로드 디렉토리 초기화 실패", e);
	        // 예외를 던지지 않고 로깅만 함 (애플리케이션 시작에 영향을 주지 않도록)
	    }
	}
	
	/**
	 * 모든 유형의 파일을 저장하는 범용 메서드
	 * @param file 저장할 파일
	 * @param subDirectory 저장할 허위 디렉토리(예: "profiles", "documents" 등) 
	 * @return 저장된 파일의 경로
	 * @throws IOException
	 */
	public String saveFile(MultipartFile file, String subDirectory) throws IOException {
		if (file == null || file.isEmpty()) {
			return null;
		}
		
		//디렉토리 경로 설정
		String directory = uploadDir + "/" + subDirectory;
		Path directoryPath = Paths.get(directory);
		
		//디렉토리가 없으면 생성
		Files.createDirectories(directoryPath);
		
		//고유한 파일명 생성
		String originalFilename = file.getOriginalFilename();
		String extension = "";
		if (originalFilename != null && originalFilename.contains(".")) {
			extension = originalFilename.substring(originalFilename.lastIndexOf("."));
		}
		String uniqueFilename = UUID.randomUUID().toString() + extension;
		
		//파일 저장
		Path filePath = directoryPath.resolve(uniqueFilename);
		Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
		
		log.info("파일 저장됨: {}, 크기: {}", filePath, file.getSize());
		
		//저장된 파일의 상대 경로 반환
		return subDirectory + "/" + uniqueFilename;
	}
	
	/**
	 * 프로필 이미지 저장을 위한 간편 메서드
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public String saveProfileImage(MultipartFile file) throws IOException {
		String relativePath = saveFile(file, "profiles");
		// 웹에서 접근 가능한 경로로 변환
		return relativePath != null ? "/uploads/" + relativePath : null;
	}
	
	/**
	 * 파일 삭제 메서드
	 * @param filePath
	 * @return
	 */
	public boolean deleteFile(String filePath) {
		if (filePath == null || filePath.isEmpty()) {
			return false;
		}
		
		Path path = Paths.get(uploadDir, filePath);
		try {
			return Files.deleteIfExists(path);
		} catch (Exception e) {
			log.error("파일 삭제 중 오류 발생: {}", path, e);
			return false;
		}
	}

}