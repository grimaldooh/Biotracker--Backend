package com.biotrack.backend.controllers;

import com.biotrack.backend.dto.ResultFileDTO;
import com.biotrack.backend.models.ResultFile;
import com.biotrack.backend.services.ResultFileService;
import com.biotrack.backend.utils.ResultFileMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/result-files")
public class ResultFileController {

    private final ResultFileService resultFileService;

    public ResultFileController(ResultFileService resultFileService) {
        this.resultFileService = resultFileService;
    }

    @PostMapping("/upload")
    public ResponseEntity<ResultFileDTO> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("sampleId") UUID sampleId
    ) {
        ResultFile result = resultFileService.uploadAndLink(file, sampleId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResultFileMapper.toDTO(result));
    }

    @GetMapping
    public ResponseEntity<List<ResultFileDTO>> getAll() {
        List<ResultFileDTO> files = resultFileService.findAll().stream()
                .map(ResultFileMapper::toDTO)
                .toList();
        return ResponseEntity.ok(files);
    }
}