package com.biotrack.backend.repositories;

import com.biotrack.backend.models.Mutation;
import com.biotrack.backend.models.enums.Relevance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MutationRepository extends JpaRepository<Mutation, UUID> {
    
    // ✅ USAR JPQL en lugar de métodos automáticos para evitar problemas de naming
    @Query("SELECT m FROM Mutation m WHERE m.sample.id = :sampleId")
    List<Mutation> findBySampleId(@Param("sampleId") UUID sampleId);
    
    List<Mutation> findByRelevance(Relevance relevance);
    
    List<Mutation> findByGeneContainingIgnoreCase(String gene);
    
    @Query("SELECT m FROM Mutation m WHERE m.sample.id = :sampleId AND m.relevance = :relevance")
    List<Mutation> findBySampleIdAndRelevance(@Param("sampleId") UUID sampleId, @Param("relevance") Relevance relevance);
    
    @Query("SELECT m FROM Mutation m WHERE m.sample.id = :sampleId AND LOWER(m.gene) LIKE LOWER(CONCAT('%', :gene, '%'))")
    List<Mutation> findBySampleIdAndGeneContainingIgnoreCase(@Param("sampleId") UUID sampleId, @Param("gene") String gene);
    
    @Query("SELECT m FROM Mutation m WHERE m.sample.id = :sampleId AND m.relevance = :relevance AND LOWER(m.gene) LIKE LOWER(CONCAT('%', :gene, '%'))")
    List<Mutation> findBySampleIdAndRelevanceAndGeneContainingIgnoreCase(
        @Param("sampleId") UUID sampleId, 
        @Param("relevance") Relevance relevance, 
        @Param("gene") String gene
    );
}