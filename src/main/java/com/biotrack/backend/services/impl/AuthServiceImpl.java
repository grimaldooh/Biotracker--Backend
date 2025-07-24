package com.biotrack.backend.services.impl;

import com.biotrack.backend.models.User;
import com.biotrack.backend.models.Patient;
import com.biotrack.backend.repositories.UserRepository;
import com.biotrack.backend.repositories.PatientRepository;
import com.biotrack.backend.services.AuthService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;

    public AuthServiceImpl(UserRepository userRepository, PatientRepository patientRepository) {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
    }

    @Override
    public Object authenticate(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent() && userOpt.get().getPassword().equals(password)) {
            return userOpt.get();
        }
        Optional<Patient> patientOpt = patientRepository.findByEmail(email);
        if (patientOpt.isPresent() && patientOpt.get().getPassword().equals(password)) {
            return patientOpt.get();
        }
        return null;
    }
}