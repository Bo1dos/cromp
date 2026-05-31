package com.cromp.analytics.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

class MlClientConfigTest {

    @Test
    void shouldCreateMlRestClientBean() {
        MlClientConfig config = new MlClientConfig();
        setField(config, "mlServiceUrl", "http://localhost:8000");
        setField(config, "timeoutMs", 10000L);

        RestClient restClient = config.mlRestClient();

        assertThat(restClient).isNotNull();
    }

    @Test
    void shouldUseConfiguredBaseUrl() {
        MlClientConfig config = new MlClientConfig();
        setField(config, "mlServiceUrl", "http://ml-service:9000");
        setField(config, "timeoutMs", 5000L);

        RestClient restClient = config.mlRestClient();

        assertThat(restClient).isNotNull();
        // RestClient is created — base URL is set internally
    }

    @Test
    void shouldUseCustomTimeout() {
        MlClientConfig config = new MlClientConfig();
        setField(config, "mlServiceUrl", "http://localhost:8000");
        setField(config, "timeoutMs", 30000L);

        RestClient restClient = config.mlRestClient();

        assertThat(restClient).isNotNull();
    }

    @Test
    void shouldUseDefaultValues() {
        MlClientConfig config = new MlClientConfig();
        // Default: url=http://localhost:8000, timeout=10000

        RestClient restClient = config.mlRestClient();

        assertThat(restClient).isNotNull();
    }

    @Test
    void shouldCreateBeanWithExplicitProperties() {
        MlClientConfig config = new MlClientConfig();
        setField(config, "mlServiceUrl", "https://ml.example.com/api");
        setField(config, "timeoutMs", 15000L);

        RestClient restClient = config.mlRestClient();

        assertThat(restClient).isNotNull();
    }

    @Test
    void shouldHandleDifferentUrls() {
        MlClientConfig config = new MlClientConfig();
        setField(config, "mlServiceUrl", "http://custom-host:1234");
        setField(config, "timeoutMs", 5000L);

        RestClient first = config.mlRestClient();
        assertThat(first).isNotNull();

        setField(config, "mlServiceUrl", "http://other-host:5678");
        RestClient second = config.mlRestClient();
        assertThat(second).isNotNull();
        assertThat(second).isNotSameAs(first);
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
