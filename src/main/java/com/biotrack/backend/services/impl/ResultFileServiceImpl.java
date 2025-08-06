package com.biotrack.backend.services.impl;

import com.biotrack.backend.models.ResultFile;
import com.biotrack.backend.models.GeneticSample; // ✅ CAMBIAR: de Sample a GeneticSample
import com.biotrack.backend.repositories.ResultFileRepository;
import com.biotrack.backend.services.ResultFileService;
import com.biotrack.backend.services.GeneticSampleService; // ✅ CAMBIAR: de SampleService a GeneticSampleService
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
    private final GeneticSampleService geneticSampleService; // ✅ CAMBIAR: de SampleService a GeneticSampleService
    private final S3Service s3Service;

    public ResultFileServiceImpl(ResultFileRepository repo, GeneticSampleService geneticSampleService, S3Service s3Service) {
        this.resultFileRepository = repo;
        this.geneticSampleService = geneticSampleService; // ✅ CAMBIAR
        this.s3Service = s3Service;
    }

    @Override
    public ResultFile uploadAndLink(MultipartFile file, UUID geneticSampleId) { // ✅ CAMBIAR: parámetro de sampleId a geneticSampleId
        // Generar nombre único para el archivo en S3
        String keyName = generateS3Key(file.getOriginalFilename(), geneticSampleId);

        // Subir archivo con el keyName generado
        String s3Url = s3Service.uploadFile(file, keyName);

        GeneticSample geneticSample = geneticSampleService.findById(geneticSampleId); // ✅ CAMBIAR

        ResultFile resultFile = ResultFile.builder()
                .fileName(file.getOriginalFilename())
                .s3Key(keyName)
                .s3Url(s3Url)
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .uploadedAt(LocalDateTime.now())
                .geneticSample(geneticSample) // ✅ CAMBIAR: de sample a geneticSample
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
    public List<ResultFile> findByGeneticSampleId(UUID geneticSampleId) { 
        return resultFileRepository.findByGeneticSampleId(geneticSampleId);
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
     * Formato: results/{timestamp}_{geneticSampleId}_{originalFileName}
     */
    private String generateS3Key(String originalFileName, UUID geneticSampleId) { // ✅ CAMBIAR: parámetro
        long timestamp = System.currentTimeMillis();
        String sanitizedFileName = sanitizeFileName(originalFileName);
        return String.format("results/%d_%s_%s", timestamp, geneticSampleId.toString(), sanitizedFileName);
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