package com.ssg.ssg.global.exception;

import com.ssg.ssg.global.code.ErrorCode;
import org.springframework.http.HttpStatus;

public class NotFoundException extends BaseException {

    private static final HttpStatus HTTP_STATUS = HttpStatus.NOT_FOUND;

    public NotFoundException(ErrorCode errorCode) {
        super(errorCode, HTTP_STATUS);
    }

}
