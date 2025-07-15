package com.biotrack.backend.services.aws;

import com.biotrack.backend.services.S3Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;

@Service
public class S3ServiceImpl implements S3Service {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    public S3ServiceImpl(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Override
    public String uploadFile(MultipartFile file, String keyName) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(keyName)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            return String.format("https://%s.s3.amazonaws.com/%s", bucketName, keyName);
        } catch (IOException e) {
            throw new RuntimeException("Error uploading file to S3", e);
        }
    }

    @Override
    public InputStream downloadFile(String keyName) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(keyName)
                    .build();

            return s3Client.getObject(getObjectRequest);
        } catch (Exception e) {
            throw new RuntimeException("Error downloading file from S3: " + keyName, e);
        }
    }

    @Override
    public void deleteFile(String keyName) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(keyName)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
        } catch (Exception e) {
            throw new RuntimeException("Error deleting file from S3: " + keyName, e);
        }
    }

    @Override
    public String generatePresignedUrl(String keyName, int expirationMinutes) {
        try (S3Presigner presigner = S3Presigner.create()) {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(keyName)
                    .build();

            GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(expirationMinutes))
                    .getObjectRequest(getObjectRequest)
                    .build();

            return presigner.presignGetObject(getObjectPresignRequest).url().toString();
        }
    }
}