package com.ssg.ssg.domain.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    @Valid
    @Schema(description = "주문 상품 리스트")
    private List<OrderItem> orderItemList;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItem {
        @NotNull(message = "상품 ID는 필수입니다.")
        @Schema(description = "상품 ID", example = "1000000001")
        private Long itemId;
        @Min(value = 1, message = "수량은 1 이상이어야 합니다.")
        @Schema(description = "주문 수량", example = "1")
        private Integer quantity;

    }
}
