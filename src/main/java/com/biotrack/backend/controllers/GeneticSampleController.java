package com.biotrack.backend.controllers;

import com.biotrack.backend.dto.GeneticSampleCreationDTO;
import com.biotrack.backend.dto.GeneticSampleDTO;
import com.biotrack.backend.models.GeneticSample;
import com.biotrack.backend.models.Patient;
import com.biotrack.backend.models.Report;
import com.biotrack.backend.models.User;
import com.biotrack.backend.services.GeneticSampleService;
import com.biotrack.backend.services.PatientService;
import com.biotrack.backend.services.ReportService;
import com.biotrack.backend.services.UserService;
import com.biotrack.backend.utils.GeneticSampleMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/genetic-samples")
@Tag(name = "Genetic Samples", description = "Genetic sample management for mutations and genomic analysis")
public class GeneticSampleController {

    private final GeneticSampleService geneticSampleService;
    private final PatientService patientService;
    private final UserService userService;
    private final ReportService reportService;

    @Autowired
    public GeneticSampleController(GeneticSampleService geneticSampleService, 
                                 PatientService patientService, 
                                 UserService userService,
                                 ReportService reportService) {
        this.geneticSampleService = geneticSampleService;
        this.patientService = patientService;
        this.userService = userService;
        this.reportService = reportService;
    }

    @PostMapping
    @Operation(summary = "Create a new genetic sample", description = "Creates a new genetic sample for genomic analysis")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Genetic sample created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "404", description = "Patient or User not found")
    })
    public ResponseEntity<GeneticSampleDTO> create(@Valid @RequestBody GeneticSampleCreationDTO creationDTO) {
        Patient patient = patientService.findById(creationDTO.patientId());
        User registeredBy = userService.getUserById(creationDTO.registeredById());
        
        GeneticSample geneticSample = GeneticSampleMapper.fromCreationDTO(creationDTO, patient, registeredBy);
        GeneticSample created = geneticSampleService.create(geneticSample);
        Report report = reportService.generateReportWithPatientInfo(created.getId(), "");
        
        return ResponseEntity.status(HttpStatus.CREATED).body(GeneticSampleMapper.toDTO(created));
    }

    @GetMapping
    @Operation(summary = "Get all genetic samples", description = "Retrieve all genetic samples in the system")
    public ResponseEntity<List<GeneticSampleDTO>> findAll() {
        List<GeneticSample> geneticSamples = geneticSampleService.findAll();
        List<GeneticSampleDTO> dtos = geneticSamples.stream()
                .map(GeneticSampleMapper::toDTO)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get genetic sample by ID", description = "Retrieve a specific genetic sample by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Genetic sample found"),
        @ApiResponse(responseCode = "404", description = "Genetic sample not found")
    })
    public ResponseEntity<GeneticSampleDTO> findById(@PathVariable UUID id) {
        GeneticSample geneticSample = geneticSampleService.findById(id);
        return ResponseEntity.ok(GeneticSampleMapper.toDTO(geneticSample));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update genetic sample", description = "Update an existing genetic sample")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Genetic sample updated successfully"),
        @ApiResponse(responseCode = "404", description = "Genetic sample not found"),
        @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<GeneticSampleDTO> update(@PathVariable UUID id, 
                                                  @Valid @RequestBody GeneticSampleCreationDTO updateDTO) {
        GeneticSample existing = geneticSampleService.findById(id);
        Patient patient = patientService.findById(updateDTO.patientId());
        User registeredBy = userService.getUserById(updateDTO.registeredById());
        
        GeneticSample updated = GeneticSampleMapper.fromCreationDTO(updateDTO, patient, registeredBy);
        GeneticSample saved = geneticSampleService.update(id, updated);
        
        return ResponseEntity.ok(GeneticSampleMapper.toDTO(saved));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete genetic sample", description = "Delete a genetic sample by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Genetic sample deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Genetic sample not found")
    })
    public ResponseEntity<Void> deleteById(@PathVariable UUID id) {
        geneticSampleService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/patient/{patientId}")
    @Operation(
        summary = "Get genetic samples by patient ID",
        description = "Retrieve all genetic samples associated with a specific patient ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Genetic samples found"),
        @ApiResponse(responseCode = "404", description = "No genetic samples found for this patient")
    })
    public ResponseEntity<List<GeneticSampleDTO>> getGeneticSamplesByPatientId(@PathVariable UUID patientId) {
        List<GeneticSample> geneticSamples = geneticSampleService.findByPatientId(patientId);
        List<GeneticSampleDTO> dtos = geneticSamples.stream()
                .map(GeneticSampleMapper::toDTO)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/hospital/{hospitalId}/latest")
    @Operation(summary = "Get latest genetic samples by hospital", description = "Retrieve the latest 10 genetic samples for a hospital")
    public ResponseEntity<List<GeneticSampleDTO>> getLatestByHospitalId(@PathVariable UUID hospitalId) {
        List<GeneticSample> geneticSamples = geneticSampleService.findLatest10ByMedicalEntityId(hospitalId);
        List<GeneticSampleDTO> dtos = geneticSamples.stream()
                .map(GeneticSampleMapper::toDTO)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/hospital/{hospitalId}")
    @Operation(summary = "Get all genetic samples by hospital", description = "Retrieve all genetic samples for a specific hospital")
    public ResponseEntity<List<GeneticSampleDTO>> getByHospitalId(@PathVariable UUID hospitalId) {
        List<GeneticSample> geneticSamples = geneticSampleService.findByMedicalEntityId(hospitalId);
        List<GeneticSampleDTO> dtos = geneticSamples.stream()
                .map(GeneticSampleMapper::toDTO)
                .toList();
        return ResponseEntity.ok(dtos);
    }
}