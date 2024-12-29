package com.springboot.notification.api.notification;

import lombok.*;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class NotificationDto {

    @Getter
    @Setter
    @AllArgsConstructor
    @Builder
    public static class MailgunEmail {
        @NotNull
        private String to;
        @NotNull
        private String subject;
        @NotNull
        private String type_user;
        @NotNull
        private Map<String, Object> data_inscription;
    }
}
