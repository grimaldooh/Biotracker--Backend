package com.biotrack.backend.services;

import com.biotrack.backend.models.Hospital;
import com.biotrack.backend.models.Patient;
import com.biotrack.backend.models.User;
import com.biotrack.backend.models.Sample;

import java.util.List;
import java.util.UUID;

public interface HospitalService {
    Hospital create(Hospital hospital);
    List<Hospital> findAll();
    Hospital findById(UUID id);
    void deleteById(UUID id);
    User registerUser(UUID hospitalId, User user);
    Patient registerPatient(UUID hospitalId, Patient patient);
    List<Patient> getActivePatientsByHospitalId(UUID hospitalId);
    List<Sample> getSamplesByHospitalId(UUID hospitalId);
    // Agrega métodos para las funciones avanzadas
}