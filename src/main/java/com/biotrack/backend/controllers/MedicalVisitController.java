package com.biotrack.backend.controllers;

import com.biotrack.backend.models.MedicalVisit;
import com.biotrack.backend.services.MedicalVisitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.parameters.*;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/medical-visits")
@Tag(name = "Medical Visits", description = "Patient medical history and clinical visits")
public class MedicalVisitController {

    private final MedicalVisitService service;

    public MedicalVisitController(MedicalVisitService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(
        summary = "Create medical visit",
        description = "Register a new medical visit for a patient"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Medical visit created successfully",
            content = @Content(schema = @Schema(implementation = MedicalVisit.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data or validation errors"
        )
    })
    public ResponseEntity<MedicalVisit> create(
        @RequestBody MedicalVisit visit
    ) {
        MedicalVisit saved = service.create(visit);
        return ResponseEntity.status(201).body(saved);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get medical visit by ID",
        description = "Retrieve the details of a specific medical visit"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Medical visit found",
            content = @Content(schema = @Schema(implementation = MedicalVisit.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Medical visit not found with the provided ID"
        )
    })
    public ResponseEntity<MedicalVisit> getById(
        @Parameter(description = "Unique identifier of the medical visit")
        @PathVariable UUID id
    ) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/patient/{patientId}")
    @Operation(
        summary = "Get all visits for a patient",
        description = "Retrieve all medical visits for a specific patient"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "List of medical visits for the patient",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = MedicalVisit.class)))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Patient not found with the provided ID"
        )
    })
    public ResponseEntity<List<MedicalVisit>> getByPatient(
        @Parameter(description = "Unique identifier of the patient")
        @PathVariable UUID patientId
    ) {
        return ResponseEntity.ok(service.findByPatientId(patientId));
    }

    @GetMapping
    @Operation(
        summary = "Get all medical visits",
        description = "Retrieve all medical visits in the system"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "List of all medical visits",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = MedicalVisit.class)))
        )
    })
    public ResponseEntity<List<MedicalVisit>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete medical visit",
        description = "Remove a medical visit from the system permanently"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Medical visit deleted successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Medical visit not found with the provided ID"
        )
    })
    public ResponseEntity<Void> delete(
        @Parameter(description = "Unique identifier of the medical visit")
        @PathVariable UUID id
    ) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}