package com.biotrack.backend.controllers;

import com.biotrack.backend.dto.PatientDTO;
import com.biotrack.backend.dto.UserDTO;
import com.biotrack.backend.models.Hospital;
import com.biotrack.backend.models.Patient;
import com.biotrack.backend.models.User;
import com.biotrack.backend.services.HospitalService;
import com.biotrack.backend.services.UserService;
import com.biotrack.backend.utils.PatientMapper;
import com.biotrack.backend.utils.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/hospitals")
@Tag(name = "Hospitals", description = "Hospital management and analytics")
public class HospitalController {

    private final HospitalService service;
    private final UserService userService;


    public HospitalController(HospitalService service, UserService userService) {
        this.service = service;
        this.userService = userService;
    }

    @PostMapping
    @Operation(summary = "Create hospital", description = "Register a new hospital entity")
    public ResponseEntity<Hospital> create(@RequestBody Hospital hospital) {
        return ResponseEntity.status(201).body(service.create(hospital));
    }

    @GetMapping
    @Operation(summary = "List all hospitals", description = "Get all hospitals")
    public ResponseEntity<List<Hospital>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get hospital by ID", description = "Get hospital details")
    public ResponseEntity<Hospital> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete hospital", description = "Remove a hospital entity")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/register-user/{hospitalId}")
    @Operation(
            summary = "Create new user",
            description = "Register a new system user with specific role and permissions"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "User created successfully",
                    content = @Content(schema = @Schema(implementation = UserDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data or validation errors"
            )
    })
    public ResponseEntity<UserDTO> create(@Valid @RequestBody UserDTO userDTO, @PathVariable UUID hospitalId) {
        try {
            User saved = service.registerUser( hospitalId , UserMapper.toEntity(userDTO));

            return ResponseEntity.status(HttpStatus.CREATED).body(UserMapper.toDTO(saved));
        } catch (Exception e) {
            throw new RuntimeException("Error creating user: " + e.getMessage(), e);
        }
    }

    @PostMapping("/register-patient/{hospitalId}")
    @Operation(
            summary = "Register new patient",
            description = "Register a new patient in the system and link to a hospital"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Patient registered successfully",
                    content = @Content(schema = @Schema(implementation = PatientDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data or validation errors"
            )
    })
    public ResponseEntity<PatientDTO> registerPatient(@Valid @RequestBody PatientDTO patientDTO, @PathVariable UUID hospitalId) {
        try {
            Patient saved = service.registerPatient(hospitalId, PatientMapper.toEntity(patientDTO));
            return ResponseEntity.status(HttpStatus.CREATED).body(PatientMapper.toDTO(saved));
        } catch (Exception e) {
            throw new RuntimeException("Error registering patient: " + e.getMessage(), e);
        }
    }

}