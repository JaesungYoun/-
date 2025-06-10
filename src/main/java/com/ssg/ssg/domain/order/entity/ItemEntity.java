package com.ssg.ssg.domain.order.entity;

import com.ssg.ssg.global.code.ErrorCode;
import com.ssg.ssg.global.exception.BadRequestException;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "items")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long id; // 상품 ID

    @NotBlank(message = "상품명은 필수입니다.")
    @Column(name = "name")
    private String name; // 상품명

    @Column(name = "price")
    private Integer price; // 판매가격

    @Min(value = 0, message = "할인 금액은 0원 이상이어야 합니다.")
    @Column(name = "discount_price")
    private Integer discountPrice; // 할인 금액

    @Min(value = 0, message = "재고는 0개 이상이어야 합니다.")
    @Column(name = "stock")
    private Integer stock; // 재고

    @Version
    private Long version;

    // 재고 차감 및 복구
    public void decreaseStock(Integer quantity) {
        if (this.stock < quantity) throw new BadRequestException(name + "의 " + ErrorCode.OUT_OF_STOCK.getMessage());
        this.stock -= quantity;
    }


    // 할인 금액 적용 (상품별 실 구매 금액)
    public Integer getPurchasePrice() {
        return this.price - this.discountPrice;
    }

    // 재고 복구
    public void increaseStock(Integer quantity) {
        this.stock += quantity;
    }
}
