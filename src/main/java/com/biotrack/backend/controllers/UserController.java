package com.biotrack.backend.controllers;

import com.biotrack.backend.dto.UserDTO;
import com.biotrack.backend.exceptions.ResourceNotFoundException;
import com.biotrack.backend.models.User;
import com.biotrack.backend.services.UserService;
import com.biotrack.backend.utils.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "System user management (doctors, technicians, administrators)")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
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
    public ResponseEntity<UserDTO> create(@Valid @RequestBody UserDTO userDTO) {
        try {
            User saved = userService.createUser(UserMapper.toEntity(userDTO));
            return ResponseEntity.status(HttpStatus.CREATED).body(UserMapper.toDTO(saved));
        } catch (Exception e) {
            throw new RuntimeException("Error creating user: " + e.getMessage(), e);
        }
    }

    @GetMapping
    @Operation(
        summary = "Get all users",
        description = "Retrieve a list of all system users with their roles and basic information"
    )
    @ApiResponse(
        responseCode = "200",
        description = "User list retrieved successfully"
    )
    public ResponseEntity<List<UserDTO>> getAll() {
        List<UserDTO> users = userService.getAllUsers().stream()
                .map(UserMapper::toDTO)
                .toList();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get user by ID",
        description = "Retrieve detailed information of a specific system user"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User found successfully",
            content = @Content(schema = @Schema(implementation = UserDTO.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found with the provided ID"
        )
    })
    public ResponseEntity<UserDTO> getById(
        @Parameter(description = "Unique identifier of the user") 
        @PathVariable UUID id
    ) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(UserMapper.toDTO(user));
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Update user",
        description = "Update the information and role of an existing system user"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User updated successfully",
            content = @Content(schema = @Schema(implementation = UserDTO.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found with the provided ID"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data or validation errors"
        )
    })
    public ResponseEntity<UserDTO> update(
        @Parameter(description = "Unique identifier of the user") 
        @PathVariable UUID id, 
        @Valid @RequestBody UserDTO userDTO
    ) {
        User updated = userService.updateUser(id, UserMapper.toEntity(userDTO));
        return ResponseEntity.ok(UserMapper.toDTO(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete user",
        description = "Remove a system user permanently (use with caution)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "User deleted successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found with the provided ID"
        )
    })
    public ResponseEntity<Void> delete(
        @Parameter(description = "Unique identifier of the user") 
        @PathVariable UUID id
    ) {
        userService.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }

    // Exception handlers locales para este controlador
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((org.springframework.validation.FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }
}