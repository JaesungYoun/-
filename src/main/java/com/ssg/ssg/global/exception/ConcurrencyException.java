package com.ssg.ssg.global.exception;

import com.ssg.ssg.global.code.ErrorCode;
import org.springframework.http.HttpStatus;

public class ConcurrencyException extends BaseException {

    private static final HttpStatus HTTP_STATUS = HttpStatus.CONFLICT;

    public ConcurrencyException(ErrorCode errorCode) {
        super(errorCode, HTTP_STATUS);
    }
}

