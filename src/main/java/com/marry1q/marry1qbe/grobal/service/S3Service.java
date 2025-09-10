package com.marry1q.marry1qbe.grobal.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {
    
    private final AmazonS3 s3Client;
    
    @Value("${aws.s3.bucket-name}")
    private String bucketName;
    
    /**
     * 파일 업로드
     */
    public String uploadFile(MultipartFile file, String s3Key) {
        try {
            log.info("S3 파일 업로드 시작 - bucket: {}, key: {}, fileSize: {}", bucketName, s3Key, file.getSize());
            
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());
            
            // ACL 없이 파일 업로드 (버킷 정책으로 접근 권한 관리)
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, s3Key, file.getInputStream(), metadata);
            
            s3Client.putObject(putObjectRequest);
            
            String fileUrl = s3Client.getUrl(bucketName, s3Key).toString();
            log.info("S3 파일 업로드 완료 - URL: {}", fileUrl);
            
            return fileUrl;
        } catch (Exception e) {
            log.error("S3 파일 업로드 실패 - key: {}, error: {}", s3Key, e.getMessage());
            throw new RuntimeException("파일 업로드에 실패했습니다.", e);
        }
    }
    
    /**
     * 파일 삭제
     */
    public void deleteFile(String s3Key) {
        try {
            log.info("S3 파일 삭제 시작 - bucket: {}, key: {}", bucketName, s3Key);
            s3Client.deleteObject(bucketName, s3Key);
            log.info("S3 파일 삭제 완료 - key: {}", s3Key);
        } catch (Exception e) {
            log.error("S3 파일 삭제 실패 - key: {}, error: {}", s3Key, e.getMessage());
            throw new RuntimeException("파일 삭제에 실패했습니다.", e);
        }
    }
}
