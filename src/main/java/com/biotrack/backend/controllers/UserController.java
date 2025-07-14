package com.biotrack.backend.controllers;

import com.biotrack.backend.models.User;
import com.biotrack.backend.services.UserService;
import com.biotrack.backend.exceptions.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<User> create(@RequestBody User user) {
        if (user == null) {
            throw new ValidationException("User data cannot be null");
        }
        User created = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<User>> getAll() {
        try {
            List<User> users = userService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            throw new ValidationException("Error retrieving users: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable UUID id) {
        if (id == null) {
            throw new ValidationException("User ID cannot be null");
        }
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> update(@PathVariable UUID id, @RequestBody User user){
        if (id == null) {
            throw new ValidationException("User ID cannot be null");
        }
        if (user == null) {
            throw new ValidationException("User data cannot be null");
        }
        User updated = userService.updateUser(id, user);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (id == null) {
            throw new ValidationException("User ID cannot be null");
        }
        userService.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }
}