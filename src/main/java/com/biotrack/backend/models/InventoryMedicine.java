package com.biotrack.backend.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "inventory_medicines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryMedicine {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String brand;

    private String activeSubstance;

    private String dosage;

    private int quantity;

    private LocalDate expirationDate;

    private String location;

    @Column(nullable = false)
    private UUID hospitalId; // Nuevo campo
}