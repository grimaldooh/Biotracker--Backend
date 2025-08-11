package com.biotrack.backend.controllers;

import com.biotrack.backend.dto.GeneticReportDTO;
import com.biotrack.backend.dto.PatientReportsDTO;
import com.biotrack.backend.dto.ReportDTO;
import com.biotrack.backend.models.Report;
import com.biotrack.backend.models.enums.ReportStatus;
import com.biotrack.backend.services.OpenAIService;
import com.biotrack.backend.services.ReportService;
import com.biotrack.backend.utils.ReportMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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

    @PostMapping("/generate-clinical")
    @Operation(
        summary = "Generate AI clinical report",
        description = "Generate a clinical report using AI for any sample type (blood, saliva, dna), even if there are no mutations"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Clinical report generated successfully",
            content = @Content(schema = @Schema(implementation = ReportDTO.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid sample ID"
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
    public ResponseEntity<ReportDTO> generateClinicalReport(
            @RequestParam UUID sampleId
            //@RequestParam(required = false) String patientInfo
    ) {
        try {
            Report report = reportService.generateClinicalReport(sampleId);
            return ResponseEntity.status(HttpStatus.CREATED).body(ReportMapper.toDTO(report));
        } catch (Exception e) {
            throw new RuntimeException("Error generating clinical report: " + e.getMessage(), e);
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
        List<ReportDTO> reports = reportService.findByStatus(ReportStatus.COMPLETED).stream()
                .map(ReportMapper::toDTO)
                .toList();
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ReportDTO>> getAllReportsFixed() {
        List<Report> allReports = reportService.findAll();
        List<ReportDTO> reports = allReports.stream()
                .sorted((r1, r2) -> r2.getGeneratedAt().compareTo(r1.getGeneratedAt()))
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

    @GetMapping("/patient/{patientId}")
    @Operation(
        summary = "Get all reports for a patient",
        description = "Retrieve all reports (normal and patient-friendly) associated with a patient's samples"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "List of patient reports retrieved successfully",
            content = @Content(
                array = @ArraySchema(schema = @Schema(implementation = PatientReportsDTO.class))
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Patient not found with the provided ID"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error"
        )
    })
    public ResponseEntity<List<PatientReportsDTO>> getPatientReports(
            @Parameter(description = "Unique identifier of the patient")
            @PathVariable UUID patientId) {
        
        List<PatientReportsDTO> reports = reportService.getPatientReports(patientId);
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/fetch-from-s3")
    @Operation(
        summary = "Fetch and parse report from S3",
        description = "Download and parse a report from S3 URL, returning structured data based on report type"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Report fetched and parsed successfully"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid S3 URL or report format"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Report not found in S3"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error downloading or parsing report"
        )
    })
    public ResponseEntity<Object> fetchReportFromS3(
            @Parameter(description = "S3 URL of the report to fetch")
            @RequestParam String s3Url,
            @Parameter(description = "Whether this is a patient-friendly report")
            @RequestParam boolean isPatientFriendly) {
        
        try {
            Object reportData = reportService.getReportFromS3(s3Url, isPatientFriendly);
            return ResponseEntity.ok(reportData);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid request parameters: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching report from S3: " + e.getMessage(), e);
        }
    }

    @GetMapping("/patient/{patientId}/genetic")
    public List<GeneticReportDTO> getGeneticReportsByPatient(@PathVariable UUID patientId) {
        return reportService.getGeneticReportsByPatient(patientId);
    }

    @GetMapping("/genetic-report-from-url")
    @Operation(
        summary = "Get genetic report from S3 URL",
        description = "Fetch and parse a genetic report (technical or patient-friendly) directly from S3 URL"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Genetic report fetched and parsed successfully"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid S3 URL or report format"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Report not found in S3"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error downloading or parsing report from S3"
        )
    })
    public ResponseEntity<Object> getGeneticReportFromUrl(
            @Parameter(description = "S3 URL of the genetic report to fetch")
            @RequestParam String s3Url,
            @Parameter(description = "Whether this is a patient-friendly report (true) or technical report (false)")
            @RequestParam(defaultValue = "false") boolean isPatientFriendly) {
        
        try {
            Object reportData = reportService.getGeneticReportFromUrl(s3Url, isPatientFriendly);
            return ResponseEntity.ok(reportData);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid request parameters: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching genetic report from S3: " + e.getMessage(), e);
        }
    }
}