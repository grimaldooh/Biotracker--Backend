package com.biotrack.backend.controllers;

import com.biotrack.backend.dto.ClinicalHistoryRecordDTO;
import com.biotrack.backend.dto.PatientDTO;
import com.biotrack.backend.exceptions.ResourceNotFoundException;
import com.biotrack.backend.models.ClinicalHistoryRecord;
import com.biotrack.backend.models.Patient;
import com.biotrack.backend.services.PatientService;
import com.biotrack.backend.utils.ClinicalHistoryRecordMapper;
import com.biotrack.backend.utils.PatientMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/patients")
@Tag(name = "Patients", description = "Patient management operations for the BioTrack system")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService){
        this.patientService = patientService;
    }

    @PostMapping
    @Operation(
        summary = "Create new patient",
        description = "Register a new patient in the system with personal and medical information"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Patient created successfully",
            content = @Content(schema = @Schema(implementation = PatientDTO.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data or validation errors"
        )
    })
    public ResponseEntity<PatientDTO> create(@Valid @RequestBody PatientDTO dto){
        try {
            Patient saved = patientService.create(PatientMapper.toEntity(dto));
            return ResponseEntity.status(HttpStatus.CREATED).body(PatientMapper.toDTO(saved));
        } catch (Exception e) {
            // En caso de error, lanzar excepción para que sea manejada por @ExceptionHandler
            throw new RuntimeException("Error creating patient: " + e.getMessage(), e);
        }
    }

    @GetMapping
    @Operation(
        summary = "Get all patients",
        description = "Retrieve a list of all registered patients in the system"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Patient list retrieved successfully"
    )
    public ResponseEntity<List<PatientDTO>> getAll(){
        List<PatientDTO> patients = patientService.findAll().stream()
                .map(PatientMapper::toDTO).toList();
        return ResponseEntity.ok(patients);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get patient by ID",
        description = "Retrieve detailed information of a specific patient by their unique identifier"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Patient found successfully",
            content = @Content(schema = @Schema(implementation = PatientDTO.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Patient not found with the provided ID"
        )
    })
    public ResponseEntity<PatientDTO> getById(
        @Parameter(description = "Unique identifier of the patient") 
        @PathVariable UUID id
    ){
        Patient patient = patientService.findById(id);
        return ResponseEntity.ok(PatientMapper.toDTO(patient));
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Update patient",
        description = "Update the personal and medical information of an existing patient"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Patient updated successfully",
            content = @Content(schema = @Schema(implementation = PatientDTO.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Patient not found with the provided ID"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data or validation errors"
        )
    })
    public ResponseEntity<PatientDTO> update(
        @Parameter(description = "Unique identifier of the patient") 
        @PathVariable UUID id, 
        @Valid @RequestBody PatientDTO updatedPatient
    ){
        Patient patient = patientService.update(id, PatientMapper.toEntity(updatedPatient));
        return ResponseEntity.ok(PatientMapper.toDTO(patient));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete patient",
        description = "Remove a patient from the system permanently"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Patient deleted successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Patient not found with the provided ID"
        )
    })
    public ResponseEntity<Void> delete(
        @Parameter(description = "Unique identifier of the patient") 
        @PathVariable UUID id
    ){
        patientService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/generate-summary/{patientId}")
    public ResponseEntity<ClinicalHistoryRecordDTO> generateSummary(@PathVariable UUID patientId) {
        ClinicalHistoryRecord record = patientService.generatePatientClinicalSummary(patientId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ClinicalHistoryRecordMapper.toDTO(record));
    }

    @GetMapping("/latest/{patientId}")
    public ResponseEntity<ClinicalHistoryRecordDTO> getLatestSummary(@PathVariable UUID patientId) {
        ClinicalHistoryRecord record = patientService.getLatestRecord(patientId);
        return ResponseEntity.ok(ClinicalHistoryRecordMapper.toDTO(record));
    }

    // Exception handlers con tipos específicos
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((org.springframework.validation.FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }
}
