package com.biotrack.backend.controllers;

import com.biotrack.backend.dto.ClinicalHistoryRecordDTO;
import com.biotrack.backend.dto.PatientCreationDTO;
import com.biotrack.backend.dto.PrimaryHospitalDTO;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
            content = @Content(schema = @Schema(implementation = PatientCreationDTO.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data or validation errors"
        )
    })
    public ResponseEntity<PatientCreationDTO> create(@Valid @RequestBody PatientCreationDTO dto){
        try {
            Patient saved = patientService.create(PatientMapper.toEntityCreation(dto));
            return ResponseEntity.status(HttpStatus.CREATED).body(PatientMapper.toDTOCreation(saved));
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
    public ResponseEntity<List<PatientCreationDTO>> getAll(){
        List<PatientCreationDTO> patients = patientService.findAll().stream()
                .map(PatientMapper::toDTOCreation).toList();
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
            content = @Content(schema = @Schema(implementation = PatientCreationDTO.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Patient not found with the provided ID"
        )
    })
    public ResponseEntity<PatientCreationDTO> getById(
        @Parameter(description = "Unique identifier of the patient") 
        @PathVariable UUID id
    ){
        Patient patient = patientService.findById(id);
        return ResponseEntity.ok(PatientMapper.toDTOCreation(patient));
    }

    @GetMapping("/getPatientsByName")
    @Operation(summary = "Search patients by first name and/or last name", description = "Retrieve patients filtered by first name and/or last name (case insensitive)")
    @ApiResponse(responseCode = "200", description = "Patient list retrieved successfully")
    public ResponseEntity<List<PatientCreationDTO>> searchPatients(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName) {
        List<Patient> patients = patientService.searchPatients(firstName, lastName);
        List<PatientCreationDTO> dtos = patients.stream().map(PatientMapper::toDTOCreation).toList();
        return ResponseEntity.ok(dtos);
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
            content = @Content(schema = @Schema(implementation = PatientCreationDTO.class))
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
    public ResponseEntity<PatientCreationDTO> update(
        @Parameter(description = "Unique identifier of the patient") 
        @PathVariable UUID id, 
        @Valid @RequestBody PatientCreationDTO updatedPatient
    ){
        Patient patient = patientService.update(id, PatientMapper.toEntityCreation(updatedPatient));
        return ResponseEntity.ok(PatientMapper.toDTOCreation(patient));
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

    @PreAuthorize("hasRole('LAB_TECHNICIAN') or hasRole('MEDIC') or hasRole('PATIENT') or hasRole('ADMIN') or hasRole('RECEPTIONIST')")
    @GetMapping("/latest/{patientId}")
    public ResponseEntity<ClinicalHistoryRecordDTO> getLatestSummary(@PathVariable UUID patientId) {
        ClinicalHistoryRecord record = patientService.getLatestRecord(patientId);
        return ResponseEntity.ok(ClinicalHistoryRecordMapper.toDTO(record));
    }
    
    @PreAuthorize("hasRole('LAB_TECHNICIAN') or hasRole('MEDIC') or hasRole('PATIENT') or hasRole('ADMIN') or hasRole('RECEPTIONIST')")
    @GetMapping("/latest/{patientId}/summary-text")
    @Operation(
        summary = "Get latest clinical summary text for a patient",
        description = "Downloads and returns the latest clinical summary as plain text from S3"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Summary text retrieved successfully"
    )
    public ResponseEntity<String> getLatestSummaryText(@PathVariable UUID patientId) {
        String content = patientService.getLatestSummaryText(patientId);
        return ResponseEntity.ok()
                .header("Content-Type", "text/plain; charset=UTF-8")
                .body(content);
    }

    @PreAuthorize("hasRole('LAB_TECHNICIAN') or hasRole('MEDIC') or hasRole('PATIENT') or hasRole('ADMIN') or hasRole('RECEPTIONIST')")
    @GetMapping("/latest/{patientId}/summary-text/patient-friendly")
    @Operation(
        summary = "Get latest clinical summary text for a patient",
        description = "Downloads and returns the latest clinical summary as plain text from S3"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Summary text retrieved successfully"
    )
    public ResponseEntity<String> getLatestSummaryTextPatientFriendly(@PathVariable UUID patientId) {
        String content = patientService.getLatestSummaryTextPatientFriendly(patientId);
        return ResponseEntity.ok()
                .header("Content-Type", "text/plain; charset=UTF-8")
                .body(content);
    }
    
    @GetMapping("/{patientId}/primary-hospital")
    @Operation(
        summary = "Get patient's primary hospital",
        description = "Retrieve basic information of the patient's main hospital (first assigned hospital)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Primary hospital found"),
        @ApiResponse(responseCode = "404", description = "Patient not found or no hospital assigned"),
        @ApiResponse(responseCode = "400", description = "Invalid patient ID")
    })
    public ResponseEntity<PrimaryHospitalDTO> getPrimaryHospital(
            @Parameter(description = "Patient ID", required = true)
            @PathVariable UUID patientId) {
        try {
            Optional<PrimaryHospitalDTO> primaryHospital = patientService.getPrimaryHospital(patientId);
            
            if (primaryHospital.isPresent()) {
                return ResponseEntity.ok(primaryHospital.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving primary hospital for patient: " + e.getMessage(), e);
        }
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
