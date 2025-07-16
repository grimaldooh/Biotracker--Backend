package com.biotrack.backend.repositories;

import com.biotrack.backend.models.Mutation;
import com.biotrack.backend.models.enums.Relevance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MutationRepository extends JpaRepository<Mutation, UUID> {List<Mutation> findBySampleId(UUID sampleId);
    List<Mutation> findByRelevance(Relevance relevance);
    List<Mutation> findByGeneContainingIgnoreCase(String gene);
    List<Mutation> findBySampleIdAndRelevance(UUID sampleId, Relevance relevance);
    List<Mutation> findBySampleIdAndGeneContainingIgnoreCase(UUID sampleId, String gene);
    List<Mutation> findBySampleIdAndRelevanceAndGeneContainingIgnoreCase(UUID sampleId, Relevance relevance, String gene);
}