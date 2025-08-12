package com.biotrack.backend.controllers;

import com.biotrack.backend.models.InventoryMedicine;
import com.biotrack.backend.services.InventoryMedicineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventory-medicines")
public class InventoryMedicineController {

    private final InventoryMedicineService service;

    public InventoryMedicineController(InventoryMedicineService service) {
        this.service = service;
    }

    @PostMapping("/hospital/{hospitalId}")
    public ResponseEntity<InventoryMedicine> create(
            @PathVariable UUID hospitalId,
            @RequestBody InventoryMedicine medicine) {
        medicine.setHospitalId(hospitalId);
        return ResponseEntity.status(201).body(service.create(medicine));
    }

    @GetMapping
    public ResponseEntity<List<InventoryMedicine>> findAll(
            @RequestParam(value = "hospitalId", required = false) UUID hospitalId) {
        if (hospitalId != null) {
            return ResponseEntity.ok(service.findByHospitalId(hospitalId));
        } else {
            return ResponseEntity.ok(service.findAll());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryMedicine> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<InventoryMedicine> update(@PathVariable UUID id, @RequestBody InventoryMedicine medicine) {
        InventoryMedicine updated = service.update(id, medicine);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/by-hospital/{hospitalId}")
    public ResponseEntity<List<InventoryMedicine>> findByHospitalId(@PathVariable UUID hospitalId) {
        return ResponseEntity.ok(service.findByHospitalId(hospitalId));
    }
}