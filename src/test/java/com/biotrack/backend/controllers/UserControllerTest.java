package com.biotrack.backend.controllers;

import com.biotrack.backend.config.TestConfig;
import com.biotrack.backend.dto.UserDTO;
import com.biotrack.backend.models.User;
import com.biotrack.backend.models.enums.Role;
import com.biotrack.backend.services.MedicalVisitService;
import com.biotrack.backend.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(TestConfig.class)
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private MedicalVisitService medicalVisitService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private UserDTO testUserDTO;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUser = User.builder()
                .id(testUserId)
                .name("John Doe")
                .email("john.doe@test.com")
                .phoneNumber("1234567890")
                .password("password123")
                .role(Role.MEDIC)
                .specialty("Cardiology")
                .createdAt(LocalDate.now())
                .build();

        testUserDTO = new UserDTO(
                testUserId,
                "John Doe",
                "john.doe@test.com",
                "1234567890",
                "password123",
                Role.MEDIC,
                "Cardiology"
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_ShouldReturnCreatedUser() throws Exception {
        // Given
        when(userService.createUser(any(User.class))).thenReturn(testUser);

        // When & Then
        mockMvc.perform(post("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUserDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(testUserId.toString()))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@test.com"))
                .andExpect(jsonPath("$.role").value("MEDIC"));

        verify(userService, times(1)).createUser(any(User.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Given
        UserDTO invalidUserDTO = new UserDTO(
                null,
                "", // nombre vacío
                "invalid-email", // email inválido
                "",
                "",
                Role.MEDIC,
                "Cardiology"
        );

        // When & Then
        mockMvc.perform(post("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUserDTO)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(User.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_ShouldReturnUsersList() throws Exception {
        // Given
        List<User> users = Arrays.asList(testUser);
        when(userService.getAllUsers()).thenReturn(users);

        // When & Then
        mockMvc.perform(get("/api/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(testUserId.toString()))
                .andExpect(jsonPath("$[0].name").value("John Doe"));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserById_ShouldReturnUser() throws Exception {
        // Given
        when(userService.getUserById(testUserId)).thenReturn(testUser);

        // When & Then
        mockMvc.perform(get("/api/users/{id}", testUserId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUserId.toString()))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@test.com"));

        verify(userService, times(1)).getUserById(testUserId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_ShouldReturnNoContent() throws Exception {
        // Given
        doNothing().when(userService).deleteUserById(testUserId);

        // When & Then
        mockMvc.perform(delete("/api/users/{id}", testUserId)
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUserById(testUserId);
    }
}
