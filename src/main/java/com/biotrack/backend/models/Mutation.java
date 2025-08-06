package com.biotrack.backend.models;

import com.biotrack.backend.models.enums.Relevance;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "mutations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mutation {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String gene;
    private String chromosome;
    private String type;

    @Enumerated(EnumType.STRING)
    private Relevance relevance;

    private String comment;

    @ManyToOne
    @JoinColumn(name = "genetic_sample_id")
    private GeneticSample sample;
}
