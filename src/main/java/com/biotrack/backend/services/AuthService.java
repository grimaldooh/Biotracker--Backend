package com.biotrack.backend.services;

import com.biotrack.backend.dto.*;

import java.util.UUID;

public interface AuthService {
    /**
     * Autentica usuario o paciente con email y password
     * @param loginRequest datos de login
     * @return información del usuario/paciente autenticado
     */
    LoginResponseDTO login(LoginRequestDTO loginRequest);
    
    /**
     * Registra un nuevo usuario
     * @param signupRequest datos del usuario
     * @return información del usuario creado
     */
    LoginResponseDTO signupUser(UserSignupRequestDTO signupRequest, UUID hospitalId);
    
    /**
     * Registra un nuevo paciente
     * @param signupRequest datos del paciente
     * @return información del paciente creado
     */
    LoginResponseDTO signupPatient(PatientSignupRequestDTO signupRequest, UUID hospitalId);
}