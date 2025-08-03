package com.biotrack.backend.models;

import com.biotrack.backend.models.enums.LabAppointmentStatus;
import com.biotrack.backend.models.enums.SampleType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "lab_appointments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabAppointment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private UUID medicalEntityId; // ID de la entidad médica que solicita la cita

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User doctor;

    @ManyToOne(optional = false)
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LabAppointmentStatus status; // SOLICITADA, COMPLETADA

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SampleType sampleType; // SANGRE, SALIVA, DNA, etc.

    @Column(columnDefinition = "TEXT")
    private String notes;

    // Puedes agregar más campos si lo necesitas (ej: fecha de cita, resultado, etc.)
}