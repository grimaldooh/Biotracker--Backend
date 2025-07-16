package com.biotrack.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test") // Usar el perfil de pruebas
class BackendApplicationTests {

    @Test
    void contextLoads() {
        // Esta prueba solo verifica que el contexto de Spring se carga correctamente
    }
}
