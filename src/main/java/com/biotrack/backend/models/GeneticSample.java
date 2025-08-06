package com.biotrack.backend.models;

import com.biotrack.backend.models.enums.SampleStatus;
import com.biotrack.backend.models.enums.SampleType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "genetic_samples")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeneticSample {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registered_by_id")
    private User registeredBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private SampleType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private SampleStatus status;

    @Column(name = "medical_entity_id")
    private UUID medicalEntityId;

    @Column(name = "collection_date")
    private LocalDate collectionDate;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at")
    private LocalDate createdAt;

    // Campos específicos de análisis genético
    @OneToMany(mappedBy = "sample", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Mutation> mutations = new ArrayList<>();

    @Column(name = "confidence_score", precision = 5, scale = 2)
    private BigDecimal confidenceScore;

    @Column(name = "processing_software")
    private String processingSoftware;

    @Column(name = "reference_genome")
    private String referenceGenome;

    // Métodos de utilidad
    public void addMutation(Mutation mutation) {
        mutations.add(mutation);
        mutation.setSample(this);
    }

    public void removeMutation(Mutation mutation) {
        mutations.remove(mutation);
        mutation.setSample(null);
    }

    public int getMutationCount() {
        return mutations != null ? mutations.size() : 0;
    }
}