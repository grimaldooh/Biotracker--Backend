package com.biotrack.backend.controllers;

import com.biotrack.backend.dto.ResultFileDTO;
import com.biotrack.backend.exceptions.ResourceNotFoundException;
import com.biotrack.backend.models.ResultFile;
import com.biotrack.backend.services.ResultFileService;
import com.biotrack.backend.utils.ResultFileMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/result-files")
@Tag(name = "Result Files", description = "Genetic test result file management and AWS S3 storage operations")
public class ResultFileController {

    private final ResultFileService resultFileService;

    public ResultFileController(ResultFileService resultFileService) {
        this.resultFileService = resultFileService;
    }

    @PostMapping("/upload")
    @Operation(
        summary = "Upload genetic test result file",
        description = "Upload a genetic test result file to AWS S3 and link it to a specific sample"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "File uploaded and linked successfully",
            content = @Content(schema = @Schema(implementation = ResultFileDTO.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid file format or missing parameters"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Sample not found with the provided ID"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error uploading file to storage service"
        )
    })
    public ResponseEntity<ResultFileDTO> upload(
            @Parameter(description = "Genetic test result file (PDF, DOC, DOCX, TXT formats)")
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Unique identifier of the sample to link the file")
            @RequestParam("sampleId") UUID sampleId
    ) {
        try {
            ResultFile result = resultFileService.uploadAndLink(file, sampleId);
            return ResponseEntity.status(HttpStatus.CREATED).body(ResultFileMapper.toDTO(result));
        } catch (Exception e) {
            throw new RuntimeException("Error uploading file: " + e.getMessage(), e);
        }
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

   
}