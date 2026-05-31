package com.cromp.orchestrator.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RestClientConfig")
class RestClientConfigTest {

    @Test
    @DisplayName("should create orchestratorRestClient bean with valid properties")
    void shouldCreateRestClientBean() {
        OrchestratorProperties props = new OrchestratorProperties();
        props.getExecutor().setHttpTimeoutMs(15_000);
        RestClientConfig config = new RestClientConfig(props);

        RestClient restClient = config.orchestratorRestClient();

        assertThat(restClient).isNotNull();
    }

    @Test
    @DisplayName("should create bean with default timeout from properties")
    void shouldUseDefaultTimeout() {
        OrchestratorProperties props = new OrchestratorProperties();
        RestClientConfig config = new RestClientConfig(props);

        RestClient restClient = config.orchestratorRestClient();

        assertThat(restClient).isNotNull();
        // RestClient is created — factory configured internally
    }

    @Test
    @DisplayName("should create bean with custom timeout")
    void shouldCreateWithCustomTimeout() {
        OrchestratorProperties props = new OrchestratorProperties();
        props.getExecutor().setHttpTimeoutMs(60_000);
        RestClientConfig config = new RestClientConfig(props);

        RestClient restClient = config.orchestratorRestClient();

        assertThat(restClient).isNotNull();
    }
}
