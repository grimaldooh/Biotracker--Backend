package com.biotrack.backend.services;

import com.biotrack.backend.models.Mutation;

import java.util.List;
import java.util.UUID;

public interface MutationService {
    List<Mutation> processResultFile(UUID resultFileId);
}
