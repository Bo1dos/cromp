package com.cromp.notifications.infrastructure.port;

import com.cromp.notifications.application.port.WebhookDispatcherPort;
import com.cromp.notifications.application.port.WebhookSignaturePort;
import com.cromp.notifications.domain.model.WebhookDelivery;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class WebhookDispatcherPortImpl implements WebhookDispatcherPort {

    private final RestClient restClient;
    private final WebhookSignaturePort signaturePort;

    public WebhookDispatcherPortImpl(RestClient.Builder restClientBuilder, WebhookSignaturePort signaturePort) {
        this.restClient = restClientBuilder.build();
        this.signaturePort = signaturePort;
    }

    @Override
    public WebhookDelivery dispatch(String url, String secret, String payloadJson) {
        String signature = signaturePort.sign(secret, payloadJson);
        WebhookDelivery delivery = WebhookDelivery.createPending(0L, null, url);

        try {
            var response = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-Cromp-Signature", signature)
                    .body(payloadJson)
                    .retrieve()
                    .toBodilessEntity();

            delivery.markSucceeded(response.getStatusCode().value());
        } catch (Exception e) {
            delivery.markFailed(0, e.getMessage());
        }

        return delivery;
    }
}
