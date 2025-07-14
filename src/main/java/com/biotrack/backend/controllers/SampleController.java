package com.biotrack.backend.controllers;

import com.biotrack.backend.models.Sample;
import com.biotrack.backend.services.SampleService;
import com.biotrack.backend.exceptions.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/samples")
public class SampleController {

    private final SampleService sampleService;

    public SampleController(SampleService sampleService){
        this.sampleService = sampleService;
    }

    @PostMapping
    public ResponseEntity<Sample> create(@RequestBody Sample sample){
        if (sample == null) {
            throw new ValidationException("Sample data cannot be null");
        }
        Sample created = sampleService.create(sample);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<Sample>> getAll(){
        try {
            List<Sample> samples = sampleService.findAll();
            return ResponseEntity.ok(samples);
        } catch (Exception e) {
            throw new ValidationException("Error retrieving samples: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Sample> getById(@PathVariable UUID id){
        if (id == null) {
            throw new ValidationException("Sample ID cannot be null");
        }
        Sample sample = sampleService.findById(id);
        return ResponseEntity.ok(sample);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Sample> update(@PathVariable UUID id, @RequestBody Sample sample){
        if (id == null) {
            throw new ValidationException("Sample ID cannot be null");
        }
        if (sample == null) {
            throw new ValidationException("Sample data cannot be null");
        }
        Sample updated = sampleService.update(id, sample);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        if (id == null) {
            throw new ValidationException("Sample ID cannot be null");
        }
        sampleService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
