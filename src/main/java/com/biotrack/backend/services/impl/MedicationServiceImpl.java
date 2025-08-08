package com.biotrack.backend.services.impl;

import com.biotrack.backend.dto.MedicationAnalysisDTO;
import com.biotrack.backend.dto.MedicationOperationDTO;
import com.biotrack.backend.dto.MedicationPatchDTO;
import com.biotrack.backend.dto.MedicationResponseDTO;
import com.biotrack.backend.models.Medication;
import com.biotrack.backend.models.Patient;
import com.biotrack.backend.models.User;
import com.biotrack.backend.repositories.MedicationRepository;
import com.biotrack.backend.services.MedicationService;
import com.biotrack.backend.services.OpenAIService;
import com.biotrack.backend.services.PatientService;
import com.biotrack.backend.services.S3Service;
import com.biotrack.backend.services.UserService;
import com.biotrack.backend.utils.MedicationMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class MedicationServiceImpl implements MedicationService {

    private final MedicationRepository repository;
    private final PatientService patientService;
    private final UserService userService;
    private final OpenAIService openAIService; // ✅ AGREGAR
    private final S3Service s3Service; // ✅ AGREGAR

    public MedicationServiceImpl(MedicationRepository repository, 
                               PatientService patientService,
                               UserService userService,
                               OpenAIService openAIService, // ✅ AGREGAR
                               S3Service s3Service) { // ✅ AGREGAR
        this.repository = repository;
        this.patientService = patientService;
        this.userService = userService;
        this.openAIService = openAIService; // ✅ AGREGAR
        this.s3Service = s3Service; // ✅ AGREGAR
    }

    @Override
    public Medication create(Medication medication) {
        return repository.save(medication);
    }

    @Override
    public List<Medication> findAll() {
        return repository.findAll();
    }

    @Override
    public Medication findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medication not found"));
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    @Override
    public List<Medication> findByPatientId(UUID patientId) {
        return repository.findByPatientId(patientId);
    }

    @Override
    public List<Medication> findByPrescribedById(UUID userId) {
        return repository.findByPrescribedById(userId);
    }

    @Override
    @Transactional
    public List<MedicationResponseDTO> patchPatientMedications(UUID patientId, MedicationPatchDTO patchDTO) {
        // Validar que el paciente existe
        Patient patient = patientService.findById(patientId);

        List<String> errors = new ArrayList<>();

        // Procesar cada operación
        for (MedicationOperationDTO operation : patchDTO.operations()) {
            try {
                if (operation.isAddOperation()) {
                    processAddOperation(patient, operation);
                } else if (operation.isRemoveOperation()) {
                    processRemoveOperation(operation);
                } else {
                    errors.add("Operación no válida: " + operation.operation() +
                              ". Solo se permiten 'ADD' o 'REMOVE'");
                }
            } catch (Exception e) {
                errors.add("Error en operación " + operation.operation() + ": " + e.getMessage());
            }
        }

        // Si hay errores, lanzar excepción
        if (!errors.isEmpty()) {
            throw new RuntimeException("Errores en operaciones PATCH: " + String.join("; ", errors));
        }

        // Retornar la lista actualizada
        return getPatientMedicationsAsDTO(patientId);
    }

    @Override
    public List<MedicationResponseDTO> getPatientMedicationsAsDTO(UUID patientId) {
        List<Medication> medications = findByPatientId(patientId);
        return medications.stream()
                .map(MedicationMapper::toResponseDTO)
                .toList();
    }

    // ✅ NUEVO: Procesar operación ADD
    private void processAddOperation(Patient patient, MedicationOperationDTO operation) {
        // Validar datos para ADD
        if (!operation.isValidForAdd()) {
            throw new RuntimeException("Datos incompletos para agregar medicamento. " +
                                     "Se requieren: name, dosage, prescribedById");
        }

        // Obtener el usuario que prescribe
        User prescribedBy = userService.getUserById(operation.prescribedById());

        // Crear nuevo medicamento
        Medication newMedication = Medication.builder()
                .name(operation.name())
                .brand(operation.brand())
                .activeSubstance(operation.activeSubstance())
                .indication(operation.indication())
                .dosage(operation.dosage())
                .frequency(operation.frequency())
                .startDate(operation.startDate())
                .endDate(operation.endDate())
                .prescribedBy(prescribedBy)
                .patient(patient)
                .build();

        repository.save(newMedication);
    }

    // ✅ NUEVO: Procesar operación REMOVE
    private void processRemoveOperation(MedicationOperationDTO operation) {
        // Validar datos para REMOVE
        if (!operation.isValidForRemove()) {
            throw new RuntimeException("medicationId es requerido para remover medicamento");
        }

        // Verificar que el medicamento existe
        Medication medication = findById(operation.medicationId());

        // Eliminar medicamento
        repository.delete(medication);
    }

    @Override
    @Transactional
    public String generateCompatibilityReport(UUID patientId, List<MedicationAnalysisDTO> medications) {
        // 1. Validar que el paciente existe
        Patient patient = patientService.findById(patientId);
        
        // 2. Validar que OpenAI está configurado
        if (!openAIService.isConfigured()) {
            throw new RuntimeException("OpenAI service is not configured. Please check API key configuration.");
        }
        
        // 3. Validar que hay medicamentos para analizar
        if (medications == null || medications.isEmpty()) {
            throw new RuntimeException("No medications provided for analysis.");
        }
        
        try {
            // 4. Obtener contexto clínico del paciente
            String clinicalContext;
            try {
                clinicalContext = patientService.getLatestSummaryText(patientId);
                if (clinicalContext == null || clinicalContext.trim().isEmpty()) {
                    clinicalContext = "No clinical history available for this patient.";
                }
            } catch (Exception e) {
                // Si no hay contexto clínico, continuar sin él
                clinicalContext = "Clinical history could not be retrieved. Analysis based solely on medication interactions.";
            }
            
            // 5. Generar reporte de compatibilidad con OpenAI
            String reportContent = openAIService.generateMedicationCompatibilityReport(medications, clinicalContext);
            
            // 6. OPCIONAL: Guardar en S3 para respaldo/auditoría (sin bloquear la respuesta)
            try {
                String s3Key = generateCompatibilityReportS3Key(patientId);
                s3Service.uploadTextContent(reportContent, s3Key);
                // Log successful backup but don't block response
                System.out.println("Medication compatibility report backed up to S3: " + s3Key);
            } catch (Exception s3Exception) {
                // Log error but don't fail the main operation
                System.err.println("Warning: Could not backup report to S3: " + s3Exception.getMessage());
            }
            
            // 7. Retornar directamente el contenido del reporte
            return reportContent;
            
        } catch (Exception e) {
            throw new RuntimeException("Error generating medication compatibility report: " + e.getMessage(), e);
        }
    }
    
    // ✅ NUEVO: Método para generar clave S3 para reportes de compatibilidad
    private String generateCompatibilityReportS3Key(UUID patientId) {
        long timestamp = System.currentTimeMillis();
        return String.format("medication-compatibility-reports/%s_%d_compatibility_report.json", 
                            patientId.toString(), timestamp);
    }
}