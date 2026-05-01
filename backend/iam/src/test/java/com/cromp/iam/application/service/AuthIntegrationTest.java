package com.cromp.iam.application.service;

import com.cromp.iam.api.dto.request.LoginRequest;
import com.cromp.iam.api.dto.request.RegisterRequest;
import com.cromp.iam.api.dto.request.SelectOrganizationRequest;
import com.cromp.iam.api.dto.response.AuthResponse;
import com.cromp.iam.api.dto.response.UserResponse;
import com.cromp.iam.domain.model.Role;
import com.cromp.iam.domain.model.enums.UserRole;
import com.cromp.iam.domain.repository.RoleRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class AuthIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @LocalServerPort
    private int port;

    @Autowired
    private RoleRepositoryPort roleRepository;

    private RestTemplate restTemplate = new RestTemplate();

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        // Liquibase/Flyway запустятся автоматически, если они есть в classpath
    }

    @BeforeEach
    void setUp() {
        // Убедимся, что роль OWNER существует в БД
        if (roleRepository.findByName(UserRole.OWNER).isEmpty()) {
            roleRepository.save(Role.of(UserRole.OWNER, "Organization owner"));
        }
    }

    private String baseUrl() {
        return "http://localhost:" + port;
    }

    @Test
    void shouldRegisterLoginSelectOrganizationAndAccessProtectedEndpoint() {
        // 1. Регистрация
        RegisterRequest regReq = new RegisterRequest(
                "new@example.com", "password123", "NewOrg",
                "John", "Doe", null, "John Doe"
        );
        ResponseEntity<AuthResponse> regResp = restTemplate.postForEntity(
                baseUrl() + "/api/v1/auth/register", regReq, AuthResponse.class
        );
        assertThat(regResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        AuthResponse regBody = regResp.getBody();
        assertThat(regBody.accessToken()).isNotBlank();
        assertThat(regBody.activeOrganization()).isNotNull();
        Long orgId = regBody.activeOrganization().id();
        String tokenAfterReg = regBody.accessToken();

        // 2. Логин
        LoginRequest loginReq = new LoginRequest("new@example.com", "password123");
        ResponseEntity<AuthResponse> loginResp = restTemplate.postForEntity(
                baseUrl() + "/api/v1/auth/login", loginReq, AuthResponse.class
        );
        assertThat(loginResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        AuthResponse loginBody = loginResp.getBody();
        assertThat(loginBody.organizations()).hasSize(1);
        // Токен без организации
        String tokenBeforeSelect = loginBody.accessToken();

        // 3. Выбор организации (с токеном из логина)
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenBeforeSelect);
        SelectOrganizationRequest selReq = new SelectOrganizationRequest(orgId);
        HttpEntity<SelectOrganizationRequest> entity = new HttpEntity<>(selReq, headers);
        ResponseEntity<AuthResponse> selResp = restTemplate.postForEntity(
                baseUrl() + "/api/v1/auth/select-organization", entity, AuthResponse.class
        );
        assertThat(selResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        AuthResponse selBody = selResp.getBody();
        assertThat(selBody.activeOrganization()).isNotNull();
        String tokenWithOrg = selBody.accessToken();

        // 4. Доступ к защищённому эндпоинту (получение профиля)
        headers = new HttpHeaders();
        headers.setBearerAuth(tokenWithOrg);
        HttpEntity<Void> profileEntity = new HttpEntity<>(headers);
        ResponseEntity<UserResponse> userResp = restTemplate.exchange(
                baseUrl() + "/api/v1/users/" + regBody.user().id(),
                HttpMethod.GET,
                profileEntity,
                UserResponse.class
        );
        assertThat(userResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(userResp.getBody().email()).isEqualTo("new@example.com");

        // 5. Попытка доступа к защищённому эндпоинту без токена должна вернуть 401/403
        ResponseEntity<String> unauthorized = restTemplate.getForEntity(
                baseUrl() + "/api/v1/users/1", String.class
        );
        assertThat(unauthorized.getStatusCode()).isIn(HttpStatus.UNAUTHORIZED, HttpStatus.FORBIDDEN);
    }
}