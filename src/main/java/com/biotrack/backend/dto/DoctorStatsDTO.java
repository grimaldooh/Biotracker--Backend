package com.biotrack.backend.dto;

public record DoctorStatsDTO(
    int totalPatients,
    int todayAppointments,
    int upcomingAppointments,
    int completedAppointments
) {}