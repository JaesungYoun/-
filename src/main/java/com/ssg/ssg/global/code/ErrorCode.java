package com.ssg.ssg.global.code;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // BAD_REQUEST(400)
    ORDER_ALREADY_CANCELED(HttpStatus.BAD_REQUEST, "이미 취소된 주문 상품입니다."),
    OUT_OF_STOCK(HttpStatus.BAD_REQUEST, "상품 재고가 부족합니다."),
    ORDER_ITEM_ALREADY_CANCELED(HttpStatus.BAD_REQUEST, "해당 주문 상품은 이미 취소되었습니다."),
    METHOD_ARGUMENT_NOT_VALID_EXCEPTION(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),

    // NOT_FOUND(404)
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 주문입니다."),
    ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 상품입니다."),
    ORDER_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "주문 내 해당 상품이 존재하지 않습니다."),

    CONCURRENCY_EXCEPTION(HttpStatus.CONFLICT, "동시에 리소스를 수정하려하여 에러가 발생하였습니다."),

    // 서버 예외
    EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "오류가 발생하였습니다."),
    RUNTIME_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "오류가 발생하였습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
