package com.biotrack.backend.services;

public interface JwtService {
    /**
     * Genera un token JWT para el usuario
     * @param email email del usuario
     * @param userType tipo de usuario (USER/PATIENT)
     * @return token JWT
     */
    String generateToken(String email, String userType);
    
    /**
     * Valida un token JWT
     * @param token token a validar
     * @return true si es válido
     */
    boolean validateToken(String token);
    
    /**
     * Extrae el email del token
     * @param token token JWT
     * @return email del usuario
     */
    String getEmailFromToken(String token);
    
    /**
     * Extrae el tipo de usuario del token
     * @param token token JWT
     * @return tipo de usuario (USER/PATIENT)
     */
    String getUserTypeFromToken(String token);
}