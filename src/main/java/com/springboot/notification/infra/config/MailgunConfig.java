package com.springboot.notification.infra.config;

import com.mailgun.api.v3.MailgunMessagesApi;
import com.mailgun.client.MailgunClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class MailgunConfig {

    @Value("${mailgun.api.key}")
    private String apiKey;

    @Value("${mailgun.domain}")
    private String domain;

    @Bean
    public MailgunMessagesApi mailgunMessagesApi() {
        return MailgunClient.config(apiKey)
                .createApi(MailgunMessagesApi.class);
    }

    @Bean
    public String mailgunDomain() {
        return domain;
    }
}