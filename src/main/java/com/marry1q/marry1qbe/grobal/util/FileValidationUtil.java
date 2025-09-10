package com.marry1q.marry1qbe.grobal.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@Slf4j
public class FileValidationUtil {
    
    // 허용된 이미지 파일 형식
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif"
    );
    
    // 허용된 파일 확장자
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "gif"
    );
    
    // 최대 파일 크기 (10MB)
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    
    /**
     * 이미지 파일 유효성 검증
     */
    public static void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }
        
        // 파일 크기 검증
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("파일 크기가 10MB를 초과합니다. 현재 크기: " + 
                    String.format("%.2f MB", file.getSize() / (1024.0 * 1024.0)));
        }
        
        // 파일 형식 검증 (Content-Type)
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("지원하지 않는 파일 형식입니다. " +
                    "지원 형식: JPG, JPEG, PNG, GIF, 현재 형식: " + contentType);
        }
        
        // 파일 확장자 검증
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new IllegalArgumentException("파일명이 없습니다.");
        }
        
        String extension = getFileExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("지원하지 않는 파일 확장자입니다. " +
                    "지원 확장자: " + String.join(", ", ALLOWED_EXTENSIONS) + 
                    ", 현재 확장자: " + extension);
        }
        
        log.info("파일 검증 완료 - 파일명: {}, 크기: {} bytes, 형식: {}", 
                originalFilename, file.getSize(), contentType);
    }
    
    /**
     * 파일 확장자 추출
     */
    private static String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
    
    /**
     * 파일 크기를 사람이 읽기 쉬운 형태로 변환
     */
    public static String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        }
    }
}
