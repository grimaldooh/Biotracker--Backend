package com.biotrack.backend.services.impl;

import com.biotrack.backend.dto.*;
import com.biotrack.backend.models.Hospital;
import com.biotrack.backend.models.Patient;
import com.biotrack.backend.models.User;
import com.biotrack.backend.repositories.HospitalRepository;
import com.biotrack.backend.repositories.PatientRepository;
import com.biotrack.backend.repositories.UserRepository;
import com.biotrack.backend.services.AuthService;
import com.biotrack.backend.services.HospitalService;
import com.biotrack.backend.services.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {
    
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final HospitalRepository hospitalRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private final HospitalServiceImpl hospitalService;
    
    public AuthServiceImpl(UserRepository userRepository,
                          PatientRepository patientRepository,
                          HospitalRepository hospitalRepository,
                          HospitalServiceImpl hospitalService,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService) {
        this.userRepository = userRepository;
        this.hospitalRepository = hospitalRepository;
        this.hospitalService = hospitalService;
        this.patientRepository = patientRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }
    
    @Override
    public LoginResponseDTO login(LoginRequestDTO loginRequest) {
        // 1. Buscar primero en usuarios
        Optional<User> userOpt = userRepository.findByEmail(loginRequest.email());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(loginRequest.password(), user.getPassword())) {
                String token = jwtService.generateToken(user.getEmail(), user.getRole().toString());
                return LoginResponseDTO.fromUser(user, token);
            }
        }

        // 2. Si no es usuario, buscar en pacientes
        Optional<Patient> patientOpt = patientRepository.findByEmail(loginRequest.email());
        if (patientOpt.isPresent()) {
            Patient patient = patientOpt.get();
            if (passwordEncoder.matches(loginRequest.password(), patient.getPassword())) {
                String token = jwtService.generateToken(patient.getEmail(), "PATIENT");
                return LoginResponseDTO.fromPatient(patient, token);
            }
        }

        throw new RuntimeException("Invalid email or password");
    }
    
    @Override
    public LoginResponseDTO signupUser(UserSignupRequestDTO signupRequest, UUID hospitalId) {
        // 1. Validar que el hospital exista
        Hospital hospital = hospitalService.findById(hospitalId);
        if (hospital == null) {
            throw new RuntimeException("Hospital not found with ID: " + hospitalId);
        }
        
        // 2. Verificar si el email ya existe como usuario
        Optional<User> existingUserOpt = userRepository.findByEmail(signupRequest.email());
        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();
            
            // Si ya existe, vincular al hospital si no está ya vinculado
            if (!existingUser.getHospitals().contains(hospital)) {
                existingUser.getHospitals().add(hospital);
                userRepository.save(existingUser);
                
                hospital.getAuthorizedUsers().add(existingUser);
                hospitalRepository.save(hospital);
            }
            
            String token = jwtService.generateToken(existingUser.getEmail(), existingUser.getRole().toString());
            return LoginResponseDTO.fromUser(existingUser, token);
        }
        
        // 3. Verificar que el email no exista como paciente
        if (patientRepository.findByEmail(signupRequest.email()).isPresent()) {
            throw new RuntimeException("Email already exists as a patient");
        }
        
        // 4. Crear nuevo usuario
        User newUser = User.builder()
                .name(signupRequest.name())
                .email(signupRequest.email())
                .password(passwordEncoder.encode(signupRequest.password()))
                .role(signupRequest.role())
                .specialty(signupRequest.specialty())
                .phoneNumber(signupRequest.phoneNumber())
                .createdAt(LocalDate.now())
                .build();
        
        // 5. Vincular usuario al hospital
        newUser.getHospitals().add(hospital);
        newUser = userRepository.save(newUser);
        
        // 6. Vincular hospital al usuario
        hospital.getAuthorizedUsers().add(newUser);
        hospitalRepository.save(hospital);
        
        // 7. Generar token y retornar respuesta
        String token = jwtService.generateToken(newUser.getEmail(), newUser.getRole().toString());
        return LoginResponseDTO.fromUser(newUser, token);
    }
    
    @Override
    public LoginResponseDTO signupPatient(PatientSignupRequestDTO signupRequest, UUID hospitalId) {
        // 1. Validar que el hospital exista
        Hospital hospital = hospitalService.findById(hospitalId);
        if (hospital == null) {
            throw new RuntimeException("Hospital not found with ID: " + hospitalId);
        }
        
        // 2. Verificar si el email ya existe como paciente
        Optional<Patient> existingPatientOpt = patientRepository.findByEmail(signupRequest.email());
        if (existingPatientOpt.isPresent()) {
            Patient existingPatient = existingPatientOpt.get();
            
            // Si ya existe, vincular al hospital si no está ya vinculado
            if (!existingPatient.getHospitals().contains(hospital)) {
                existingPatient.getHospitals().add(hospital);
                patientRepository.save(existingPatient);
                
                hospital.getActivePatients().add(existingPatient);
                hospitalRepository.save(hospital);
            }
            
            String token = jwtService.generateToken(existingPatient.getEmail(), "PATIENT");
            return LoginResponseDTO.fromPatient(existingPatient, token);
        }
        
        // 3. Verificar que el email no exista como usuario
        if (userRepository.findByEmail(signupRequest.email()).isPresent()) {
            throw new RuntimeException("Email already exists as a user");
        }
        
        // 4. Verificar que el teléfono no exista
        if (patientRepository.findByPhoneNumber(signupRequest.phoneNumber()).isPresent()) {
            throw new RuntimeException("Phone number already exists");
        }
        
        // 5. Crear nuevo paciente
        Patient newPatient = Patient.builder()
                .firstName(signupRequest.firstName())
                .lastName(signupRequest.lastName())
                .email(signupRequest.email())
                .password(passwordEncoder.encode(signupRequest.password()))
                .phoneNumber(signupRequest.phoneNumber())
                .birthDate(signupRequest.birthDate())
                .gender(signupRequest.gender())
                .curp(signupRequest.curp())
                .createdAt(LocalDate.now())
                .build();
        
        // 6. Vincular paciente al hospital
        newPatient.getHospitals().add(hospital);
        newPatient = patientRepository.save(newPatient);
        
        // 7. Vincular hospital al paciente
        hospital.getActivePatients().add(newPatient);
        hospitalRepository.save(hospital);
        
        // 8. Generar token y retornar respuesta
        String token = jwtService.generateToken(newPatient.getEmail(), "PATIENT");
        return LoginResponseDTO.fromPatient(newPatient, token);
    }
}