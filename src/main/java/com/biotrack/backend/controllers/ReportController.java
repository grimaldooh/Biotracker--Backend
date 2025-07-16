package com.biotrack.backend.controllers;

import com.biotrack.backend.dto.ReportDTO;
import com.biotrack.backend.models.Report;
import com.biotrack.backend.models.enums.ReportStatus;
import com.biotrack.backend.services.OpenAIService;
import com.biotrack.backend.services.ReportService;
import com.biotrack.backend.utils.ReportMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "AI Reports", description = "AI-generated genetic analysis reports using OpenAI")
public class ReportController {

    private final ReportService reportService;
    private final OpenAIService openAIService;

    public ReportController(ReportService reportService, OpenAIService openAIService) {
        this.reportService = reportService;
        this.openAIService = openAIService;
    }

    @PostMapping("/generate")
    @Operation(
        summary = "Generate AI genetic report",
        description = "Generate a comprehensive genetic analysis report using AI for a specific sample"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Report generated successfully",
            content = @Content(schema = @Schema(implementation = ReportDTO.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid sample ID or no mutations available"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Sample not found"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error generating report or OpenAI service unavailable"
        )
    })
    public ResponseEntity<ReportDTO> generateReport(
            @Parameter(description = "Unique identifier of the sample to analyze")
            @RequestParam UUID sampleId,
            @Parameter(description = "Additional patient information for context")
            @RequestParam(required = false) String patientInfo
    ) {
        try {
            Report report = reportService.generateReportWithPatientInfo(sampleId, patientInfo);
            return ResponseEntity.status(HttpStatus.CREATED).body(ReportMapper.toDTO(report));
        } catch (Exception e) {
            throw new RuntimeException("Error generating genetic report: " + e.getMessage(), e);
        }
    }

    @GetMapping
    @Operation(
        summary = "Get all reports",
        description = "Retrieve a list of all generated genetic reports"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Report list retrieved successfully"
    )
    public ResponseEntity<List<ReportDTO>> getAllReports() {
        List<ReportDTO> reports = reportService.findByStatus(null).stream()
                .map(ReportMapper::toDTO)
                .toList();
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/sample/{sampleId}")
    @Operation(
        summary = "Get reports by sample",
        description = "Retrieve all reports generated for a specific sample"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Reports retrieved successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Sample not found"
        )
    })
    public ResponseEntity<List<ReportDTO>> getReportsBySample(
            @Parameter(description = "Unique identifier of the sample")
            @PathVariable UUID sampleId
    ) {
        List<ReportDTO> reports = reportService.findBySampleId(sampleId).stream()
                .map(ReportMapper::toDTO)
                .toList();
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/{reportId}")
    @Operation(
        summary = "Get report by ID",
        description = "Retrieve a specific genetic report by its unique identifier"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Report retrieved successfully",
            content = @Content(schema = @Schema(implementation = ReportDTO.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Report not found"
        )
    })
    public ResponseEntity<ReportDTO> getReportById(
            @Parameter(description = "Unique identifier of the report")
            @PathVariable UUID reportId
    ) {
        Report report = reportService.findById(reportId);
        return ResponseEntity.ok(ReportMapper.toDTO(report));
    }

    @GetMapping("/status/{status}")
    @Operation(
        summary = "Get reports by status",
        description = "Retrieve reports filtered by their processing status"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Reports retrieved successfully"
    )
    public ResponseEntity<List<ReportDTO>> getReportsByStatus(
            @Parameter(description = "Report processing status")
            @PathVariable ReportStatus status
    ) {
        List<ReportDTO> reports = reportService.findByStatus(status).stream()
                .map(ReportMapper::toDTO)
                .toList();
        return ResponseEntity.ok(reports);
    }

    @DeleteMapping("/{reportId}")
    @Operation(
        summary = "Delete report",
        description = "Remove a genetic report permanently from both database and S3 storage"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Report deleted successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Report not found"
        )
    })
    public ResponseEntity<Void> deleteReport(
            @Parameter(description = "Unique identifier of the report")
            @PathVariable UUID reportId
    ) {
        reportService.deleteReport(reportId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/test-openai")
    @Operation(
        summary = "Test OpenAI configuration",
        description = "Verify that OpenAI API is properly configured and accessible"
    )
    public ResponseEntity<String> testOpenAI() {
        try {
            if (!openAIService.isConfigured()) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body("OpenAI service is not configured. Please check API key.");
            }
            
            // Test con una lista vacía de mutaciones
            String testResult = openAIService.generateGeneticReport(
                    List.of(), 
                    "Test patient for API connectivity"
            );
            
            return ResponseEntity.ok("OpenAI API is working! Response length: " + testResult.length());
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("OpenAI test failed: " + e.getMessage());
        }
    }

    // Exception handlers
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }
}