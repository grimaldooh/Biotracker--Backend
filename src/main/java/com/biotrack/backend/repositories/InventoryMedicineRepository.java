package com.biotrack.backend.repositories;

import com.biotrack.backend.models.InventoryMedicine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InventoryMedicineRepository extends JpaRepository<InventoryMedicine, UUID> {
    List<InventoryMedicine> findByHospitalId(UUID hospitalId);
}