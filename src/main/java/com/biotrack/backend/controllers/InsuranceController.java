package com.biotrack.backend.controllers;

import com.biotrack.backend.dto.insurance.InsuranceQuoteRequestDTO;
import com.biotrack.backend.dto.insurance.InsuranceQuoteResponseDTO;
import com.biotrack.backend.dto.insurance.InsuranceQuoteStatsDTO;
import com.biotrack.backend.services.AwsLambdaInsuranceService;
import com.biotrack.backend.services.InsuranceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/insurance")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Insurance", description = "Insurance Quote Management API")
public class InsuranceController {

    private final InsuranceService insuranceService;
    private final AwsLambdaInsuranceService awsLambdaInsuranceService; // AGREGAR esta línea

    @PostMapping("/quotes")
    @Operation(summary = "Calculate insurance quote", description = "Calculate a personalized insurance quote for a patient based on medical history and lifestyle factors")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Quote calculated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "404", description = "Patient not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    //@PreAuthorize("hasRole('MEDIC') or hasRole('ADMIN')")
    public ResponseEntity<InsuranceQuoteResponseDTO> calculateQuote(
            @Valid @RequestBody InsuranceQuoteRequestDTO request) {
        
        log.info("Received request to calculate insurance quote for patient: {}", request.getPatientId());
        
        try {
            InsuranceQuoteResponseDTO response = insuranceService.calculateInsuranceQuote(request);
            
            log.info("Insurance quote calculated successfully. Quote ID: {}", response.getQuoteId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            log.error("Error calculating insurance quote for patient: {}", request.getPatientId(), e);
            throw e;
        }
    }

    @GetMapping("/quotes/patient/{patientId}")
    @Operation(summary = "Get patient quotes", description = "Retrieve all insurance quotes for a specific patient")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Quotes retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Patient not found")
    })
    @PreAuthorize("hasRole('MEDIC') or hasRole('ADMIN') or hasRole('PATIENT')")
    public ResponseEntity<List<InsuranceQuoteResponseDTO>> getPatientQuotes(
            @Parameter(description = "Patient ID") @PathVariable UUID patientId) {
        
        log.info("Fetching insurance quotes for patient: {}", patientId);
        
        List<InsuranceQuoteResponseDTO> quotes = insuranceService.getPatientQuotes(patientId);
        
        log.info("Found {} quotes for patient: {}", quotes.size(), patientId);
        return ResponseEntity.ok(quotes);
    }

    @GetMapping("/quotes/patient/{patientId}/latest")
    @Operation(summary = "Get latest active quote", description = "Retrieve the most recent active insurance quote for a patient")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Latest quote retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "No active quote found for patient")
    })
    @PreAuthorize("hasRole('MEDIC') or hasRole('ADMIN') or hasRole('PATIENT')")
    public ResponseEntity<InsuranceQuoteResponseDTO> getLatestActiveQuote(
            @Parameter(description = "Patient ID") @PathVariable UUID patientId) {
        
        log.info("Fetching latest active quote for patient: {}", patientId);
        
        InsuranceQuoteResponseDTO quote = insuranceService.getLatestActiveQuote(patientId);
        
        if (quote == null) {
            log.info("No active quote found for patient: {}", patientId);
            return ResponseEntity.notFound().build();
        }
        
        log.info("Latest active quote found for patient: {}", patientId);
        return ResponseEntity.ok(quote);
    }

    @GetMapping("/quotes/{quoteId}")
    @Operation(summary = "Get quote by ID", description = "Retrieve a specific insurance quote by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Quote retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Quote not found")
    })
    @PreAuthorize("hasRole('MEDIC') or hasRole('ADMIN') or hasRole('PATIENT')")
    public ResponseEntity<InsuranceQuoteResponseDTO> getQuoteById(
            @Parameter(description = "Quote UUID") @PathVariable UUID quoteId) {
        
        log.info("Fetching quote by ID: {}", quoteId);
        
        try {
            InsuranceQuoteResponseDTO quote = insuranceService.getQuoteById(quoteId);
            return ResponseEntity.ok(quote);
        } catch (RuntimeException e) {
            log.error("Quote not found: {}", quoteId);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/quotes/{quoteId}/accept")
    @Operation(summary = "Accept quote", description = "Accept an insurance quote and change its status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Quote accepted successfully"),
        @ApiResponse(responseCode = "400", description = "Quote cannot be accepted (expired or invalid status)"),
        @ApiResponse(responseCode = "404", description = "Quote not found")
    })
    //@PreAuthorize("hasRole('MEDIC') or hasRole('ADMIN')")
    public ResponseEntity<InsuranceQuoteResponseDTO> acceptQuote(
            @Parameter(description = "Quote UUID") @PathVariable UUID quoteId) {
        
        log.info("Request to accept quote: {}", quoteId);
        
        try {
            InsuranceQuoteResponseDTO quote = insuranceService.acceptQuote(quoteId);
            
            log.info("Quote accepted successfully: {}", quoteId);
            return ResponseEntity.ok(quote);
            
        } catch (RuntimeException e) {
            log.error("Error accepting quote: {}", quoteId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/quotes/{quoteId}/reject")
    @Operation(summary = "Reject quote", description = "Reject an insurance quote and change its status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Quote rejected successfully"),
        @ApiResponse(responseCode = "404", description = "Quote not found")
    })
    @PreAuthorize("hasRole('MEDIC') or hasRole('ADMIN') or hasRole('PATIENT')")
    public ResponseEntity<InsuranceQuoteResponseDTO> rejectQuote(
            @Parameter(description = "Quote UUID") @PathVariable UUID quoteId) {
        
        log.info("Request to reject quote: {}", quoteId);
        
        try {
            InsuranceQuoteResponseDTO quote = insuranceService.rejectQuote(quoteId);
            
            log.info("Quote rejected successfully: {}", quoteId);
            return ResponseEntity.ok(quote);
            
        } catch (RuntimeException e) {
            log.error("Quote not found: {}", quoteId);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/stats")
    @Operation(summary = "Get insurance statistics", description = "Retrieve insurance quote statistics and analytics")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDIC') or hasRole('PATIENT')")
    public ResponseEntity<InsuranceQuoteStatsDTO> getInsuranceStatistics() {
        
        log.info("Fetching insurance statistics");
        
        InsuranceQuoteStatsDTO stats = insuranceService.getQuoteStatistics();
        
        log.info("Insurance statistics retrieved successfully");
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/quotes/expire")
    @Operation(summary = "Mark expired quotes", description = "Manually trigger the process to mark expired quotes (admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Expired quotes marked successfully")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('MEDIC') or hasRole('PATIENT')")
    public ResponseEntity<Void> markExpiredQuotes() {
        
        log.info("Manual trigger to mark expired quotes");
        
        insuranceService.markExpiredQuotes();
        
        log.info("Expired quotes marked successfully");
        return ResponseEntity.ok().build();
    }

    // AGREGAR nuevo endpoint:
    @GetMapping("/lambda/health")
    @Operation(summary = "Test Lambda connection", description = "Test connectivity and functionality of the AWS Lambda insurance calculator")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lambda is healthy"),
        @ApiResponse(responseCode = "503", description = "Lambda is not responding")
    })
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> testLambdaHealth() {
        
        log.info("Testing Lambda health check");
        
        try {
            boolean isHealthy = awsLambdaInsuranceService.testLambdaConnection();
            
            Map<String, Object> healthStatus = Map.of(
                "lambda_healthy", isHealthy,
                "function_name", "insurance_lambda",
                "region", "us-east-2",
                "timestamp", LocalDateTime.now()
            );
            
            if (isHealthy) {
                log.info("Lambda health check passed");
                return ResponseEntity.ok(healthStatus);
            } else {
                log.warn("Lambda health check failed");
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(healthStatus);
            }
            
        } catch (Exception e) {
            log.error("Error during Lambda health check", e);
            Map<String, Object> errorStatus = Map.of(
                "lambda_healthy", false,
                "error", e.getMessage(),
                "timestamp", LocalDateTime.now()
            );
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorStatus);
        }
    }
}