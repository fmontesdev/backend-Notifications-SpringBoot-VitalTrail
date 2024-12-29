package com.springboot.notification.domain.notification;

import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface MailgunEmailService {
    ResponseEntity<Map<String, String>> sendMailgunEmail(
        final String to,
        final String subject,
        final String type_user,
        final Map<String, Object> data_inscription
    );
}
