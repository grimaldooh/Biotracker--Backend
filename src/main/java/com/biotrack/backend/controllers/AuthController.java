package com.biotrack.backend.controllers;

import com.biotrack.backend.dto.*;
import com.biotrack.backend.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication endpoints for users and patients")
public class AuthController {
    
    private final AuthService authService;
    
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    @PostMapping("/login")
    @Operation(
        summary = "Login user or patient",
        description = "Authenticate user or patient with email and password"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        try {
            LoginResponseDTO response = authService.login(loginRequest);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            throw new RuntimeException("Authentication failed: " + e.getMessage(), e);
        }
    }
    
    @PostMapping("/signup/user/{hospitalId}")
    @Operation(
        summary = "Register new user to hospital",
        description = "Create a new user account (doctor, admin, etc.) and link to hospital"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User created and linked to hospital successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data or email already exists"),
        @ApiResponse(responseCode = "404", description = "Hospital not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<LoginResponseDTO> signupUser(
            @Parameter(description = "Hospital ID to link the user to", required = true)
            @PathVariable UUID hospitalId,
            @Valid @RequestBody UserSignupRequestDTO signupRequest) {
        try {
            LoginResponseDTO response = authService.signupUser(signupRequest, hospitalId);
            return ResponseEntity.status(201).body(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Hospital not found")) {
                throw new RuntimeException("Hospital not found with ID: " + hospitalId, e);
            }
            throw new RuntimeException("User registration failed: " + e.getMessage(), e);
        }
    }
    
    @PostMapping("/signup/patient/{hospitalId}")
    @Operation(
        summary = "Register new patient to hospital",
        description = "Create a new patient account and link to hospital"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Patient created and linked to hospital successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data or email/phone already exists"),
        @ApiResponse(responseCode = "404", description = "Hospital not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<LoginResponseDTO> signupPatient(
            @Parameter(description = "Hospital ID to link the patient to", required = true)
            @PathVariable UUID hospitalId,
            @Valid @RequestBody PatientSignupRequestDTO signupRequest) {
        try {
            LoginResponseDTO response = authService.signupPatient(signupRequest, hospitalId);
            return ResponseEntity.status(201).body(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Hospital not found")) {
                throw new RuntimeException("Hospital not found with ID: " + hospitalId, e);
            }
            throw new RuntimeException("Patient registration failed: " + e.getMessage(), e);
        }
    }
}