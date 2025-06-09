package com.ssg.ssg;

import com.ssg.ssg.domain.order.dto.request.CancelOrderItemRequest;
import com.ssg.ssg.domain.order.dto.request.CreateOrderRequest;
import com.ssg.ssg.domain.order.dto.response.CreateOrderResponse;
import com.ssg.ssg.domain.order.entity.ItemEntity;
import com.ssg.ssg.domain.order.entity.OrderEntity;
import com.ssg.ssg.domain.order.entity.OrderItemEntity;
import com.ssg.ssg.domain.order.facade.OrderServiceFacade;
import com.ssg.ssg.domain.order.repository.ItemRepository;
import com.ssg.ssg.domain.order.repository.OrderItemRepository;
import com.ssg.ssg.domain.order.repository.OrderRepository;
import com.ssg.ssg.domain.order.service.OrderService;
import com.ssg.ssg.global.code.ErrorCode;
import com.ssg.ssg.global.exception.NotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ConCurrencyTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderServiceFacade orderServiceFacade;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;



    @Test
    void decreaseStock_동시에_100개_주문생성_재고차감() throws InterruptedException {
        /** Given */
        CreateOrderRequest request = new CreateOrderRequest(List.of(
                new CreateOrderRequest.OrderItem(1000000001L, 5),
                new CreateOrderRequest.OrderItem(1000000002L, 3),
                new CreateOrderRequest.OrderItem(1000000003L, 1)
        ));

        /** When */
        // 100개의 스레드를 동시에 실행하여 주문 상품 취소 요청을 병렬로 수행 (동시성 테스트 목적)
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    orderService.createOrder(request);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        /** Then */
        ItemEntity decreasedItem1 = itemRepository.findById(1000000001L)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ITEM_NOT_FOUND));

        ItemEntity decreasedItem2 = itemRepository.findById(1000000002L)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ITEM_NOT_FOUND));

        ItemEntity decreasedItem3 = itemRepository.findById(1000000003L)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ITEM_NOT_FOUND));

        assertThat(decreasedItem1.getStock()).isEqualTo(500);
        assertThat(decreasedItem2.getStock()).isEqualTo(200);
        assertThat(decreasedItem3.getStock()).isEqualTo(100);
    }


    @Test
    void increaseStock_동시에_100개_주문상품_개별취소_재고복구() throws InterruptedException {
        /** Given **/
        Long itemId = 1000000001L;
        int orderCount = 100;

        // 주문 생성 (각 주문에 동일 아이템 1개씩 포함)
        List<Long> orderIds = new ArrayList<>();
        for (int i = 0; i < orderCount; i++) {
            CreateOrderRequest request = new CreateOrderRequest(List.of(
                    new CreateOrderRequest.OrderItem(itemId, 1)
            ));
            CreateOrderResponse response = orderService.createOrder(request);
            orderIds.add(response.getOrderId());
        }

        // 취소 전 상품 조회
        ItemEntity item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ITEM_NOT_FOUND));

        // 취소 전 재고
        Integer stockBefore = item.getStock();

        /** When **/
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(orderCount);

        // 각 주문별로 취소 요청을 병렬로 실행
        for (Long orderId : orderIds) {
            CancelOrderItemRequest cancelRequest = new CancelOrderItemRequest(orderId, itemId);
            executorService.submit(() -> {
                try {
                    orderServiceFacade.cancelOrderItem(cancelRequest);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        /** Then **/
        // 재고가 100만큼 증가했는지 검증
        ItemEntity updatedItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ITEM_NOT_FOUND));

        assertThat(updatedItem.getStock()).isEqualTo(stockBefore + orderCount);
    }
}
