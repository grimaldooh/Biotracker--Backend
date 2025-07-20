package com.biotrack.backend.models;

import com.biotrack.backend.models.enums.SampleStatus;
import com.biotrack.backend.models.enums.SampleType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "sample") 
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor // <-- Agrega esto
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Sample {
    @Id
    @GeneratedValue
    protected UUID id;

    @ManyToOne
    protected Patient patient;

    @ManyToOne
    protected User registeredBy;

    @Enumerated(EnumType.STRING)
    protected SampleType type;

    @Enumerated(EnumType.STRING)
    protected SampleStatus status;

    protected LocalDate collectionDate;
    protected String notes;
    protected LocalDate createdAt;

    // Métodos abstractos si necesitas lógica específica
    public abstract SampleType getSampleType();
    public abstract String getSampleTypeDescription();
    public abstract boolean isGeneticAnalysisRequired();
    public abstract boolean isValidForProcessing();
    public abstract String getSpecificSampleInfo();

}
