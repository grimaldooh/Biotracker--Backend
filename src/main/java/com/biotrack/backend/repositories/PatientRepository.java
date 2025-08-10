package com.biotrack.backend.repositories;

import com.biotrack.backend.models.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PatientRepository extends JpaRepository<Patient, UUID> {
    Optional<Patient> findByEmail(String email);

    List<Patient> findByFirstNameContainingIgnoreCaseAndLastNameContainingIgnoreCase(String firstName, String lastName);

    @Query("""
        SELECT p FROM Patient p
        JOIN p.hospitals h
        WHERE h.id = :hospitalId
        AND (
            LOWER(p.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR
            LOWER(p.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR
            LOWER(p.email) LIKE LOWER(CONCAT('%', :query, '%')) OR
            LOWER(p.curp) LIKE LOWER(CONCAT('%', :query, '%'))
        )
    """)
    List<Patient> searchPatientsByHospitalAndQuery(@Param("hospitalId") UUID hospitalId, @Param("query") String query);
}
