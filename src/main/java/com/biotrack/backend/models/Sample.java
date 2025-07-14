package com.biotrack.backend.models;

import com.biotrack.backend.models.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import com.biotrack.backend.models.enums.SampleStatus;
import com.biotrack.backend.models.enums.SampleType;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

@Entity
@Table(name = "samples")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sample {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "registered_by")
    private User registeredBy;

    @Enumerated(EnumType.STRING)
    private SampleType type;

    @Enumerated(EnumType.STRING)
    private SampleStatus status;

    private LocalDate collectionDate;
    private String notes;

    private LocalDate createdAt;
}
