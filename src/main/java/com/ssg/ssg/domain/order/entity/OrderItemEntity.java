package com.ssg.ssg.domain.order.entity;

import com.ssg.ssg.domain.order.enums.OrderStatus;
import com.ssg.ssg.global.code.ErrorCode;
import com.ssg.ssg.global.exception.BadRequestException;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "order_items",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"order_id", "item_id"})
})
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long id;  // 주문 상품 id

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT), nullable = false)
    @NotNull
    private OrderEntity order; // 주문

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT), nullable = false)
    @NotNull
    private ItemEntity item; // 상품

    @Column(name = "qty")
    private Integer quantity; // 수량

    @Column(name = "purchase_price")
    private Integer purchasePrice; // 실 구매금액

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private OrderStatus orderStatus = OrderStatus.ORDER; // 주문 상품 상태

    // 주문 상품 생성
    public static OrderItemEntity createOrderItem(OrderEntity order, ItemEntity item, Integer quantity, Integer purchasePrice) {
        OrderItemEntity orderItem = OrderItemEntity.builder()
                .order(order)
                .item(item)
                .quantity(quantity)
                .purchasePrice(quantity * purchasePrice)
                .orderStatus(OrderStatus.ORDER)
                .build();
        return orderItem;
    }


    // 연관관계 셋팅
    public void setOrder(OrderEntity order) {
        this.order = order;
    }

    // 상품 취소
    public Integer cancel() {
        if (orderStatus == OrderStatus.CANCELED) { // 이미 취소되었는 지 여부 체크
            throw new BadRequestException(ErrorCode.ORDER_ITEM_ALREADY_CANCELED);
        }
        orderStatus = OrderStatus.CANCELED;

        // 재고 복구
        item.increaseStock(quantity);

        return purchasePrice;
    }


}
