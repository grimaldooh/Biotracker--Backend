package com.biotrack.backend.models;

import com.biotrack.backend.models.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "hospitals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hospital {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String name;

    

    private String location;
    private String fullAddress;
    private String legalIdentifier; // RFC, NIT, etc.

    @ManyToMany
    @JoinTable(
        name = "hospital_users",
        joinColumns = @JoinColumn(name = "hospital_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> authorizedUsers;

    @ManyToMany
    @JoinTable(
        name = "hospital_patients",
        joinColumns = @JoinColumn(name = "hospital_id"),
        inverseJoinColumns = @JoinColumn(name = "patient_id")
    )
    private List<Patient> activePatients;

    private String contactPhone;
    private String contactEmail;
    private String website;

    // Example management methods
    public void registerUser(User user) {
        authorizedUsers.add(user);
    }

    public void removeUser(UUID userId) {
        authorizedUsers.removeIf(u -> u.getId().equals(userId));
    }

    public void updateContactInfo(String phone, String email, String website) {
        this.contactPhone = phone;
        this.contactEmail = email;
        this.website = website;
    }

    public void assignRole(UUID userId, Role role) {
        authorizedUsers.stream()
            .filter(u -> u.getId().equals(userId))
            .findFirst()
            .ifPresent(u -> u.setRole(role));
    }

    public List<Patient> getActivePatients() {
        return activePatients;
    }

    public List<User> getAuthorizedUsers() {
        return authorizedUsers;
    }

    public String getNombre() { return name; }


    public String getUbicacion() { return location; }

    public String getDireccionCompleta() { return fullAddress; }

    public String getIdentificadorLegal() { return legalIdentifier; }

    public List<User> getUsuariosAutorizados() { return authorizedUsers; }

    public List<Patient> getPacientesActivos() { return activePatients; }

    // Implementa los métodos de gestión según tu lógica de negocio
    // Ejemplo:
    public void registrarUsuario(User usuario) { authorizedUsers.add(usuario); }

    // @Override
    // public void eliminarUsuario(UUID userId) { authorizedUsers.removeIf(u -> u.getId().equals(userId)); }

    // @Override
    // public void actualizarInformacionContacto(String phone, String email, String website) {
    //     this.contactPhone = phone;
    //     this.contactEmail = email;
    //     this.website = website;
    // }

    // @Override
    // public void asignarPermisos(UUID userId, Role role) {
    //     authorizedUsers.stream()
    //         .filter(u -> u.getId().equals(userId))
    //         .findFirst()
    //         .ifPresent(u -> u.setRole(role));
    // }

    // @Override
    // public ActivityMetrics obtenerMetricasActividad() { /* Implementa tu lógica */ return null; }

    // @Override
    // public void agendarCita(Patient paciente, java.util.Date fecha) { /* Implementa tu lógica */ }

    public List<MedicalVisit> verHistorialMedico(UUID patientId) { /* Implementa tu lógica */ return null; }

    // @Override
    // public List<Report> listarReportes() { /* Implementa tu lógica */ return null; }

    // @Override
    // public void registrarExamenSangre(BloodSample test) { /* Implementa tu lógica */ }

    // @Override
    // public void subirDocumento(UUID patientId, MedicalDocument doc) { /* Implementa tu lógica */ }

    // @Override
    // public List<Medication> listarInventarioMedicamentos() { /* Implementa tu lógica */ return null; }

    // @Override
    // public void actualizarStockMedicamento(UUID medId, int cantidad) { /* Implementa tu lógica */ }

    // @Override
    // public void registrarVenta(UUID patientId, List<Medication> items) { /* Implementa tu lógica */ }

    // @Override
    // public ConsumptionReport generarReporteConsumo() { /* Implementa tu lógica */ return null; }
}