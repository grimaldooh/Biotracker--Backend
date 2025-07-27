package com.biotrack.backend.services.impl;

import com.biotrack.backend.models.Hospital;
import com.biotrack.backend.models.Patient;
import com.biotrack.backend.models.Sample;
import com.biotrack.backend.models.User;
import com.biotrack.backend.repositories.HospitalRepository;
import com.biotrack.backend.repositories.PatientRepository;
import com.biotrack.backend.repositories.SampleRepository;
import com.biotrack.backend.repositories.UserRepository;
import com.biotrack.backend.services.HospitalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class HospitalServiceImpl implements HospitalService {

    private final HospitalRepository repository;

    private UserRepository userRepository;

    private PatientRepository patientRepository;

    private SampleRepository sampleRepository;

    public HospitalServiceImpl(HospitalRepository repository,
                               UserRepository userRepository,
                               PatientRepository patientRepository,
                               SampleRepository sampleRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.sampleRepository = sampleRepository;
    }

    @Override
    public Hospital create(Hospital hospital) {
        return repository.save(hospital);
    }

    @Override
    public List<Hospital> findAll() {
        return repository.findAll();
    }

    @Override
    public Hospital findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hospital not found"));
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    @Override
    public User registerUser(UUID hospitalId, User user) {
        Hospital hospital = findById(hospitalId);

        // Check if email is already registered as a user
        if (isEmailRegistered(user.getEmail())) {
            User existingUser = userRepository.findByEmail(user.getEmail())
                    .orElseThrow(() -> new RuntimeException("User already registered, linking to hospital"));
            existingUser.getHospitals().add(hospital);
            userRepository.save(existingUser);
            hospital.getAuthorizedUsers().add(existingUser);
            repository.save(hospital);
            return existingUser;
        } else {
            user.getHospitals().add(hospital);
            User newUser = userRepository.save(user);
            hospital.getAuthorizedUsers().add(newUser);
            repository.save(hospital);
            return newUser;
        }
    }

    @Override
    public Patient registerPatient(UUID hospitalId, Patient patient) {
        Hospital hospital = findById(hospitalId);
        patient.setCreatedAt(LocalDateTime.now().toLocalDate());

        if (isEmailRegistered(patient.getEmail())) {

            Patient existingPatient = patientRepository.findByEmail(patient.getEmail())
                    .orElseThrow(() -> new RuntimeException("Patient already registered, linking to hospital"));
            existingPatient.getHospitals().add(hospital);
            patientRepository.save(existingPatient);
            repository.save(findById(hospitalId));
            return existingPatient;
        } else {
            patient.getHospitals().add(hospital);
            Patient newPatient = patientRepository.save(patient);
            hospital.getActivePatients().add(newPatient);
            repository.save(hospital);
            return newPatient;
        }

    }

    @Override
    public List<Patient> getActivePatientsByHospitalId(UUID hospitalId) {
        Hospital hospital = findById(hospitalId);
        return hospital.getActivePatients();
    }

    @Override
    public List<Sample> getSamplesByHospitalId(UUID hospitalId) {
        Hospital hospital = findById(hospitalId);
        List<UUID> patientIds = hospital.getActivePatients().stream()
            .map(Patient::getId)
            .toList();
        return sampleRepository.findByPatientIdIn(patientIds);
    }

    private boolean isEmailRegistered(String email) {
        return userRepository.findByEmail(email).isPresent() || patientRepository.findByEmail(email).isPresent();
    }
}