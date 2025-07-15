package com.biotrack.backend.services.impl;

import com.biotrack.backend.models.ResultFile;
import com.biotrack.backend.models.Sample;
import com.biotrack.backend.repositories.ResultFileRepository;
import com.biotrack.backend.services.ResultFileService;
import com.biotrack.backend.services.SampleService;
import com.biotrack.backend.services.S3Service;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ResultFileServiceImpl implements ResultFileService {

    private final ResultFileRepository resultFileRepository;
    private final SampleService sampleService;
    private final S3Service s3Service;

    public ResultFileServiceImpl(ResultFileRepository repo, SampleService sampleService, S3Service s3Service) {
        this.resultFileRepository = repo;
        this.sampleService = sampleService;
        this.s3Service = s3Service;
    }

    @Override
    public ResultFile uploadAndLink(MultipartFile file, UUID sampleId) {
        // Generar nombre único para el archivo en S3
        String keyName = generateS3Key(file.getOriginalFilename(), sampleId);

        // Subir archivo con el keyName generado
        String s3Url = s3Service.uploadFile(file, keyName);

        Sample sample = sampleService.findById(sampleId);

        ResultFile resultFile = ResultFile.builder()
                .fileName(file.getOriginalFilename())
                .s3Key(keyName)
                .s3Url(s3Url)
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .uploadedAt(LocalDateTime.now())
                .sample(sample)
                .build();

        return resultFileRepository.save(resultFile);
    }

    @Override
    public List<ResultFile> findAll() {
        return resultFileRepository.findAll();
    }

    @Override
    public ResultFile findById(UUID id) {
        return resultFileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Result file not found with id: " + id));
    }

    @Override
    public List<ResultFile> findBySampleId(UUID sampleId) {
        return resultFileRepository.findBySampleId(sampleId);
    }

    @Override
    public Resource downloadFile(UUID fileId) {
        ResultFile resultFile = findById(fileId);
        return (Resource) s3Service.downloadFile(resultFile.getS3Key());
    }

    @Override
    public void deleteFile(UUID fileId) {
        ResultFile resultFile = findById(fileId);

        // Eliminar de S3
        s3Service.deleteFile(resultFile.getS3Key());

        // Eliminar de base de datos
        resultFileRepository.delete(resultFile);
    }

    @Override
    public ResultFile updateStatus(UUID fileId, String status) {
        ResultFile resultFile = findById(fileId);
        resultFile.setProcessingStatus(status);
        return resultFileRepository.save(resultFile);
    }

    /**
     * Genera un nombre único para el archivo en S3
     * Formato: results/{timestamp}_{sampleId}_{originalFileName}
     */
    private String generateS3Key(String originalFileName, UUID sampleId) {
        long timestamp = System.currentTimeMillis();
        String sanitizedFileName = sanitizeFileName(originalFileName);
        return String.format("results/%d_%s_%s", timestamp, sampleId.toString(), sanitizedFileName);
    }

    /**
     * Limpia el nombre del archivo para uso seguro en S3
     */
    private String sanitizeFileName(String fileName) {
        if (fileName == null) {
            return "unnamed_file";
        }

        // Reemplazar caracteres problemáticos
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_")
                .replaceAll("_{2,}", "_"); // Reemplazar múltiples guiones bajos consecutivos
    }
}