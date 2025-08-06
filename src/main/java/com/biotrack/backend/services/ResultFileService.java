package com.biotrack.backend.services;

import com.biotrack.backend.models.ResultFile;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface ResultFileService {
    
    // ✅ CAMBIAR: parámetro de sampleId a geneticSampleId
    ResultFile uploadAndLink(MultipartFile file, UUID geneticSampleId);
    
    List<ResultFile> findAll();
    
    ResultFile findById(UUID id);
    
    // ✅ CAMBIAR: nombre del método
    List<ResultFile> findByGeneticSampleId(UUID geneticSampleId);
    
    Resource downloadFile(UUID fileId);
    
    void deleteFile(UUID fileId);
    
    ResultFile updateStatus(UUID fileId, String status);
}