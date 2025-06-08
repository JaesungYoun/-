package com.ssg.ssg;

import com.ssg.ssg.domain.order.entity.ItemEntity;
import com.ssg.ssg.domain.order.entity.OrderEntity;
import com.ssg.ssg.domain.order.entity.OrderItemEntity;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderEntityTest {

    @Test
    void createOrder_주문상품_리스트로_주문_생성() {
        /** Given */
        ItemEntity item1 = new ItemEntity(1000000001L, "이마트 생수", 800, 100, 1000);

        ItemEntity item2 = new ItemEntity(1000000002L, "신라면 멀티팩", 4200, 500, 500);

        OrderItemEntity orderItem1 = OrderItemEntity.createOrderItem(item1, 10, item1.getPurchasePrice());
        OrderItemEntity orderItem2 = OrderItemEntity.createOrderItem(item2, 20, item2.getPurchasePrice());

        List<OrderItemEntity> orderItems = new ArrayList<>();
        orderItems.add(orderItem1);
        orderItems.add(orderItem2);

        /** When */
        OrderEntity order = OrderEntity.createOrder(orderItems);

        /** Then */
        assertThat(order.getOrderItems()).containsExactlyInAnyOrder(orderItem1, orderItem2);

        // 실주문금액 합계
        Integer expectedTotalPrice = orderItems.stream()
                .mapToInt(OrderItemEntity::getPurchasePrice)
                .sum();

        // 실주문금액의 합계와 주문 전체 금액이 동일한 지 검증
        assertThat(order.getTotalPrice()).isEqualTo(expectedTotalPrice);
    }

    @Test
    void calculateTotalPrice_전체_주문금액_계산() {
        /** Given */
        ItemEntity item1 = new ItemEntity(1000000001L, "이마트 생수", 800, 100, 1000);

        ItemEntity item2 = new ItemEntity(1000000002L, "신라면 멀티팩", 4200, 500, 500);

        // 주문 상품 생성
        OrderItemEntity orderItem1 = OrderItemEntity.createOrderItem(item1, 10, item1.getPurchasePrice());
        OrderItemEntity orderItem2 = OrderItemEntity.createOrderItem(item2, 20, item2.getPurchasePrice());

        List<OrderItemEntity> orderItems = new ArrayList<>();
        orderItems.add(orderItem1);
        orderItems.add(orderItem2);

        // 주문 생성
        OrderEntity order = OrderEntity.createOrder(orderItems);

        // 주문 생성 후 item2 취소
        orderItem2.cancel();

        /** When */
        order.calculateTotalPrice();

        /** Then */
        Integer expectedTotal = orderItem1.getPurchasePrice(); // orderItem2는 취소됐으므로 제외
        assertThat(order.getTotalPrice()).isEqualTo(expectedTotal);
    }

}
