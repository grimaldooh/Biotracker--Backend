package com.biotrack.backend.services;

import com.biotrack.backend.models.ResultFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface ResultFileService {
    ResultFile uploadAndLink(MultipartFile file, UUID sampleId);
    List<ResultFile> findAll();
}