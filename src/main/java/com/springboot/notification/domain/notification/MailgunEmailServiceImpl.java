package com.springboot.notification.domain.notification;

import com.springboot.notification.api.notification.NotificationDto;
import com.mailgun.api.v3.MailgunMessagesApi;
import com.mailgun.model.message.Message;
import com.mailgun.model.message.MessageResponse;
import com.springboot.notification.domain.exception.NotificationException;
import com.springboot.notification.domain.exception.Error;
import feign.FeignException;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class MailgunEmailServiceImpl implements MailgunEmailService {
    private final MailgunMessagesApi mailgunMessagesApi;
    private final String domain;
    private final SpringTemplateEngine templateEngine;

    @Override
    public ResponseEntity<Map<String, String>> sendMailgunEmail(NotificationDto.MailgunEmail notification) {
        String htmlContent = getEmailTemplate(notification.getTemplate(), notification.getDataSubscription());
        
        Message message = Message.builder()
                .from("VitalTrail <vitaltrail@" + domain + ">")
                .to(notification.getTo())
                .subject(notification.getSubject())
                .html(htmlContent)
                .build();
        try {
            MessageResponse response = mailgunMessagesApi.sendMessage(domain, message);

            Map<String, String> okResponse = Map.of(
                "message", "Email enviado con id: " + response.getId());
            return ResponseEntity.ok(okResponse);

        } catch (FeignException.BadRequest e) {
            throw new NotificationException(Error.INVALID_REQUEST);
        } catch (FeignException.Unauthorized e) {
            throw new NotificationException(Error.UNAUTHORIZED);
        } catch (FeignException.Forbidden e) {
            throw new NotificationException(Error.FORBIDDEN);
        } catch (FeignException.NotFound e) {
            throw new NotificationException(Error.ENDPOINT_NOT_FOUND);
        } catch (FeignException e) {
            throw new NotificationException(Error.SERVICE_UNAVAILABLE);
        } catch (RuntimeException e) {
            throw new NotificationException(Error.INVALID_REQUEST);
        } catch (Exception e) {
            throw new NotificationException(Error.INTERNAL_SERVER_ERROR);
        }
    }

    private String getEmailTemplate(String template, Map<String, Object> data) {
        Context context = new Context();
        context.setVariables(data);

        return templateEngine.process(template.toLowerCase(), context);
    }
}
