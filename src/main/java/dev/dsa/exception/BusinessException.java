package dev.dsa.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException {
    private final HttpStatus status;
    private final String userMessage;

    public BusinessException(String userMessage) {
        super(userMessage);
        this.userMessage = userMessage;
        this.status = HttpStatus.BAD_REQUEST;
    }

    public BusinessException(String userMessage, HttpStatus status) {
        super(userMessage);
        this.userMessage = userMessage;
        this.status = status;
    }

    public BusinessException(String userMessage, Throwable cause) {
        super(userMessage, cause);
        this.userMessage = userMessage;
        this.status = HttpStatus.BAD_REQUEST;
    }

    public BusinessException(String userMessage, HttpStatus status, Throwable cause) {
        super(userMessage, cause);
        this.userMessage = userMessage;
        this.status = status;
    }
}
