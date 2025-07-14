package com.biotrack.backend.controllers;

import com.biotrack.backend.dto.PatientDTO;
import com.biotrack.backend.models.Patient;
import com.biotrack.backend.services.PatientService;
import com.biotrack.backend.utils.PatientMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService){
        this.patientService = patientService;
    }

    @PostMapping
    public ResponseEntity<PatientDTO> create(@Valid @RequestBody PatientDTO dto){
        Patient saved = patientService.create(PatientMapper.toEntity(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(PatientMapper.toDTO(saved));
    }

    @GetMapping
    public ResponseEntity<List<PatientDTO>> getAll(){
        List<PatientDTO> patients = patientService.findAll().stream()
                .map(PatientMapper::toDTO).toList();
        return ResponseEntity.ok(patients);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatientDTO> getById(@PathVariable UUID id){
        Patient patient = patientService.findById(id);
        return ResponseEntity.ok(PatientMapper.toDTO(patient));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PatientDTO> update(@PathVariable UUID id, @RequestBody PatientDTO updatedPatient){
        Patient patient = patientService.update(id, PatientMapper.toEntity(updatedPatient));
        return ResponseEntity.ok(PatientMapper.toDTO(patient));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        patientService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
