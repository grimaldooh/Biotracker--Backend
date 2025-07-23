package com.biotrack.backend.controllers;

import com.biotrack.backend.models.Medication;
import com.biotrack.backend.services.MedicationService;
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
@RequestMapping("/api/medications")
@Tag(name = "Medications", description = "Medication management for patients")
public class MedicationController {

    private final MedicationService medicationService;

    public MedicationController(MedicationService medicationService) {
        this.medicationService = medicationService;
    }

    @PostMapping
    @Operation(
        summary = "Create new medication",
        description = "Register a new medication for a patient"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Medication created successfully",
            content = @Content(schema = @Schema(implementation = Medication.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data or validation errors"
        )
    })
    public ResponseEntity<Medication> create(@RequestBody Medication medication) {
        Medication saved = medicationService.create(medication);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    @Operation(
        summary = "List all medications",
        description = "Retrieve all medications in the system"
    )
    public ResponseEntity<List<Medication>> findAll() {
        return ResponseEntity.ok(medicationService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get medication by ID",
        description = "Retrieve a medication by its unique identifier"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Medication found",
            content = @Content(schema = @Schema(implementation = Medication.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Medication not found with the provided ID"
        )
    })
    public ResponseEntity<Medication> findById(
        @Parameter(description = "Unique identifier of the medication")
        @PathVariable UUID id
    ) {
        return ResponseEntity.ok(medicationService.findById(id));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete medication",
        description = "Remove a medication from the system"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Medication deleted successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Medication not found with the provided ID"
        )
    })
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        medicationService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/patient/{patientId}")
    @Operation(
        summary = "Get medications for a patient",
        description = "Retrieve all medications prescribed to a specific patient"
    )
    public ResponseEntity<List<Medication>> findByPatientId(@PathVariable UUID patientId) {
        return ResponseEntity.ok(medicationService.findByPatientId(patientId));
    }

   @GetMapping("/prescribed-by/{userId}")
    @Operation(
        summary = "Get medications prescribed by a user",
        description = "Retrieve all medications prescribed by a specific user (doctor)"
    )
    public ResponseEntity<List<Medication>> findByPrescribedById(@PathVariable UUID userId) {
        return ResponseEntity.ok(medicationService.findByPrescribedById(userId));
    }
}