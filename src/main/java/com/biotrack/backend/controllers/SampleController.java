package com.biotrack.backend.controllers;

import com.biotrack.backend.dto.SampleCreationDTO;
import com.biotrack.backend.dto.SampleDTO;
import com.biotrack.backend.dto.Samples.SampleDetailDTO;
import com.biotrack.backend.exceptions.ResourceNotFoundException;
import com.biotrack.backend.factories.SampleFactory;
import com.biotrack.backend.models.Patient;
import com.biotrack.backend.models.Sample;
import com.biotrack.backend.models.User;
import com.biotrack.backend.services.PatientService;
import com.biotrack.backend.services.SampleService;
import com.biotrack.backend.services.UserService;
import com.biotrack.backend.utils.SampleMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
@RequestMapping("/api/samples")
@Tag(name = "Samples", description = "Genetic sample management and laboratory operations")
public class SampleController {

    private final SampleService sampleService;
    private final PatientService patientService;
    private final UserService userService;

    public SampleController(SampleService sampleService, PatientService patientService, UserService userService) {
        this.sampleService = sampleService;
        this.patientService = patientService;
        this.userService = userService;
    }

    @PostMapping
    @Operation(
        summary = "Create new genetic sample",
        description = "Register a new genetic sample linked to a patient and assigned technician"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Sample created successfully",
            content = @Content(schema = @Schema(implementation = SampleDTO.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data or validation errors"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Patient or technician not found"
        )
    })
    public ResponseEntity<SampleDTO> create(@Valid @RequestBody SampleCreationDTO sampleDTO){
        Patient patient = patientService.findById(sampleDTO.patientId());
        User technician = userService.getUserById(sampleDTO.registeredById());

        Sample sample = SampleFactory.create(sampleDTO, patient, technician);
        Sample created = sampleService.create(sample);
        

        // Busca la entidad concreta por ID y mapea a DTO específico
        Sample fullSample = sampleService.findById(created.getId());
        SampleDTO dto = SampleMapper.toDTO(fullSample);

        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping
    @Operation(
        summary = "Get all genetic samples",
        description = "Retrieve a list of all genetic samples registered in the laboratory"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Sample list retrieved successfully"
    )
    public ResponseEntity<List<SampleDTO>> getAll(){
        List<SampleDTO> samples = sampleService.findAll().stream()
                .map(SampleMapper::toDTO)
                .toList();
        return ResponseEntity.ok(samples);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get sample by ID",
        description = "Retrieve detailed information of a specific genetic sample including status and results"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Sample found successfully",
            content = @Content(schema = @Schema(implementation = SampleDTO.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Sample not found with the provided ID"
        )
    })
    public ResponseEntity<SampleDTO> getById(
        @Parameter(description = "Unique identifier of the genetic sample") 
        @PathVariable UUID id
    ){
        Sample sample = sampleService.findById(id);
        return ResponseEntity.ok(SampleMapper.toDTO(sample));
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Update sample status",
        description = "Update the status, results, and metadata of an existing genetic sample"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Sample updated successfully",
            content = @Content(schema = @Schema(implementation = SampleDTO.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Sample not found with the provided ID"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data or validation errors"
        )
    })
    public ResponseEntity<SampleDTO> update(
        @Parameter(description = "Unique identifier of the genetic sample") 
        @PathVariable UUID id, 
        @Valid @RequestBody SampleCreationDTO sampleDTO
    ){
        Sample existing = sampleService.findById(id);

        // Usa el factory para construir la entidad actualizada
        Sample updated = SampleFactory.create(
            sampleDTO, // Si tu factory espera SampleCreationDTO, convierte SampleDTO a SampleCreationDTO aquí
            existing.getPatient(),
            existing.getRegisteredBy()
        );
        Sample saved = sampleService.update(id, updated);
        return ResponseEntity.ok(SampleMapper.toDTO(saved));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete sample",
        description = "Remove a genetic sample from the system permanently (use with extreme caution)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Sample deleted successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Sample not found with the provided ID"
        )
    })
    public ResponseEntity<Void> delete(
        @Parameter(description = "Unique identifier of the genetic sample") 
        @PathVariable UUID id
    ){
        sampleService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/patient/{patientId}")
    @Operation(
        summary = "List samples by patient",
        description = "Retrieve all samples linked to a specific patient"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "List of samples for the patient",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = SampleDTO.class)))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Patient not found with the provided ID"
        )
    })
    public ResponseEntity<List<SampleDetailDTO>> getSamplesByPatient(@PathVariable UUID patientId) {
        List<Sample> samples = sampleService.findByPatientId(patientId);
        List<SampleDetailDTO> dtos = samples.stream().map(SampleMapper::toDetailDTO).toList();
        return ResponseEntity.ok(dtos);
    }

    // Exception handlers locales para este controlador
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
