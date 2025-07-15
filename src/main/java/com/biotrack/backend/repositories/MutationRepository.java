package com.biotrack.backend.repositories;

import com.biotrack.backend.models.Mutation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MutationRepository extends JpaRepository<Mutation, UUID> {
}