package com.biotrack.backend.services;

import com.biotrack.backend.models.ClinicalHistoryRecord;
import com.biotrack.backend.models.Patient;

import java.util.List;
import java.util.UUID;

public interface PatientService {
    Patient create(Patient patient);
    List<Patient> findAll();
    Patient findById(UUID id);
    Patient update(UUID id, Patient updatedPatient);
    void deleteById(UUID id);
    ClinicalHistoryRecord generatePatientClinicalSummary(UUID patientId);
    ClinicalHistoryRecord getLatestRecord(UUID patientId);
    List<Patient> searchPatients(String firstName, String lastName);
}
