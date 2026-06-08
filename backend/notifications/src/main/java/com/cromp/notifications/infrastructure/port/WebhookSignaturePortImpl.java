package com.cromp.notifications.infrastructure.port;

import com.cromp.notifications.application.port.WebhookSignaturePort;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;

@Component
public class WebhookSignaturePortImpl implements WebhookSignaturePort {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final long TOLERANCE_SECONDS = 300;

    @Override
    public String sign(String secret, String payload) {
        long timestamp = Instant.now().getEpochSecond();
        String signedPayload = timestamp + "." + payload;
        String hmac = computeHmac(secret, signedPayload);
        return "t=" + timestamp + ",v1=" + hmac;
    }

    @Override
    public boolean verify(String secret, String payload, String signature) {
        try {
            String[] parts = signature.split(",");
            long timestamp = Long.parseLong(parts[0].substring(2));
            String hmac = parts[1].substring(3);

            long now = Instant.now().getEpochSecond();
            if (Math.abs(now - timestamp) > TOLERANCE_SECONDS) {
                return false;
            }

            String expectedHmac = computeHmac(secret, timestamp + "." + payload);
            return hmac.equals(expectedHmac);
        } catch (Exception e) {
            return false;
        }
    }

    private String computeHmac(String secret, String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
            mac.init(keySpec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("HMAC computation failed", e);
        }
    }
}
