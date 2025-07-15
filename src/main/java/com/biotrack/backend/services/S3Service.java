package com.biotrack.backend.services;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface S3Service {
    String uploadFile(MultipartFile file, String keyName);
    InputStream downloadFile(String keyName);
    void deleteFile(String keyName);
    String generatePresignedUrl(String keyName, int expirationMinutes);
}