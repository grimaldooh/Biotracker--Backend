package com.biotrack.backend.controllers;

import com.biotrack.backend.dto.SampleDTO;
import com.biotrack.backend.models.Patient;
import com.biotrack.backend.models.Sample;
import com.biotrack.backend.models.User;
import com.biotrack.backend.services.PatientService;
import com.biotrack.backend.services.SampleService;
import com.biotrack.backend.services.UserService;
import com.biotrack.backend.utils.SampleMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/samples")
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
    public ResponseEntity<SampleDTO> create(@Valid @RequestBody SampleDTO sampleDTO){
        Patient patient = patientService.findById(sampleDTO.patientId());
        User technician = userService.getUserById(sampleDTO.registeredById());

        Sample sample = SampleMapper.toEntity(sampleDTO, patient, technician);
        Sample created = sampleService.create(sample);

        return ResponseEntity.status(HttpStatus.CREATED).body(SampleMapper.toDTO(created));
    }

    @GetMapping
    public ResponseEntity<List<SampleDTO>> getAll(){
        List<SampleDTO> samples = sampleService.findAll().stream()
                .map(SampleMapper::toDTO)
                .toList();
        return ResponseEntity.ok(samples);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SampleDTO> getById(@PathVariable UUID id){
        Sample sample = sampleService.findById(id);
        return ResponseEntity.ok(SampleMapper.toDTO(sample));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SampleDTO> update(@PathVariable UUID id, @Valid @RequestBody SampleDTO sampleDTO){
        Sample existing = sampleService.findById(id);

        Sample updated = SampleMapper.toEntity(
                sampleDTO,
                existing.getPatient(),
                existing.getRegisteredBy()
        );
        Sample saved = sampleService.update(id, updated);
        return ResponseEntity.ok(SampleMapper.toDTO(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        sampleService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
