package com.ssg.ssg.domain.order.controller;

import com.ssg.ssg.domain.order.dto.request.CancelOrderItemRequest;
import com.ssg.ssg.domain.order.dto.request.CreateOrderRequest;
import com.ssg.ssg.domain.order.dto.response.CancelOrderItemResponse;
import com.ssg.ssg.domain.order.dto.response.CreateOrderResponse;
import com.ssg.ssg.domain.order.dto.response.GetOrderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "주문 서비스 API")
public interface OrderControllerDoc {

    @Operation(summary = "주문 생성", description = "주문을 생성합니다.")

    ResponseEntity<CreateOrderResponse> createOrder(@RequestBody @Valid CreateOrderRequest request);
    @Operation(summary = "주문 상품 개별 취소", description = "주문 상품을 개별로 취소합니다.")

    ResponseEntity<CancelOrderItemResponse> cancelOrderItem(@RequestBody @Valid CancelOrderItemRequest request) throws InterruptedException;
    @Operation(summary = "주문 상품 조회", description = "주문 상품을 조회합니다.")

    ResponseEntity<GetOrderResponse> getOrder(@PathVariable Long orderId);


}
