package com.biotrack.backend.utils;

import com.biotrack.backend.dto.GeneticSampleDTO;
import com.biotrack.backend.dto.GeneticSampleCreationDTO;
import com.biotrack.backend.dto.MutationDTO;
import com.biotrack.backend.dto.MutationCreationDTO;
import com.biotrack.backend.models.GeneticSample;
import com.biotrack.backend.models.Mutation;
import com.biotrack.backend.models.Patient;
import com.biotrack.backend.models.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GeneticSampleMapper {

    public static GeneticSampleDTO toDTO(GeneticSample geneticSample) {
        List<MutationDTO> mutationDTOs = geneticSample.getMutations() != null 
            ? geneticSample.getMutations().stream()
                .map(mutation -> new MutationDTO(
                    mutation.getId(),
                    mutation.getGene(),
                    mutation.getChromosome(),
                    mutation.getType(),
                    mutation.getRelevance(),
                    mutation.getComment(),
                    geneticSample.getId()
                )).collect(Collectors.toList())
            : List.of();

        return new GeneticSampleDTO(
            geneticSample.getId(),
            geneticSample.getPatient().getId(),
            geneticSample.getRegisteredBy().getId(),
            geneticSample.getType(),
            geneticSample.getStatus(),
            geneticSample.getMedicalEntityId(),
            geneticSample.getCollectionDate(),
            geneticSample.getNotes(),
            geneticSample.getCreatedAt(),
            mutationDTOs,
            geneticSample.getConfidenceScore(),
            geneticSample.getProcessingSoftware(),
            geneticSample.getReferenceGenome(),
            geneticSample.getMutationCount()
        );
    }

    public static GeneticSample fromCreationDTO(GeneticSampleCreationDTO dto, Patient patient, User registeredBy) {
        // ✅ CREAR: La muestra genética base
        GeneticSample geneticSample = GeneticSample.builder()
            .patient(patient)
            .registeredBy(registeredBy)
            .type(dto.type())
            .status(dto.status())
            .medicalEntityId(dto.medicalEntityId())
            .collectionDate(dto.collectionDate())
            .notes(dto.notes())
            .createdAt(LocalDate.now())
            .confidenceScore(dto.confidenceScore())
            .processingSoftware(dto.processingSoftware())
            .referenceGenome(dto.referenceGenome())
            .mutations(new ArrayList<>()) // ✅ INICIALIZAR: Lista vacía de mutaciones
            .build();

        // ✅ AGREGAR: Mutaciones si vienen en el DTO
        if (dto.mutations() != null && !dto.mutations().isEmpty()) {
            List<Mutation> mutations = dto.mutations().stream()
                .map(mutationDTO -> createMutationFromDTO(mutationDTO, geneticSample))
                .collect(Collectors.toList());
            
            geneticSample.setMutations(mutations);
        }

        return geneticSample;
    }

    // ✅ NUEVO: Método auxiliar para crear mutaciones
    private static Mutation createMutationFromDTO(MutationCreationDTO dto, GeneticSample geneticSample) {
        return Mutation.builder()
            .gene(dto.gene())
            .chromosome(dto.chromosome())
            .type(dto.type())
            .relevance(dto.relevance())
            .comment(dto.comment())
            .sample(geneticSample)
            .build();
    }
}