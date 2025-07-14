package com.biotrack.backend.repositories;

import com.biotrack.backend.models.Sample;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SampleRepository extends JpaRepository<Sample, UUID>{
}
