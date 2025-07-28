package com.biotrack.backend.services;

import com.biotrack.backend.models.InventoryMedicine;
import java.util.List;
import java.util.UUID;

public interface InventoryMedicineService {
    InventoryMedicine create(InventoryMedicine medicine);
    List<InventoryMedicine> findAll();
    InventoryMedicine findById(UUID id);
    void deleteById(UUID id);
    InventoryMedicine update(UUID id, InventoryMedicine updatedMedicine);
}