package com.springboot.notification.api.notification;

import com.springboot.notification.domain.notification.MailgunEmailService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final MailgunEmailService mailgunEmailService;

    @PostMapping("/email")
    public ResponseEntity<Map<String, String>> sendMailgunEmail(@RequestBody NotificationDto.MailgunEmail notification) {
        return mailgunEmailService.sendMailgunEmail(
            notification.getTo(),
            notification.getSubject(),
            notification.getType_user(),
            notification.getData_inscription()
        );
    }
}