package com.biotrack.backend.controllers;

import com.biotrack.backend.dto.LabAppointmentCreationDTO;
import com.biotrack.backend.dto.LabAppointmentDTO;
import com.biotrack.backend.services.LabAppointmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/lab-appointments")
public class LabAppointmentController {

    private final LabAppointmentService service;

    public LabAppointmentController(LabAppointmentService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<LabAppointmentDTO> create(@RequestBody LabAppointmentCreationDTO dto) {
        return ResponseEntity.status(201).body(service.create(dto));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<LabAppointmentDTO>> findByPatient(@PathVariable UUID patientId) {
        
        return ResponseEntity.ok(service.findByPatientId(patientId));
    }

    @PatchMapping("/{appointmentId}/status")
    public ResponseEntity<LabAppointmentDTO> updateStatus(
            @PathVariable UUID appointmentId,
            @RequestParam("status") String status
    ) {
        LabAppointmentDTO updated = service.updateStatus(appointmentId, status);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/hospital/{hospitalId}/solicited")
    public ResponseEntity<List<LabAppointmentDTO>> getSolicitedByHospital(@PathVariable UUID hospitalId) {
        return ResponseEntity.ok(service.findSolicitedByMedicalEntityId(hospitalId));
    }
}