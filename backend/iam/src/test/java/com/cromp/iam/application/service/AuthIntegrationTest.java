package com.cromp.iam.application.service;

import com.cromp.iam.api.dto.request.LoginRequest;
import com.cromp.iam.api.dto.request.RegisterRequest;
import com.cromp.iam.api.dto.request.SelectOrganizationRequest;
import com.cromp.iam.api.dto.response.AuthResponse;
import com.cromp.iam.domain.model.Role;
import com.cromp.iam.domain.model.enums.UserRole;
import com.cromp.iam.domain.repository.RoleRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.http.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = AuthIntegrationTest.TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
public class AuthIntegrationTest {

    @SpringBootApplication(scanBasePackages = "com.cromp.iam")
    @EnableJpaRepositories(basePackages = "com.cromp.iam.infrastructure.persistence.jpa.repository")
    @EntityScan(basePackages = "com.cromp.iam.infrastructure.persistence.jpa.entity")
    static class TestApplication {
    }

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
        // Liquibase/Flyway Р·Р°РїСѓСЃС‚СЏС‚СЃСЏ Р°РІС‚РѕРјР°С‚РёС‡РµСЃРєРё, РµСЃР»Рё РѕРЅРё РµСЃС‚СЊ РІ classpath
    }

    @BeforeEach
    void setUp() {
        // РЈР±РµРґРёРјСЃСЏ, С‡С‚Рѕ СЂРѕР»СЊ OWNER СЃСѓС‰РµСЃС‚РІСѓРµС‚ РІ Р‘Р”
        if (roleRepository.findByName(UserRole.OWNER).isEmpty()) {
            roleRepository.save(Role.of(UserRole.OWNER, "Organization owner"));
        }
    }

    private String baseUrl() {
        return "http://localhost:" + port;
    }

    @Test
    void shouldRegisterLoginSelectOrganizationAndAccessProtectedEndpoint() {
        // 1. Р РµРіРёСЃС‚СЂР°С†РёСЏ
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

        // 2. Р›РѕРіРёРЅ
        LoginRequest loginReq = new LoginRequest("new@example.com", "password123");
        ResponseEntity<AuthResponse> loginResp = restTemplate.postForEntity(
                baseUrl() + "/api/v1/auth/login", loginReq, AuthResponse.class
        );
        assertThat(loginResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        AuthResponse loginBody = loginResp.getBody();
        assertThat(loginBody.organizations()).hasSize(1);
        // РўРѕРєРµРЅ Р±РµР· РѕСЂРіР°РЅРёР·Р°С†РёРё
        String tokenBeforeSelect = loginBody.accessToken();

        // 3. Р’С‹Р±РѕСЂ РѕСЂРіР°РЅРёР·Р°С†РёРё (СЃ С‚РѕРєРµРЅРѕРј РёР· Р»РѕРіРёРЅР°)
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
        assertThat(selBody.accessToken()).isNotBlank();
    }
}
