package com.biotrack.backend.services;

import com.biotrack.backend.models.User;
import com.biotrack.backend.models.enums.Role;
import com.biotrack.backend.repositories.UserRepository;
import com.biotrack.backend.services.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private final UserRepository repo = mock(UserRepository.class);
    private final UserService service = new UserServiceImpl(repo);

    @Test
    void createUser_shouldSaveAndReturnUser() {
        User u = User.builder()
                .name("Angel")
                .email("angel@example.com")
                .password("1234")
                .role(Role.TECHNICIAN)
                .build();

        when(repo.save(Mockito.any(User.class))).thenReturn(u);

        User result = service.createUser(u);

        assertEquals("Angel", result.getName());
        assertEquals(Role.TECHNICIAN, result.getRole());
        verify(repo, times(1)).save(u);
    }
}