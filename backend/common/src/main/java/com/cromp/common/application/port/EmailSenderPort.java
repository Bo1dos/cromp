package com.cromp.common.application.port;

import java.util.List;

public interface EmailSenderPort {
    void send(String to, String subject, String htmlBody);
    void sendBulk(List<String> to, String subject, String htmlBody);
}
