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

@Service
public class FileService {	//FileService 인터페이스와 구현체 만듦
	
	@Value("${file.upload.dir}")
	private String uploadDir;
	
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
		return saveFile(file, "profiles");
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
			return false;
		}
	}

}
