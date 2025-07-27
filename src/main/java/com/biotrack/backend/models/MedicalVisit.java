package com.biotrack.backend.models;

import com.biotrack.backend.models.enums.MedicalVisitType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "medical_visits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalVisit {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private User doctor;

    @Column(name = "visit_date", nullable = false)
    private LocalDateTime visitDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(columnDefinition = "TEXT")
    private String diagnosis;

    @Column(columnDefinition = "TEXT")
    private String recommendations;

    @Column(name = "medical_entity_id", nullable = false)
    private UUID medicalEntityId;

    @Column(name = "visit_completed", nullable = false)
    private boolean visitCompleted = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private MedicalVisitType type;

    @Column(name = "medicalArea")
    private String medicalArea;
}