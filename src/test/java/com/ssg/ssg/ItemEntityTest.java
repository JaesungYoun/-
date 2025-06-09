package com.ssg.ssg;

import com.ssg.ssg.domain.order.entity.ItemEntity;
import com.ssg.ssg.global.code.ErrorCode;
import com.ssg.ssg.global.exception.BadRequestException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ItemEntityTest {

    @Test
    void decreaseStock_재고차감_성공() {
        /** Given */
        ItemEntity item = new ItemEntity(1000000001L, "이마트 생수", 800, 100, 1000,1L);

        /** When */
        item.decreaseStock(10);

        /** Then */
        assertEquals(990, item.getStock());
    }

    @Test
    void decreaseStock_재고부족_예외() {
        /** Given */
        ItemEntity item = new ItemEntity(1000000001L, "이마트 생수", 800, 100, 1000, 1L);

        /** When & Then */
        // 예외 발생 검증
        BadRequestException e = assertThrows(BadRequestException.class, () -> item.decreaseStock(1001));

        // 에러 코드 일치하는지 검증
        assertThat(ErrorCode.OUT_OF_STOCK).isEqualTo(e.getErrorCode());
    }

    @Test
    void getPurchasePrice_할인적용된_실구매금액_계산() {
        /** Given */
        ItemEntity item = new ItemEntity(1000000001L, "이마트 생수", 800, 100, 1000, 1L);

        Integer expectedPurchasePrice = 700;

        /** When */
        Integer purchasePrice = item.getPurchasePrice();

        /** Then */
        assertThat(purchasePrice).isEqualTo(expectedPurchasePrice);
    }
}

