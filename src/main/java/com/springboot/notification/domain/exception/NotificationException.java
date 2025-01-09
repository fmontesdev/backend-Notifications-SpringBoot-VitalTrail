package com.springboot.notification.domain.exception;

import lombok.Getter;

@Getter
public class NotificationException extends RuntimeException {
    private final Error error;
    
    public NotificationException(Error error) {
        super(error.getMessage());
        this.error = error;
    }

    public NotificationException(Error error, Throwable cause) {
        super(error.getMessage(), cause);
        this.error = error;
    }
}
