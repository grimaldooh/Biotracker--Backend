package com.biotrack.backend.repositories;

import com.biotrack.backend.models.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, UUID> {
    List<InventoryItem> findByHospitalId(UUID hospitalId);
}