package com.biotrack.backend.utils;

import com.biotrack.backend.dto.UserDTO;
import com.biotrack.backend.models.User;

public class UserMapper {

    public static UserDTO toDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getPassword(),
                user.getRole(),
                user.getSpecialty()
        );
    }

    public static User toEntity(UserDTO dto) {
        return User.builder()
                .id(dto.id())
                .name(dto.name())
                .email(dto.email())
                .phoneNumber(dto.phoneNumber())
                .password(dto.password())
                .role(dto.role())
                .specialty(dto.specialty())
                .build();
    }
}