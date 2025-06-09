package com.ssg.ssg.domain.order.dto.response;

import com.ssg.ssg.domain.order.entity.OrderEntity;
import com.ssg.ssg.domain.order.entity.OrderItemEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
public class CreateOrderResponse {

    @Schema(description = "주문 번호")
    private Long orderId; // 주문 번호
    @Schema(description = "주문 상품 리스트")
    private List<OrderItem> orderItemList; // 주문 상품 리스트
    @Schema(description = "주문 전체 금액")
    private Integer totalPrice; // 주문 전체 금액

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItem {
        @Schema(description = "상품 ID")
        private Long itemId; // 주문한 상품 ID
        @Schema(description = "상품명")
        private String name; // 상품명
        @Schema(description = "주문 수량")
        private Integer quantity; // 주문 상품 수량
        @Schema(description = "주문 상품별 총 실구매금액")
        private Integer purchasePrice; // 실구매금액

        public static OrderItem toOrderItemDto(OrderItemEntity orderItem) {
            return OrderItem.builder()
                    .itemId(orderItem.getItem().getId())
                    .name(orderItem.getItem().getName())
                    .quantity(orderItem.getQuantity())
                    .purchasePrice(orderItem.getPurchasePrice())
                    .build();
        }
    }

    /**
     * 주문 생성 응답 Dto 생성
     * @param order
     * @return
     */
    public static CreateOrderResponse toOrderDto(OrderEntity order) {
        List<OrderItem> orderItemList = order.getOrderItems().stream()
                .map(OrderItem::toOrderItemDto)
                .toList();

        return CreateOrderResponse.builder()
                .orderId(order.getId())
                .totalPrice(order.getTotalPrice())
                .orderItemList(orderItemList)
                .build();
    }

}
