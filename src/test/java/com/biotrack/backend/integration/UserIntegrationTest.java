package com.biotrack.backend.integration;

import com.biotrack.backend.models.User;
import com.biotrack.backend.models.enums.Role;
import com.biotrack.backend.repositories.UserRepository;
import com.biotrack.backend.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Testcontainers
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class UserIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("biotrack_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        
        userRepository.deleteAll();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_IntegrationTest() throws Exception {
        // Given
        User newUser = User.builder()
                .name("Integration Test User")
                .email("integration@test.com")
                .phoneNumber("1234567890")
                .password("password123")
                .role(Role.MEDIC)
                .specialty("Cardiology")
                .createdAt(LocalDate.now())
                .build();

        // When
        mockMvc.perform(post("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Integration Test User"))
                .andExpect(jsonPath("$.email").value("integration@test.com"))
                .andExpect(jsonPath("$.role").value("MEDIC"));

        // Then
        List<User> users = userRepository.findAll();
        assertThat(users).hasSize(1);
        assertThat(users.get(0).getName()).isEqualTo("Integration Test User");
        assertThat(users.get(0).getEmail()).isEqualTo("integration@test.com");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_IntegrationTest() throws Exception {
        // Given
        User user1 = User.builder()
                .name("User One")
                .email("user1@test.com")
                .phoneNumber("1111111111")
                .password("password123")
                .role(Role.MEDIC)
                .build();

        User user2 = User.builder()
                .name("User Two")
                .email("user2@test.com")
                .phoneNumber("2222222222")
                .password("password123")
                .role(Role.ADMIN)
                .build();

        userRepository.saveAll(List.of(user1, user2));

        // When & Then
        mockMvc.perform(get("/api/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));

        List<User> users = userRepository.findAll();
        assertThat(users).hasSize(2);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUser_IntegrationTest() throws Exception {
        // Given
        User existingUser = User.builder()
                .name("Original Name")
                .email("original@test.com")
                .phoneNumber("1234567890")
                .password("password123")
                .role(Role.MEDIC)
                .build();

        User savedUser = userRepository.save(existingUser);

        User updatedUserData = User.builder()
                .name("Updated Name")
                .email("updated@test.com")
                .phoneNumber("0987654321")
                .password("newpassword")
                .role(Role.ADMIN)
                .specialty("Neurology")
                .build();

        // When
        mockMvc.perform(put("/api/users/{id}", savedUser.getId())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedUserData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.email").value("updated@test.com"))
                .andExpect(jsonPath("$.role").value("ADMIN"));

        // Then
        User updatedUser = userRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(updatedUser.getName()).isEqualTo("Updated Name");
        assertThat(updatedUser.getEmail()).isEqualTo("updated@test.com");
        assertThat(updatedUser.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_IntegrationTest() throws Exception {
        // Given
        User userToDelete = User.builder()
                .name("User To Delete")
                .email("delete@test.com")
                .phoneNumber("1234567890")
                .password("password123")
                .role(Role.MEDIC)
                .build();

        User savedUser = userRepository.save(userToDelete);
        assertThat(userRepository.findAll()).hasSize(1);

        // When
        mockMvc.perform(delete("/api/users/{id}", savedUser.getId())
                .with(csrf()))
                .andExpect(status().isNoContent());

        // Then
        assertThat(userRepository.findAll()).hasSize(0);
        assertThat(userRepository.findById(savedUser.getId())).isEmpty();
    }

    @Test
    void getUserById_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        // Given
        User user = User.builder()
                .name("Test User")
                .email("test@test.com")
                .phoneNumber("1234567890")
                .password("password123")
                .role(Role.MEDIC)
                .build();

        User savedUser = userRepository.save(user);

        // When & Then
        mockMvc.perform(get("/api/users/{id}", savedUser.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
