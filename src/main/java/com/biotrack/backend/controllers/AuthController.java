package com.biotrack.backend.controllers;

import com.biotrack.backend.dto.LoginRequestDTO;
import com.biotrack.backend.models.User;
import com.biotrack.backend.models.Patient;
import com.biotrack.backend.services.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO request) {
        Object userOrPatient = authService.authenticate(request.email(), request.password());
        if (userOrPatient == null) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
        // Aquí deberías generar y retornar un JWT, pero para el ejemplo retornamos el objeto
        return ResponseEntity.ok(userOrPatient);
    }
}