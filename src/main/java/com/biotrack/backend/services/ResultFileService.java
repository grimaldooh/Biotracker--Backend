package com.biotrack.backend.services;

import com.biotrack.backend.models.ResultFile;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface ResultFileService {
    ResultFile uploadAndLink(MultipartFile file, UUID sampleId);
    List<ResultFile> findAll();
    ResultFile findById(UUID id);
    List<ResultFile> findBySampleId(UUID sampleId);
    Resource downloadFile(UUID fileId);
    void deleteFile(UUID fileId);
    ResultFile updateStatus(UUID fileId, String status);
}