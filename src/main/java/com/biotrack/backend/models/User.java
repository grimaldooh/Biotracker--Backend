package com.biotrack.backend.models;

import com.biotrack.backend.models.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.time.LocalDate;

@Entity 
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String phoneNumber;

    @Column(nullable = false, unique = true)
    private String email;

    private String password;

    @ManyToMany(mappedBy = "authorizedUsers")
    @Builder.Default
    private List<Hospital> hospitals = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "specialty", nullable = true)
    private String specialty;

    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt = LocalDate.now();

    //Methods 

    
}