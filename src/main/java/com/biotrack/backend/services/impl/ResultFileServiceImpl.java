package com.biotrack.backend.services.impl;

import com.biotrack.backend.models.ResultFile;
import com.biotrack.backend.models.Sample;
import com.biotrack.backend.repositories.ResultFileRepository;
import com.biotrack.backend.services.ResultFileService;
import com.biotrack.backend.services.SampleService;
import com.biotrack.backend.services.aws.S3Service;
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
        String url = s3Service.uploadFile(file);
        Sample sample = sampleService.findById(sampleId);

        ResultFile resultFile = ResultFile.builder()
                .fileName(file.getOriginalFilename())
                .s3Url(url)
                .uploadedAt(LocalDateTime.now())
                .sample(sample)
                .build();

        return resultFileRepository.save(resultFile);
    }

    @Override
    public List<ResultFile> findAll() {
        return resultFileRepository.findAll();
    }
}