package com.cromp.orchestrator.exception;

import com.cromp.iam.domain.model.exceptions.DomainException;
import com.cromp.jobs.domain.model.exceptions.InvalidJobStateException;
import com.cromp.jobs.domain.model.exceptions.JobNotFoundException;
import com.cromp.schedules.domain.model.exceptions.ScheduleNotFoundException;
import com.cromp.secrets.domain.model.exceptions.SecretNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Nested
    @DisplayName("DomainException")
    class DomainExceptionHandling {

        @Test
        @DisplayName("should return 400 with message")
        void shouldReturn400ForDomainException() {
            var ex = new DomainException("Invalid domain operation");

            var response = handler.handleDomain(ex);

            assertThat(response.status()).isEqualTo(400);
            assertThat(response.error()).isEqualTo("Invalid domain operation");
            assertThat(response.timestamp()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Not Found exceptions")
    class NotFoundHandling {

        @Test
        @DisplayName("should return 404 for JobNotFoundException")
        void shouldReturn404ForJobNotFound() {
            var ex = new JobNotFoundException("Job not found: 123");

            var response = handler.handleNotFound(ex);

            assertThat(response.status()).isEqualTo(404);
            assertThat(response.error()).isEqualTo("Job not found: 123");
        }

        @Test
        @DisplayName("should return 404 for ScheduleNotFoundException")
        void shouldReturn404ForScheduleNotFound() {
            var ex = new ScheduleNotFoundException("Schedule not found");

            var response = handler.handleNotFound(ex);

            assertThat(response.status()).isEqualTo(404);
            assertThat(response.error()).isEqualTo("Schedule not found");
        }

        @Test
        @DisplayName("should return 404 for SecretNotFoundException")
        void shouldReturn404ForSecretNotFound() {
            var ex = new SecretNotFoundException("Secret not found");

            var response = handler.handleNotFound(ex);

            assertThat(response.status()).isEqualTo(404);
            assertThat(response.error()).isEqualTo("Secret not found");
        }
    }

    @Nested
    @DisplayName("InvalidJobStateException")
    class ConflictHandling {

        @Test
        @DisplayName("should return 409 with message")
        void shouldReturn409ForInvalidJobState() {
            var ex = new InvalidJobStateException("Job is already disabled");

            var response = handler.handleConflict(ex);

            assertThat(response.status()).isEqualTo(409);
            assertThat(response.error()).isEqualTo("Job is already disabled");
        }
    }

    @Nested
    @DisplayName("SecurityException")
    class SecurityHandling {

        @Test
        @DisplayName("should return 403 with message")
        void shouldReturn403ForSecurityException() {
            var ex = new SecurityException("Access denied");

            var response = handler.handleSecurity(ex);

            assertThat(response.status()).isEqualTo(403);
            assertThat(response.error()).isEqualTo("Access denied");
        }
    }

    @Nested
    @DisplayName("MethodArgumentNotValidException")
    class ValidationHandling {

        @Test
        @DisplayName("should return 422 with field error message")
        void shouldReturn422WithFieldError() {
            var bindingResult = new BeanPropertyBindingResult(new Object(), "target");
            bindingResult.addError(new FieldError("target", "name", "must not be blank"));
            var ex = new MethodArgumentNotValidException(null, bindingResult);

            var response = handler.handleValidation(ex);

            assertThat(response.status()).isEqualTo(422);
            assertThat(response.error()).isEqualTo("name: must not be blank");
            assertThat(response.timestamp()).isNotNull();
        }

        @Test
        @DisplayName("should return 422 with fallback message when no field errors")
        void shouldReturn422WithFallbackMessage() {
            var bindingResult = new BeanPropertyBindingResult(new Object(), "target");
            var ex = new MethodArgumentNotValidException(null, bindingResult);

            var response = handler.handleValidation(ex);

            assertThat(response.status()).isEqualTo(422);
            assertThat(response.error()).isEqualTo("Validation failed");
        }

        @Test
        @DisplayName("should include first field error only")
        void shouldIncludeFirstFieldErrorOnly() {
            var bindingResult = new BeanPropertyBindingResult(new Object(), "target");
            bindingResult.addError(new FieldError("target", "email", "invalid format"));
            bindingResult.addError(new FieldError("target", "name", "must not be blank"));
            var ex = new MethodArgumentNotValidException(null, bindingResult);

            var response = handler.handleValidation(ex);

            assertThat(response.error()).isEqualTo("email: invalid format");
        }
    }

    @Nested
    @DisplayName("Generic Exception")
    class GenericHandling {

        @Test
        @DisplayName("should return 500 with internal error message")
        void shouldReturn500ForGenericException() {
            var ex = new RuntimeException("Something went wrong");

            var response = handler.handleGeneric(ex);

            assertThat(response.status()).isEqualTo(500);
            assertThat(response.error()).isEqualTo("Internal error");
            assertThat(response.timestamp()).isNotNull();
        }
    }
}
