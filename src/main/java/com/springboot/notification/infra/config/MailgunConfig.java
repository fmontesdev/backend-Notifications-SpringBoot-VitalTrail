package com.springboot.notification.infra.config;

import com.mailgun.api.v3.MailgunMessagesApi;
import com.mailgun.client.MailgunClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.AllArgsConstructor;

@Configuration
@AllArgsConstructor
public class MailgunConfig {
    private final Dotenv dotenv;

    @Bean
    public MailgunMessagesApi mailgunMessagesApi() {
        return MailgunClient.config(dotenv.get("MAILGUN_API_KEY"))
                .createApi(MailgunMessagesApi.class);
    }

    @Bean
    public String mailgunDomain() {
        return dotenv.get("MAILGUN_DOMAIN");
    }
}