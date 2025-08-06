package com.biotrack.backend.utils;

import com.biotrack.backend.dto.MutationDTO;
import com.biotrack.backend.models.Mutation;

public class MutationMapper {
    public static MutationDTO toDTO(Mutation m) {
        return new MutationDTO(
                m.getId(),
                m.getGene(),
                m.getChromosome(),
                m.getType(),
                m.getRelevance(),
                m.getComment(),
                m.getSample().getId() 
        );
    }
}