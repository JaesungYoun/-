package com.ssg.ssg.domain.order.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CancelOrderItemRequest {
    @NotNull(message = "주문 번호는 필수입니다.")
    private Long orderId; // 주문 번호
    @NotNull(message = "상품 ID는 필수입니다.")
    private Long itemId; // 상품 ID
}
