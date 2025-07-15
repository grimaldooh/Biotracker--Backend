package com.biotrack.backend.repositories;

import com.biotrack.backend.models.ResultFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ResultFileRepository extends JpaRepository<ResultFile, UUID> {
}