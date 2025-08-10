package com.biotrack.backend.services;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface S3Service {
    String uploadFile(MultipartFile file, String keyName);
    InputStream downloadFile(String keyName);
    void deleteFile(String keyName);
    String generatePresignedUrl(String keyName, int expirationMinutes);
    String uploadTextContent(String content, String keyName);
    String uploadStreamContent(InputStream inputStream, String keyName, String contentType, long contentLength);
    String downloadTextContent(String keyName);
    String downloadFileAsString(String s3Url);
}