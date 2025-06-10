package com.ssg.ssg.global.exception;

import com.ssg.ssg.global.code.ErrorCode;
import org.springframework.http.HttpStatus;

public class BadRequestException extends BaseException{
    private static final HttpStatus HTTP_STATUS = HttpStatus.BAD_REQUEST;

    public BadRequestException(ErrorCode errorCode) {
        super(errorCode, HTTP_STATUS);
    }

    public BadRequestException(String message) {
        super(HTTP_STATUS, message);
    }

    public BadRequestException(ErrorCode errorCode, String message) {
        super(errorCode, HTTP_STATUS, message);
    }


}
