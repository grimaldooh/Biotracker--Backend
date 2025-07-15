package com.biotrack.backend.exceptions;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Standard error response format")
public class ErrorResponse {
    
    @Schema(description = "HTTP status code", example = "404")
    private int status;
    
    @Schema(description = "Error type", example = "Not Found")
    private String error;
    
    @Schema(description = "Detailed error message", example = "Patient not found with id: 123")
    private String message;

    public ErrorResponse(int status, String error, String message) {
        this.status = status;
        this.error = error;
        this.message = message;
    }

    // Getters y setters
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}