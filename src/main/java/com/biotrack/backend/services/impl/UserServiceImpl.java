package com.biotrack.backend.services.impl;

import com.biotrack.backend.models.User;
import com.biotrack.backend.repositories.UserRepository;
import com.biotrack.backend.services.UserService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User createUser(User User) {
        return userRepository.save(User);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User no encontrado"));
    }

    @Override
    public void deleteUserById(UUID id) {
        userRepository.deleteById(id);
    }
}