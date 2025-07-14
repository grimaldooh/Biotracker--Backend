package com.biotrack.backend.utils;

import com.biotrack.backend.dto.UserDTO;
import com.biotrack.backend.models.User;

public class UserMapper {

    public static UserDTO toDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPassword(),
                user.getRole()
        );
    }

    public static User toEntity(UserDTO dto) {
        return User.builder()
                .id(dto.id())
                .name(dto.name())
                .email(dto.email())
                .password(dto.password())
                .role(dto.role())
                .build();
    }
}