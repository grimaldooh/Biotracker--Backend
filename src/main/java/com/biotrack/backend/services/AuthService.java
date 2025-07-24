package com.biotrack.backend.services;

import com.biotrack.backend.models.User;
import com.biotrack.backend.models.Patient;

public interface AuthService {
    Object authenticate(String email, String password); // Retorna User o Patient
}