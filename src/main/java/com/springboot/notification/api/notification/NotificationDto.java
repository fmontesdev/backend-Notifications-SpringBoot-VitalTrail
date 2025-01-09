package com.springboot.notification.api.notification;

import lombok.*;
import jakarta.validation.constraints.NotNull;
// import io.micrometer.common.lang.Nullable;
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

    // @Getter
    // @Setter
    // @Builder
    // @AllArgsConstructor
    // @NoArgsConstructor
    // public static class DataInscription {
    //     private String name_client;
    //     private String surname_client;
    //     private String date;
    //     private String n_activity;
    //     private String slot_hour;
    //     private String slug_inscription;
    //     private String name_instructor;
    //     private String surname_instructor;
    //     @Nullable
    //     private String error;
    // }
}
