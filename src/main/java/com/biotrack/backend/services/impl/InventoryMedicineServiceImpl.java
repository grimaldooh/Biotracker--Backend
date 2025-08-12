package com.biotrack.backend.services.impl;

import com.biotrack.backend.models.InventoryMedicine;
import com.biotrack.backend.repositories.InventoryMedicineRepository;
import com.biotrack.backend.services.InventoryMedicineService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class InventoryMedicineServiceImpl implements InventoryMedicineService {

    private final InventoryMedicineRepository repository;

    public InventoryMedicineServiceImpl(InventoryMedicineRepository repository) {
        this.repository = repository;
    }

    @Override
    public InventoryMedicine create(InventoryMedicine medicine) {
        return repository.save(medicine);
    }

    @Override
    public List<InventoryMedicine> findAll() {
        return repository.findAll();
    }

    @Override
    public InventoryMedicine findById(UUID id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Medicine not found"));
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    @Override
    public InventoryMedicine update(UUID id, InventoryMedicine updatedMedicine) {
        InventoryMedicine existing = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Inventory medicine not found"));
        // existing.setName(updatedMedicine.getName());
        // existing.setBrand(updatedMedicine.getBrand());
        // existing.setActiveSubstance(updatedMedicine.getActiveSubstance());
        // existing.setDosage(updatedMedicine.getDosage());
        existing.setQuantity(updatedMedicine.getQuantity());
        // existing.setExpirationDate(updatedMedicine.getExpirationDate());
        // existing.setLocation(updatedMedicine.getLocation());
        return repository.save(existing);
    }

    @Override
    public List<InventoryMedicine> findByHospitalId(UUID hospitalId) {
        return repository.findByHospitalId(hospitalId);
    }
}