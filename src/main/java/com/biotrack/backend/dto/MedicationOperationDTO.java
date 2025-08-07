package com.biotrack.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record MedicationOperationDTO(
        @NotNull
        @NotBlank
        String operation, // "ADD" o "REMOVE"
        
        // Para ADD: todos los campos son requeridos excepto medicationId
        String name,
        String brand,
        String activeSubstance,
        String indication,
        String dosage,
        String frequency,
        LocalDate startDate,
        LocalDate endDate,
        UUID prescribedById,
        
        // Para REMOVE: solo medicationId es requerido
        UUID medicationId
) {
    // Validaciones personalizadas
    public boolean isAddOperation() {
        return "ADD".equalsIgnoreCase(operation);
    }
    
    public boolean isRemoveOperation() {
        return "REMOVE".equalsIgnoreCase(operation);
    }
    
    public boolean isValidForAdd() {
        return isAddOperation() && name != null && !name.trim().isEmpty() 
               && dosage != null && prescribedById != null;
    }
    
    public boolean isValidForRemove() {
        return isRemoveOperation() && medicationId != null;
    }
}