package com.biotrack.backend.services.aws;

import com.biotrack.backend.services.S3Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.stream.Collectors;

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

    // ✅ Nuevos métodos para contenido generado dinámicamente
    @Override
    public String uploadTextContent(String content, String keyName) {
        try {
            byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(keyName)
                    .contentType("text/plain")
                    .contentLength((long) contentBytes.length)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(contentBytes));

            return String.format("https://%s.s3.amazonaws.com/%s", bucketName, keyName);
        } catch (Exception e) {
            throw new RuntimeException("Error uploading text content to S3: " + keyName, e);
        }
    }

    @Override
    public String uploadStreamContent(InputStream inputStream, String keyName, String contentType, long contentLength) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(keyName)
                    .contentType(contentType)
                    .contentLength(contentLength)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, contentLength));

            return String.format("https://%s.s3.amazonaws.com/%s", bucketName, keyName);
        } catch (Exception e) {
            throw new RuntimeException("Error uploading stream content to S3: " + keyName, e);
        }
    }

    @Override
    public String downloadFileAsString(String s3Url) {
        try {
            // Extraer bucket y key de la URL
            String[] urlParts = extractBucketAndKeyFromUrl(s3Url);
            String bucketName = urlParts[0];
            String key = urlParts[1];
            
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            
            ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest);
            
            // Leer el contenido como String
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(s3Object, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Error downloading file from S3: " + e.getMessage(), e);
        }
    }

    @Override
    public String downloadFileAsStringNotFormated(String s3Url) {
        try {
            // Extraer bucket y key de la URL
            String[] urlParts = extractBucketAndKeyFromUrlNotFormated(s3Url);
            String bucketName = urlParts[0];
            String key = urlParts[1];
            
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            
            ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest);
            
            // Leer el contenido como String
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(s3Object, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Error downloading file from S3: " + e.getMessage(), e);
        }
    }

    private String[] extractBucketAndKeyFromUrlNotFormated(String s3Url) {
    try {
        // Formato esperado: https://biotrack-results-files.s3.amazonaws.com/reports/1754883001169_402b89a1-b97c-4002-abfc-d8540a7db178_genetic_report.txt
        
        if (s3Url.contains(".s3.amazonaws.com/")) {
            // Formato: https://bucket-name.s3.amazonaws.com/path/to/file
            String[] urlParts = s3Url.split("://", 2); // Separar protocolo
            if (urlParts.length != 2) {
                throw new IllegalArgumentException("Invalid URL protocol format");
            }
            
            String withoutProtocol = urlParts[1]; // biotrack-results-files.s3.amazonaws.com/reports/file.txt
            String[] domainAndPath = withoutProtocol.split("\\.s3\\.amazonaws\\.com/", 2);
            
            if (domainAndPath.length != 2) {
                throw new IllegalArgumentException("Invalid S3 domain format");
            }
            
            String bucketName = domainAndPath[0]; // biotrack-results-files
            String key = domainAndPath[1]; // reports/1754883001169_402b89a1-b97c-4002-abfc-d8540a7db178_genetic_report.txt
            
            return new String[]{bucketName, key};
            
        } else if (s3Url.contains("s3.amazonaws.com/")) {
            // Formato alternativo: https://s3.amazonaws.com/bucket-name/path/to/file
            String[] parts = s3Url.split("s3\\.amazonaws\\.com/", 2);
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid S3 path format");
            }
            
            String pathPart = parts[1]; // bucket-name/path/to/file
            String[] pathParts = pathPart.split("/", 2);
            
            if (pathParts.length < 1) {
                throw new IllegalArgumentException("Invalid bucket/key format");
            }
            
            String bucketName = pathParts[0];
            String key = pathParts.length > 1 ? pathParts[1] : "";
            
            return new String[]{bucketName, key};
            
        } else {
            throw new IllegalArgumentException("Unsupported S3 URL format. Expected format: https://bucket-name.s3.amazonaws.com/path/to/file");
        }
        
    } catch (Exception e) {
        throw new IllegalArgumentException("Could not parse S3 URL: " + s3Url + ". Error: " + e.getMessage(), e);
    }
}
    
    // ✅ NUEVO: Método helper para extraer bucket y key de URL
    private String[] extractBucketAndKeyFromUrl(String s3Url) {
        try {
            // Formato esperado: https://bucket-name.s3.amazonaws.com/path/to/file
            // o https://s3.amazonaws.com/bucket-name/path/to/file
            
            if (s3Url.contains(".s3.amazonaws.com/")) {
                // Formato: https://bucket-name.s3.amazonaws.com/path/to/file
                String[] parts = s3Url.split("\\.s3\\.amazonaws\\.com/", 2);
                String bucketName = parts[0].substring(parts[0].lastIndexOf("/") + 1);
                String key = parts[1];
                return new String[]{bucketName, key};
            } else if (s3Url.contains("s3.amazonaws.com/")) {
                // Formato: https://s3.amazonaws.com/bucket-name/path/to/file
                String[] parts = s3Url.split("s3\\.amazonaws\\.com/", 2);
                String[] pathParts = parts[1].split("/", 2);
                String bucketName = pathParts[0];
                String key = pathParts.length > 1 ? pathParts[1] : "";
                return new String[]{bucketName, key};
            } else {
                throw new IllegalArgumentException("Invalid S3 URL format: " + s3Url);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not parse S3 URL: " + s3Url, e);
        }
    }

    @Override
    public String downloadTextContent(String keyName) {
        try (InputStream inputStream = downloadFile(keyName)) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Error downloading text content from S3: " + keyName, e);
        }
    }
}