package com.biotrack.backend.controllers;

import com.biotrack.backend.models.MedicalVisit;
import com.biotrack.backend.services.MedicalVisitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
    @Operation(summary = "Create medical visit")
    public ResponseEntity<MedicalVisit> create(@RequestBody MedicalVisit visit) {
        MedicalVisit saved = service.create(visit);
        return ResponseEntity.status(201).body(saved);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get medical visit by ID")
    public ResponseEntity<MedicalVisit> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/patient/{patientId}")
    @Operation(summary = "Get all visits for a patient")
    public ResponseEntity<List<MedicalVisit>> getByPatient(@PathVariable UUID patientId) {
        return ResponseEntity.ok(service.findByPatientId(patientId));
    }

    @GetMapping
    @Operation(summary = "Get all medical visits")
    public ResponseEntity<List<MedicalVisit>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete medical visit")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}