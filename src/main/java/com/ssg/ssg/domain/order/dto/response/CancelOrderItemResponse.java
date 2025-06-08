package com.ssg.ssg.domain.order.dto.response;

import com.ssg.ssg.domain.order.entity.ItemEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelOrderItemResponse {

    private Long itemId;            // 상품 ID
    private String name;            // 상품명
    private Integer price;          // 판매가격
    private Integer discountPrice;  // 할인 금액
    private Integer stock;          // 재고
    private Integer canceledPrice;  // 환불 금액
    private Integer totalPrice;     // 취소 후 남은 주문 전체 금액

    /**
     * 주문 상품 취소 응답 DTO 생성
     * @param item
     * @param canceledPrice
     * @param totalPrice
     * @return
     */
    public static CancelOrderItemResponse toDto(ItemEntity item, Integer canceledPrice, Integer totalPrice) {
        return CancelOrderItemResponse.builder()
                .itemId(item.getId())
                .name(item.getName())
                .price(item.getPrice())
                .discountPrice(item.getDiscountPrice())
                .stock(item.getStock())
                .canceledPrice(canceledPrice)
                .totalPrice(totalPrice)
                .build();
    }

}
