package com.biotrack.backend.services.impl;

import com.biotrack.backend.models.User;
import com.biotrack.backend.models.enums.Role;
import com.biotrack.backend.repositories.UserRepository;
import com.biotrack.backend.services.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
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
    }

    @Test
    void createUser_ShouldReturnSavedUser() {
        // Given
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.createUser(testUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testUserId);
        assertThat(result.getName()).isEqualTo("John Doe");
        assertThat(result.getEmail()).isEqualTo("john.doe@test.com");
        
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void getAllUsers_ShouldReturnListOfUsers() {
        // Given
        User anotherUser = User.builder()
                .id(UUID.randomUUID())
                .name("Jane Smith")
                .email("jane.smith@test.com")
                .phoneNumber("0987654321")
                .role(Role.ADMIN)
                .build();

        List<User> expectedUsers = Arrays.asList(testUser, anotherUser);
        when(userRepository.findAll()).thenReturn(expectedUsers);

        // When
        List<User> result = userService.getAllUsers();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(testUser, anotherUser);
        
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getUserById_WithExistingId_ShouldReturnUser() {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        // When
        User result = userService.getUserById(testUserId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testUserId);
        assertThat(result.getName()).isEqualTo("John Doe");
        
        verify(userRepository, times(1)).findById(testUserId);
    }

    @Test
    void getUserById_WithNonExistingId_ShouldThrowException() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserById(nonExistentId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User no encontrado");
        
        verify(userRepository, times(1)).findById(nonExistentId);
    }

    @Test
    void updateUser_WithExistingUser_ShouldReturnUpdatedUser() {
        // Given
        User updatedUserData = User.builder()
                .name("Jane Doe")
                .email("jane.doe@test.com")
                .phoneNumber("0987654321")
                .role(Role.ADMIN)
                .specialty("Neurology")
                .build();

        User existingUser = User.builder()
                .id(testUserId)
                .name("John Doe")
                .email("john.doe@test.com")
                .phoneNumber("1234567890")
                .role(Role.MEDIC)
                .specialty("Cardiology")
                .build();

        User expectedUpdatedUser = User.builder()
                .id(testUserId)
                .name("Jane Doe")
                .email("jane.doe@test.com")
                .phoneNumber("0987654321")
                .role(Role.ADMIN)
                .specialty("Neurology")
                .build();

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(expectedUpdatedUser);

        // When
        User result = userService.updateUser(testUserId, updatedUserData);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testUserId);
        assertThat(result.getName()).isEqualTo("Jane Doe");
        assertThat(result.getEmail()).isEqualTo("jane.doe@test.com");
        assertThat(result.getRole()).isEqualTo(Role.ADMIN);
        
        verify(userRepository, times(1)).findById(testUserId);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUser_WithNonExistingUser_ShouldThrowException() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        User updatedUserData = User.builder()
                .name("Jane Doe")
                .email("jane.doe@test.com")
                .build();

        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.updateUser(nonExistentId, updatedUserData))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User no encontrado");
        
        verify(userRepository, times(1)).findById(nonExistentId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUserById_WithExistingUser_ShouldDeleteSuccessfully() {
        // Given
        doNothing().when(userRepository).deleteById(testUserId);

        // When
        userService.deleteUserById(testUserId);

        // Then
        verify(userRepository, times(1)).deleteById(testUserId);
    }
}
