package com.biotrack.backend.services;

import com.biotrack.backend.models.InventoryItem;
import java.util.List;
import java.util.UUID;

public interface InventoryItemService {
    InventoryItem create(InventoryItem item);
    List<InventoryItem> findAll();
    InventoryItem findById(UUID id);
    void deleteById(UUID id);
    InventoryItem update(UUID id, InventoryItem updatedItem);
    List<InventoryItem> findByHospitalId(UUID hospitalId);
}