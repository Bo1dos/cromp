package com.cromp.notifications.infrastructure.port;

import com.cromp.common.application.port.EmailSenderPort;
import com.cromp.notifications.infrastructure.email.EmailProperties;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Properties;

@Component
public class EmailSenderPortImpl implements EmailSenderPort {

    private final JavaMailSender mailSender;
    private final EmailProperties emailProperties;

    public EmailSenderPortImpl(EmailProperties emailProperties) {
        this.emailProperties = emailProperties;
        this.mailSender = createMailSender();
    }

    private JavaMailSender createMailSender() {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(emailProperties.getHost());
        sender.setPort(emailProperties.getPort());
        sender.setUsername(emailProperties.getUsername());
        sender.setPassword(emailProperties.getPassword());

        Properties props = sender.getJavaMailProperties();
        props.put("mail.smtp.auth", String.valueOf(emailProperties.isAuth()));
        props.put("mail.smtp.starttls.enable", String.valueOf(emailProperties.isStarttls()));
        return sender;
    }

    @Override
    public void send(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(emailProperties.getFrom());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email to " + to, e);
        }
    }

    @Override
    public void sendBulk(List<String> to, String subject, String htmlBody) {
        for (String recipient : to) {
            send(recipient, subject, htmlBody);
        }
    }
}
