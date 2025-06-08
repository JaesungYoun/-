package com.ssg.ssg.domain.order.dto.response;

import com.ssg.ssg.domain.order.entity.OrderEntity;
import com.ssg.ssg.domain.order.entity.OrderItemEntity;
import com.ssg.ssg.domain.order.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetOrderResponse {

    private Long orderId;
    private List<OrderItem> orderItemList;
    private Integer totalPrice;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItem {
        private Long itemId;             // 상품 ID
        private String name;             // 상품명
        private Integer price;           // 판매가격
        private Integer discountPrice;   // 할인금액
        private Integer quantity;        // 주문 수량
        private Integer purchasePrice;   // 실구매금액
        private OrderStatus orderStatus; // 주문 상품 상태

        public static OrderItem toDto(OrderItemEntity orderItem) {
            return OrderItem.builder()
                    .itemId(orderItem.getItem().getId())
                    .name(orderItem.getItem().getName())
                    .price(orderItem.getItem().getPrice())
                    .discountPrice(orderItem.getItem().getDiscountPrice())
                    .quantity(orderItem.getQuantity())
                    .purchasePrice(orderItem.getPurchasePrice())
                    .orderStatus(orderItem.getOrderStatus())
                    .build();
        }
    }

    /**
     * 주문 조회 응답 DTO 생성
     * @param order
     * @return
     */
    public static GetOrderResponse toDto(OrderEntity order) {
        List<OrderItem> orderItemList = order.getOrderItems().stream()
                .map(OrderItem::toDto)
                .toList();

        return GetOrderResponse.builder()
                .orderId(order.getId())
                .orderItemList(orderItemList)
                .totalPrice(order.getTotalPrice())
                .build();
    }

}
