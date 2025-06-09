package com.ssg.ssg;

import com.ssg.ssg.domain.order.entity.ItemEntity;
import com.ssg.ssg.domain.order.entity.OrderEntity;
import com.ssg.ssg.domain.order.entity.OrderItemEntity;
import com.ssg.ssg.domain.order.enums.OrderStatus;
import com.ssg.ssg.global.code.ErrorCode;
import com.ssg.ssg.global.exception.BadRequestException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderItemEntityTest {

    @Test
    void createOrderItem_주문_상품_생성() {
        /** Given */
        ItemEntity item = new ItemEntity(1000000001L, "이마트 생수", 800, 100, 1000, 1L);

        Integer quantity = 3;
        Integer expectedPurchasePrice = quantity * (item.getPrice() - item.getDiscountPrice());

        /** When */
        OrderItemEntity orderItem = OrderItemEntity.createOrderItem(item, quantity, item.getPurchasePrice());

        /** Then */
        assertThat(orderItem.getItem()).isEqualTo(item);
        assertThat(orderItem.getQuantity()).isEqualTo(quantity);
        assertThat(orderItem.getPurchasePrice()).isEqualTo(expectedPurchasePrice);
        assertThat(orderItem.getOrderStatus()).isEqualTo(OrderStatus.ORDER);
    }

    @Test
    void cancel_주문_상품_취소_정상() {
        /** Given */
        ItemEntity item = new ItemEntity(1000000001L, "이마트 생수", 800, 100, 1000, 1L);
        OrderEntity order = new OrderEntity(1L, 7000, new ArrayList<>());
        OrderItemEntity orderItem = new OrderItemEntity(1L, order, item, 10, 7000, OrderStatus.ORDER);

        Integer stockBefore = item.getStock();

        /** When */
        // 상품 취소
        Integer canceledPrice = orderItem.cancel();

        /** Then */
        // 주문 상태가 CANCELED로 변경됐는지 검증
        assertThat(OrderStatus.CANCELED).isEqualTo(orderItem.getOrderStatus());

        // 재고가 quantity(주문 수량) 만큼 증가했는지 검증
        assertThat(stockBefore + orderItem.getQuantity()).isEqualTo(item.getStock());

        // 환불 금액이 취소한 상품의 실구매금액과 동일한지 확인
        assertThat(orderItem.getPurchasePrice()).isEqualTo(canceledPrice);
    }

    @Test
    void cancel_이미_취소된_상품_재취소_예외() {
        /** Given */

        ItemEntity item = new ItemEntity(1000000001L, "이마트 생수", 800, 100, 1000, 1L);
        OrderEntity order = new OrderEntity(1L, 7000, new ArrayList<>());
        OrderItemEntity orderItem = new OrderItemEntity(1L, order, item, 10, 7000, OrderStatus.ORDER);

        /** When */
        // 상품 취소
        orderItem.cancel();

        /** Then */

        // 예외 발생하는지 검증
        BadRequestException e = assertThrows(BadRequestException.class, () -> {
            orderItem.cancel();
        });

        // 예외 코드 일치하는지 검증
        assertThat(ErrorCode.ORDER_ITEM_ALREADY_CANCELED).isEqualTo(e.getErrorCode());

    }
}
