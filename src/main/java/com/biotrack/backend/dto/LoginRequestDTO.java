package com.biotrack.backend.dto;

public record LoginRequestDTO(
    String email,
    String password
) {}