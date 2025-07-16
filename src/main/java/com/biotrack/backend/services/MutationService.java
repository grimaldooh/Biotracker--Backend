package com.biotrack.backend.services;

import com.biotrack.backend.models.Mutation;
import com.biotrack.backend.models.enums.Relevance;

import java.util.List;
import java.util.UUID;

public interface MutationService {
    List<Mutation> processResultFile(UUID resultFileId);
    List<Mutation> search(UUID sampleId, Relevance relevance, String gene);
}
