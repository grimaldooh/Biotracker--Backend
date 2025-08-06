package com.biotrack.backend.controllers;

import com.biotrack.backend.dto.ResultFileDTO;
import com.biotrack.backend.models.ResultFile;
import com.biotrack.backend.services.ResultFileService;
import com.biotrack.backend.utils.ResultFileMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/result-files")
@Tag(name = "Result Files", description = "Result file management for genetic analysis")
public class ResultFileController {

    private final ResultFileService resultFileService;

    @Autowired
    public ResultFileController(ResultFileService resultFileService) {
        this.resultFileService = resultFileService;
    }

    @PostMapping("/upload")
    @Operation(summary = "Upload result file", description = "Upload a result file and link it to a genetic sample")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "File uploaded successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid file or genetic sample ID"),
        @ApiResponse(responseCode = "404", description = "Genetic sample not found")
    })
    public ResponseEntity<ResultFileDTO> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("geneticSampleId") UUID geneticSampleId) { // ✅ CAMBIAR: parámetro
        
        ResultFile resultFile = resultFileService.uploadAndLink(file, geneticSampleId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResultFileMapper.toDTO(resultFile));
    }

    @GetMapping
    @Operation(
        summary = "Get all result files",
        description = "Retrieve a list of all genetic test result files stored in the system"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Result file list retrieved successfully"
    )
    public ResponseEntity<List<ResultFileDTO>> getAll() {
        List<ResultFileDTO> files = resultFileService.findAll().stream()
                .map(ResultFileMapper::toDTO)
                .toList();
        return ResponseEntity.ok(files);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get result file by ID", description = "Retrieve a specific result file by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Result file found"),
        @ApiResponse(responseCode = "404", description = "Result file not found")
    })
    public ResponseEntity<ResultFileDTO> findById(@PathVariable UUID id) {
        ResultFile resultFile = resultFileService.findById(id);
        return ResponseEntity.ok(ResultFileMapper.toDTO(resultFile));
    }

    @GetMapping("/genetic-sample/{geneticSampleId}")
    @Operation(summary = "Get result files by genetic sample", description = "Retrieve all result files for a specific genetic sample")
    public ResponseEntity<List<ResultFileDTO>> findByGeneticSampleId(@PathVariable UUID geneticSampleId) {
        List<ResultFile> resultFiles = resultFileService.findByGeneticSampleId(geneticSampleId);
        List<ResultFileDTO> dtos = resultFiles.stream()
                .map(ResultFileMapper::toDTO)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "Download result file", description = "Download a result file by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "File downloaded successfully"),
        @ApiResponse(responseCode = "404", description = "Result file not found")
    })
    public ResponseEntity<Resource> downloadFile(@PathVariable UUID id) {
        Resource resource = resultFileService.downloadFile(id);
        ResultFile resultFile = resultFileService.findById(id);
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(resultFile.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                       "attachment; filename=\"" + resultFile.getFileName() + "\"")
                .body(resource);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update processing status", description = "Update the processing status of a result file")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status updated successfully"),
        @ApiResponse(responseCode = "404", description = "Result file not found")
    })
    public ResponseEntity<ResultFileDTO> updateStatus(
            @PathVariable UUID id,
            @RequestParam String status) {
        
        ResultFile resultFile = resultFileService.updateStatus(id, status);
        return ResponseEntity.ok(ResultFileMapper.toDTO(resultFile));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete result file", description = "Delete a result file by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Result file deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Result file not found")
    })
    public ResponseEntity<Void> deleteFile(@PathVariable UUID id) {
        resultFileService.deleteFile(id);
        return ResponseEntity.noContent().build();
    }
}