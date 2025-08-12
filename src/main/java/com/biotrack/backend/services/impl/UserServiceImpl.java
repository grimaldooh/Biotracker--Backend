package com.biotrack.backend.services.impl;

import com.biotrack.backend.dto.PrimaryHospitalDTO;
import com.biotrack.backend.models.User;
import com.biotrack.backend.models.Hospital;
import com.biotrack.backend.repositories.UserRepository;
import com.biotrack.backend.services.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.biotrack.backend.models.enums.Role;

@Service
@Transactional
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
    @Transactional
    public void deleteUserById(UUID id) {
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public User updateUser(UUID id, User updatedUser) {
        User existing = getUserById(id);
        existing.setName(updatedUser.getName());
        existing.setEmail(updatedUser.getEmail());
        existing.setPassword(updatedUser.getPassword());
        existing.setRole(updatedUser.getRole());
        return userRepository.save(existing);
    }

    @Override
    public List<User> getMedicsByHospitalId(UUID hospitalId) {
        return userRepository.findByHospitals_IdAndRole(hospitalId, Role.MEDIC);
    }

    @Override
    public Optional<PrimaryHospitalDTO> getPrimaryHospital(UUID userId) {
        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isEmpty()) {
            return Optional.empty();
        }

        User user = userOpt.get();

        // Verificar si el usuario tiene hospitales asignados
        if (user.getHospitals() == null || user.getHospitals().isEmpty()) {
            return Optional.empty();
        }

        // Obtener el primer hospital (índice 0)
        Hospital primaryHospital = user.getHospitals().get(0);

        return Optional.of(PrimaryHospitalDTO.fromHospital(primaryHospital));
    }
}