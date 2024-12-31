package com.springboot.notification.domain.notification;

import com.springboot.notification.api.notification.NotificationDto;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface MailgunEmailService {
    ResponseEntity<Map<String, String>> sendMailgunEmail(final NotificationDto.MailgunEmail notification);
}
