package com.biotrack.backend.controllers;

import com.biotrack.backend.dto.MedicalVisitCreationDTO;
import com.biotrack.backend.dto.MedicalVisitDTO;
import com.biotrack.backend.models.MedicalVisit;
import com.biotrack.backend.services.MedicalVisitService;
import com.biotrack.backend.utils.MedicalVisitMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.parameters.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestBody;

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

    @PostMapping("/{medical_entity_id}")
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
    public ResponseEntity<MedicalVisitCreationDTO> create(@RequestBody MedicalVisitCreationDTO visit, @PathVariable UUID medical_entity_id ) {
        MedicalVisit saved = service.create(MedicalVisitMapper.toEntity(visit), medical_entity_id);
        MedicalVisitCreationDTO dto = MedicalVisitMapper.toDTO(saved);

        return ResponseEntity.status(201).body(dto);
    }

    @PostMapping("/submitAdvance/{id}")
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
    public ResponseEntity<MedicalVisitCreationDTO> submitAdvance(@RequestBody MedicalVisitCreationDTO visit,@PathVariable UUID id) {
        MedicalVisit appointmentUpdate = service.submitAdvance(id , MedicalVisitMapper.toEntity(visit));
        MedicalVisitCreationDTO dto = MedicalVisitMapper.toDTO(appointmentUpdate);
        dto.setDoctorId(null);
        dto.setPatientId(null);
        dto.setVisitDate(appointmentUpdate.getVisitDate().toString());
        return ResponseEntity.status(201).body(dto);
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
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = MedicalVisitDTO.class)))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Patient not found with the provided ID"
        )
    })
    public ResponseEntity<List<MedicalVisitDTO>> getByPatient(
        @Parameter(description = "Unique identifier of the patient")
        @PathVariable UUID patientId
    ) {
        List<MedicalVisit> visits = service.findByPatientId(patientId);
        return ResponseEntity.ok(MedicalVisitMapper.toDTOList(visits));
    }

    @GetMapping("/patient/{patientId}/pending")
    @Operation(summary = "Get pending visits for a patient")
    public ResponseEntity<List<MedicalVisitDTO>> getPendingVisitsByPatient(@PathVariable UUID patientId) {
        List<MedicalVisit> visits = service.findPendingByPatientId(patientId);
        return ResponseEntity.ok(MedicalVisitMapper.toDTOList(visits));
    }

    @GetMapping("/doctor/{doctorId}")
    @Operation(summary = "Get all visits for a doctor")
    public ResponseEntity<List<MedicalVisitDTO>> getVisitsByDoctor(@PathVariable UUID doctorId) {
        List<MedicalVisit> visits = service.findByDoctorId(doctorId);
        return ResponseEntity.ok(MedicalVisitMapper.toDTOList(visits));
    }

    @GetMapping("/doctor/{doctorId}/pending")
    @Operation(summary = "Get pending visits for a doctor")
    public ResponseEntity<List<MedicalVisitDTO>> getPendingVisitsByDoctor(@PathVariable UUID doctorId) {
        List<MedicalVisit> visits = service.findPendingByDoctorId(doctorId);
        return ResponseEntity.ok(MedicalVisitMapper.toDTOList(visits));
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