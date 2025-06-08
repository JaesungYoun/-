package com.ssg.ssg.global.exception;

import com.ssg.ssg.global.code.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;


@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class ApiControllerExceptionAdvice extends ResponseEntityExceptionHandler {


    /**
     * BaseException을 비롯한 BaseException을 상속받는 모든 커스텀 예외를 이 핸들러가 처리
     * @param e
     * @param request
     * @return
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<Object> handleBaseException(BaseException e, WebRequest request) {
        log.error("handleBaseException: {}", e.getMessage());
        return handleExceptionInternal(e, e.buildExceptionResponseDto(), new HttpHeaders(), e.getHttpStatus(), request);
    }

    /**
     * BaseException 보다 상위인 런타임 예외를 이 핸들러가 처리
     * @param e
     * @param request
     * @return
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleRuntimeException(RuntimeException e, WebRequest request) {
        log.error("handleRunTimeException(runtime exception) : {}", e.getMessage(), e);

        Object body = ExceptionResponse.builder()
                .httpStatus(ErrorCode.RUNTIME_EXCEPTION.getStatus())
                .message(ErrorCode.RUNTIME_EXCEPTION.getMessage())
                .build();

        return handleExceptionInternal(e, body, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    /**
     * 런타임 예외가 아닌 체크 예외를 이 핸들러가 처리
     * @param e
     * @param request
     * @return
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleCheckedException(Exception e, WebRequest request) {
        log.error("handleException(checked exception) : {}", e.getMessage(), e);

        Object body = ExceptionResponse.builder()
                .httpStatus(ErrorCode.EXCEPTION.getStatus())
                .message(ErrorCode.EXCEPTION.getMessage())
                .build();

        return handleExceptionInternal(e, body, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    /**
     * DTO에 대한 Validation Check Error를 핸들링하는 전역 Exeption Handler
     *
     * @return BadRequestException
     */
    @Override
    public ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                               HttpHeaders headers,
                                                               HttpStatusCode status,
                                                               WebRequest request) {

        BindingResult bindingResult = ex.getBindingResult();

        if (bindingResult.hasErrors()) {
            FieldError fieldError = bindingResult.getFieldErrors().stream()
                    .findFirst()
                    .orElse(null);

            if (fieldError != null && fieldError.getDefaultMessage() != null && !fieldError.getDefaultMessage().isBlank()) {
                log.warn("Validation Error - Field: {}, Message: {}", fieldError.getField(), fieldError.getDefaultMessage());

                BadRequestException badRequest = new BadRequestException(fieldError.getDefaultMessage());
                return handleBaseException(badRequest, request);
            }
        }

        BadRequestException defaultException = new BadRequestException(ErrorCode.METHOD_ARGUMENT_NOT_VALID_EXCEPTION);
        return handleBaseException(defaultException, request);
    }


}
