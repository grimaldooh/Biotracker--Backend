package com.biotrack.backend.services.impl;

import com.biotrack.backend.models.Patient;
import com.biotrack.backend.repositories.PatientRepository;
import com.biotrack.backend.services.PatientService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;

    public PatientServiceImpl(PatientRepository patientRepository){
        this.patientRepository = patientRepository;
    }

    @Override
    public Patient create(Patient patient){
        return patientRepository.save(patient);
    }

    @Override
    public List<Patient> findAll(){
        return patientRepository.findAll();
    }

    @Override
    public Patient findById(UUID id){
        return patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
    }

    @Override
    public Patient update(UUID id , Patient patient){
        Patient existing = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        existing.setFirstName(patient.getFirstName());
        existing.setLastName(patient.getLastName());
        existing.setGender(patient.getGender());
        existing.setBirthDate(patient.getBirthDate());
        existing.setCurp(patient.getCurp());
        return patientRepository.save(existing);
    }


    @Override
    public void deleteById(UUID id){
        patientRepository.deleteById(id);
    }
}
