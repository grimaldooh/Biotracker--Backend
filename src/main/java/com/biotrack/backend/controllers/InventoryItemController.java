package com.biotrack.backend.controllers;

import com.biotrack.backend.models.InventoryItem;
import com.biotrack.backend.services.InventoryItemService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventory-items")
public class InventoryItemController {

    private final InventoryItemService service;

    public InventoryItemController(InventoryItemService service) {
        this.service = service;
    }

    @PostMapping("/hospital/{hospitalId}")
    public ResponseEntity<InventoryItem> create(
            @PathVariable UUID hospitalId,
            @RequestBody InventoryItem item) {
        item.setHospitalId(hospitalId);
        return ResponseEntity.status(201).body(service.create(item));
    }

    @PreAuthorize("hasRole('LAB_TECHNICIAN') or hasRole('MEDIC') or hasRole('PATIENT') or hasRole('ADMIN') or hasRole('RECEPTIONIST')")
    @GetMapping
    public ResponseEntity<List<InventoryItem>> findAll(
            @RequestParam(value = "hospitalId", required = false) UUID hospitalId) {
        if (hospitalId != null) {
            return ResponseEntity.ok(service.findByHospitalId(hospitalId));
        } else {
            return ResponseEntity.ok(service.findAll());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryItem> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<InventoryItem> update(@PathVariable UUID id, @RequestBody InventoryItem item) {
        InventoryItem updated = service.update(id, item);
        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasRole('LAB_TECHNICIAN') or hasRole('MEDIC') or hasRole('PATIENT') or hasRole('ADMIN') or hasRole('RECEPTIONIST')")
    @GetMapping("/by-hospital/{hospitalId}")
    public ResponseEntity<List<InventoryItem>> findByHospitalId(@PathVariable UUID hospitalId) {
        return ResponseEntity.ok(service.findByHospitalId(hospitalId));
    }
}