# Pruebas Unitarias - Sistema Genético BioTrack

Este documento describe la implementación completa de pruebas unitarias para el sistema de gestión genética BioTrack.

## 📋 Estructura de Testing

### Dependencias de Testing
- **JUnit 5**: Framework principal de testing
- **Mockito**: Framework para mocking y stubbing
- **Spring Boot Test**: Integración con Spring Boot
- **Spring Security Test**: Testing de seguridad
- **TestContainers**: Pruebas de integración con base de datos real
- **H2 Database**: Base de datos en memoria para pruebas rápidas
- **AssertJ**: Librería de assertions más expresiva

### Configuraciones de Testing

#### application-test.properties
```properties
# Configuración de base de datos en memoria H2
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# Configuración JPA para testing
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=false

# JWT para testing
jwt.secret=testsecretkeythatislongenoughfortesting123456789012345678901234567890
jwt.expiration=86400000
```

## 🧪 Tipos de Pruebas Implementadas

### 1. Pruebas Unitarias de Controllers
- **UserControllerTest**: Testing completo del controlador de usuarios
- **PatientControllerTest**: Testing del controlador de pacientes  
- **HospitalControllerTest**: Testing del controlador de hospitales

#### Características:
- Testing de endpoints REST
- Validación de respuestas HTTP
- Testing de autenticación y autorización
- Validación de datos de entrada
- Manejo de errores y excepciones

### 2. Pruebas Unitarias de Services
- **UserServiceImplTest**: Testing de la lógica de negocio de usuarios
- **PatientServiceImplTest**: Testing de la lógica de pacientes

#### Características:
- Mocking de repositorios
- Testing de validaciones de negocio
- Testing de transformaciones de datos
- Manejo de excepciones
- Validación de flujos de trabajo

### 3. Pruebas de Integración
- **UserIntegrationTest**: Testing end-to-end con base de datos real

#### Características:
- Uso de TestContainers con PostgreSQL
- Testing de transacciones
- Validación de persistencia
- Testing de configuración completa

## 🚀 Ejecutar las Pruebas

### Todas las pruebas
```bash
./mvnw test
```

### Pruebas específicas por clase
```bash
# Testing de controllers
./mvnw test -Dtest=UserControllerTest
./mvnw test -Dtest=PatientControllerTest
./mvnw test -Dtest=HospitalControllerTest

# Testing de services  
./mvnw test -Dtest=UserServiceImplTest
./mvnw test -Dtest=PatientServiceImplTest

# Testing de integración
./mvnw test -Dtest=UserIntegrationTest
```

### Pruebas por paquete
```bash
# Todos los controllers
./mvnw test -Dtest="com.biotrack.backend.controllers.*Test"

# Todos los services
./mvnw test -Dtest="com.biotrack.backend.services.impl.*Test"

# Todas las pruebas de integración
./mvnw test -Dtest="com.biotrack.backend.integration.*Test"
```

## 📊 Cobertura de Testing

### Controllers Testeados
- ✅ UserController
- ✅ PatientController  
- ✅ HospitalController
- ⚠️ MedicationController (pendiente)
- ⚠️ SampleController (pendiente)
- ⚠️ InventoryController (pendiente)

### Services Testeados
- ✅ UserServiceImpl
- ✅ PatientServiceImpl
- ⚠️ HospitalServiceImpl (pendiente)
- ⚠️ MedicationServiceImpl (pendiente)
- ⚠️ SampleServiceImpl (pendiente)

### Casos de Prueba Cubiertos

#### Scenarios de Success (200/201)
- Creación exitosa de entidades
- Recuperación de datos
- Actualización de registros
- Eliminación de registros
- Búsquedas y filtros

#### Scenarios de Error (400/401/403/404/500)
- Validación de datos inválidos
- Autenticación requerida
- Autorización insuficiente
- Recursos no encontrados
- Errores del servidor

#### Security Testing
- Testing con roles específicos (@WithMockUser)
- Validación de endpoints protegidos
- Testing de CSRF protection
- Validación de autorización por rol

## 🛠️ Utilidades de Testing

### TestDataFactory
Clase utilitaria para crear objetos de prueba:
```java
// Crear usuarios de prueba
User testUser = TestDataFactory.createTestUser();
User adminUser = TestDataFactory.createAdminUser();
User medicUser = TestDataFactory.createMedicUser();

// Crear pacientes de prueba
Patient testPatient = TestDataFactory.createTestPatient();
Patient femalePatient = TestDataFactory.createFemalePatient();

// Crear hospitales de prueba
Hospital testHospital = TestDataFactory.createTestHospital();
```

### TestConfig
Configuración específica para testing con beans mockeados y configuraciones optimizadas.

## 📝 Convenciones de Testing

### Naming Convention
- Clases de test: `{ClaseOriginal}Test.java`
- Métodos de test: `{metodo}_{condicion}_Should{resultado}`

### Estructura de Métodos de Test
```java
@Test
void methodName_WithCondition_ShouldExpectedResult() {
    // Given - Preparación de datos
    
    // When - Ejecución del método a testear
    
    // Then - Validación de resultados
}
```

### Annotations Utilizadas
- `@WebMvcTest`: Para testing de controllers
- `@ExtendWith(MockitoExtension.class)`: Para testing con Mockito
- `@SpringBootTest`: Para testing de integración
- `@ActiveProfiles("test")`: Para usar configuración de test
- `@WithMockUser`: Para simular usuarios autenticados
- `@Transactional`: Para rollback automático en tests

## 🔧 Próximos Pasos

### Controllers Pendientes
- [ ] MedicationController
- [ ] SampleController  
- [ ] InventoryController (Medicine & Items)
- [ ] MedicalVisitController
- [ ] AuthController

### Services Pendientes
- [ ] HospitalServiceImpl
- [ ] MedicationServiceImpl
- [ ] SampleServiceImpl
- [ ] InventoryServices
- [ ] AuthService

### Mejoras Adicionales
- [ ] Testing de performance con JMeter
- [ ] Testing de carga con Gatling
- [ ] Mutation testing con PIT
- [ ] Cobertura de código con JaCoCo
- [ ] Testing de contratos con Pact

## 📋 Comandos Útiles

### Limpiar y compilar
```bash
./mvnw clean compile
```

### Generar reporte de cobertura
```bash
./mvnw jacoco:report
```

### Ejecutar solo tests rápidos (excluyendo integración)
```bash
./mvnw test -Dtest="!**/*IntegrationTest"
```

### Testing con perfil específico
```bash
./mvnw test -Dspring.profiles.active=test
```

## ✅ Checklist de Testing Completado

- [x] Configuración base de testing
- [x] Dependencias de testing añadidas
- [x] Configuración de propiedades de test
- [x] Clase TestConfig para beans de testing
- [x] Testing de UserController (completo)
- [x] Testing de UserService (completo)
- [x] Testing de PatientController (completo)  
- [x] Testing de PatientService (completo)
- [x] Testing de HospitalController (completo)
- [x] Pruebas de integración con TestContainers
- [x] Utilidades TestDataFactory
- [x] Documentación de testing

¡El sistema ahora cuenta con una suite completa de pruebas unitarias que garantiza la calidad y robustez del código!
