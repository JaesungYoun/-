package com.ssg.ssg.domain.order.entity;

import com.ssg.ssg.domain.order.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id; // 주문번호

    @Column(name = "total_price", nullable = false)
    @Builder.Default
    private Integer totalPrice = 0; // 주문 전체 금액

    @OneToMany(mappedBy = "order")
    @Builder.Default
    private List<OrderItemEntity> orderItems = new ArrayList<>();

    // 주문 생성
    public static OrderEntity createOrder(List<OrderItemEntity> orderItems) {
        OrderEntity order = new OrderEntity();
        for (OrderItemEntity item : orderItems) {
            order.addOrderItem(item);
        }
        order.calculateTotalPrice(); // 주문 전체 금액 계산
        return order;
    }

    // 비어있는 주문 생성(주문 상품을 주문에 넣기 전 초기화 용도)
    public static OrderEntity createEmptyOrder() {
        return new OrderEntity();
    }
    
    // 주문의 전체 금액 계산
    public void calculateTotalPrice() {
        this.totalPrice = orderItems.stream()
                .filter(item -> item.getOrderStatus() != OrderStatus.CANCELED) // 취소된 상품 제외
                .mapToInt(item -> item.getPurchasePrice())
                .sum();
    }

    // 양방향 연관관계 값 설정
    public void addOrderItem(OrderItemEntity orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

}
