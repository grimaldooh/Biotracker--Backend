package com.biotrack.backend.dto.medical;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
public class MedicalReportSummaryDTO {
    
    @JsonProperty("reporteMedico")
    private MedicalReport reporteMedico;
    
    @Data
    @NoArgsConstructor
    public static class MedicalReport {
        private Patient paciente;
        private List<MedicalVisit> historialMedico;
        private List<StudyReport> reportesEstudiosRecientes;
        private Summary resumen;
        private List<String> recomendaciones;
        
        @Data
        @NoArgsConstructor
        public static class Patient {
            private String nombre;
            private String fechaNacimiento;
            private String curp;
        }
        
        @Data
        @NoArgsConstructor
        public static class MedicalVisit {
            private String fechaVisita;
            private String diagnostico;
            private List<String> recomendaciones;
            private String notas;
        }
        
        @Data
        @NoArgsConstructor
        public static class StudyReport {
            private String fechaEstudio;
            private String tipoMuestra;
            private String idMuestra;
            private String modeloAnalizador;
            private String hallazgosPrincipales;
        }
        
        @Data
        @NoArgsConstructor
        public static class Summary {
            private String texto;
            private List<String> enfermedadesDetectadas;
            private List<Evidence> evidenciaRespalda;
        }
        
        @Data
        @NoArgsConstructor
        public static class Evidence {
            private String enfermedad;
            private String idMuestraRespaldo;
            private String hallazgoEspecifico;
        }
    }
}