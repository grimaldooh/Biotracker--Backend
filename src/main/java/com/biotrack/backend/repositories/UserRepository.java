package com.biotrack.backend.repositories;


import com.biotrack.backend.models.User;
import com.biotrack.backend.models.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    List<User> findByHospitals_IdAndRole(UUID hospitalId, Role role);
}

