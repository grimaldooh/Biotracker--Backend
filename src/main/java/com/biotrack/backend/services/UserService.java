package com.biotrack.backend.services;

import com.biotrack.backend.models.User;

import java.util.List;
import java.util.UUID;

public interface UserService {
    User createUser(User user);
    List<User> getAllUsers();
    User getUserById(UUID id);
    void deleteUserById(UUID id);
}