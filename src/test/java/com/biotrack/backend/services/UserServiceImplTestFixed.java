package com.biotrack.backend.services;

import com.biotrack.backend.models.User;
import com.biotrack.backend.models.enums.Role;
import com.biotrack.backend.repositories.UserRepository;
import com.biotrack.backend.services.impl.UserServiceImpl;
import com.biotrack.backend.utils.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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
        testUser = TestDataFactory.createTestUser();
        testUser.setId(testUserId);
    }

    @Test
    void createUser_ShouldReturnSavedUser() {
        // Given
        User userToSave = TestDataFactory.createTestUser();
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.createUser(userToSave);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testUserId);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void getUserById_ShouldReturnUser_WhenUserExists() {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        // When
        User result = userService.getUserById(testUserId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testUserId);
        verify(userRepository).findById(testUserId);
    }

    @Test
    void getUserById_ShouldThrowException_WhenUserNotFound() {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserById(testUserId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User no encontrado");

        verify(userRepository).findById(testUserId);
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        // Given
        List<User> users = Arrays.asList(testUser, TestDataFactory.createTestUser());
        when(userRepository.findAll()).thenReturn(users);

        // When
        List<User> result = userService.getAllUsers();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).contains(testUser);
        verify(userRepository).findAll();
    }

    @Test
    void updateUser_ShouldReturnUpdatedUser_WhenUserExists() {
        // Given
        User updateData = TestDataFactory.createTestUser();
        updateData.setEmail("updated@example.com");
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.updateUser(testUserId, updateData);

        // Then
        assertThat(result).isNotNull();
        verify(userRepository).findById(testUserId);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_ShouldThrowException_WhenUserNotFound() {
        // Given
        User updateData = TestDataFactory.createTestUser();
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.updateUser(testUserId, updateData))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User no encontrado");

        verify(userRepository).findById(testUserId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUserById_ShouldDeleteUser() {
        // Given
        doNothing().when(userRepository).deleteById(testUserId);

        // When
        userService.deleteUserById(testUserId);

        // Then
        verify(userRepository).deleteById(testUserId);
    }

    @Test
    void getMedicsByHospitalId_ShouldReturnMedics() {
        // Given
        UUID hospitalId = UUID.randomUUID();
        List<User> medics = Arrays.asList(testUser);
        when(userRepository.findByHospitals_IdAndRole(hospitalId, Role.MEDIC)).thenReturn(medics);

        // When
        List<User> result = userService.getMedicsByHospitalId(hospitalId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result).contains(testUser);
        verify(userRepository).findByHospitals_IdAndRole(hospitalId, Role.MEDIC);
    }
}
