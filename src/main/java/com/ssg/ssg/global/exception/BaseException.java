package com.ssg.ssg.global.exception;

import com.ssg.ssg.global.code.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BaseException extends RuntimeException {

    protected final HttpStatus httpStatus;

    protected ErrorCode errorCode;

    protected BaseException(ErrorCode errorCode, HttpStatus httpStatus) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    protected BaseException(ErrorCode errorCode, HttpStatus httpStatus, String message) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    protected BaseException(HttpStatus httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public ExceptionResponse buildExceptionResponseDto() {
        return ExceptionResponse.builder()
                .httpStatus(this.httpStatus)
                .message(getMessage())
                .build();
    }
}
