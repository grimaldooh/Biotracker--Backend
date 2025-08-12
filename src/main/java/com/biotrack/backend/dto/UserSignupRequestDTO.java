package com.biotrack.backend.dto;

import com.biotrack.backend.models.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserSignupRequestDTO(
    @NotBlank(message = "Name is required")
    String name,
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    String email,
    
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    String password,
    
    @NotNull(message = "Role is required")
    Role role,

    @NotBlank(message = "Phone number is required")
    String phoneNumber,
    
    String specialty // opcional
) {}