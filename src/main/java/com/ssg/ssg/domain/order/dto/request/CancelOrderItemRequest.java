package com.ssg.ssg.domain.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CancelOrderItemRequest {
    @NotNull(message = "주문 번호는 필수입니다.")
    @Schema(description = "주문 번호", example = "1")
    private Long orderId; // 주문 번호
    @NotNull(message = "상품 ID는 필수입니다.")
    @Schema(description = "상품 ID", example = "1000000001")
    private Long itemId; // 상품 ID
}
