package com.ssg.ssg;

import com.ssg.ssg.domain.order.dto.request.CancelOrderItemRequest;
import com.ssg.ssg.domain.order.dto.request.CreateOrderRequest;
import com.ssg.ssg.domain.order.dto.response.CancelOrderItemResponse;
import com.ssg.ssg.domain.order.dto.response.CreateOrderResponse;
import com.ssg.ssg.domain.order.dto.response.GetOrderResponse;
import com.ssg.ssg.domain.order.entity.ItemEntity;
import com.ssg.ssg.domain.order.entity.OrderEntity;
import com.ssg.ssg.domain.order.entity.OrderItemEntity;
import com.ssg.ssg.domain.order.enums.OrderStatus;
import com.ssg.ssg.domain.order.facade.OrderServiceFacade;
import com.ssg.ssg.domain.order.repository.ItemRepository;
import com.ssg.ssg.domain.order.repository.OrderItemRepository;
import com.ssg.ssg.domain.order.repository.OrderRepository;
import com.ssg.ssg.domain.order.service.OrderService;
import com.ssg.ssg.global.code.ErrorCode;
import com.ssg.ssg.global.exception.BadRequestException;
import com.ssg.ssg.global.exception.NotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class OrderServiceTest {

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
    void createOrder_주문_생성_성공() {
        /** Given */

        // 요청 DTO 생성
        CreateOrderRequest request = new CreateOrderRequest(List.of(
                new CreateOrderRequest.OrderItem(1000000001L, 10),
                new CreateOrderRequest.OrderItem(1000000003L, 20),
                new CreateOrderRequest.OrderItem(1000000004L, 30)
        ));

        // 주문 전 재고 조회
        ItemEntity item1Before = itemRepository.findById(1000000001L).orElseThrow(() -> new NotFoundException(ErrorCode.ITEM_NOT_FOUND));
        ItemEntity item2Before = itemRepository.findById(1000000003L).orElseThrow(() -> new NotFoundException(ErrorCode.ITEM_NOT_FOUND));
        ItemEntity item3Before = itemRepository.findById(1000000004L).orElseThrow(() -> new NotFoundException(ErrorCode.ITEM_NOT_FOUND));

        Integer stock1Before = item1Before.getStock();
        Integer stock2Before = item2Before.getStock();
        Integer stock3Before = item3Before.getStock();

        Integer total = (item1Before.getPrice() - item1Before.getDiscountPrice()) * 10
                + (item2Before.getPrice() - item2Before.getDiscountPrice()) * 20
                + (item3Before.getPrice() - item3Before.getDiscountPrice()) * 30;

        /** When */
        CreateOrderResponse response = orderService.createOrder(request);

        // 영속성 컨텍스트 flush 및 clear
        em.flush();
        em.clear();

        /** Then */

        // 주문 후 재고 조회
        ItemEntity item1After = itemRepository.findById(1000000001L).orElseThrow(() -> new NotFoundException(ErrorCode.ITEM_NOT_FOUND));
        ItemEntity item2After = itemRepository.findById(1000000003L).orElseThrow(() -> new NotFoundException(ErrorCode.ITEM_NOT_FOUND));
        ItemEntity item3After = itemRepository.findById(1000000004L).orElseThrow(() -> new NotFoundException(ErrorCode.ITEM_NOT_FOUND));

        // 재고 차감 검증
        assertEquals(stock1Before - 10, item1After.getStock());
        assertEquals(stock2Before - 20, item2After.getStock());
        assertEquals(stock3Before - 30, item3After.getStock());

        // 주문 조회
        Long orderId = response.getOrderId();
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ORDER_NOT_FOUND));

        // 주문 상품 조회
        OrderItemEntity orderItem1 = orderItemRepository.findByOrderAndItem(order, item1Before)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ORDER_ITEM_NOT_FOUND));

        OrderItemEntity orderItem2 = orderItemRepository.findByOrderAndItem(order, item2Before)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ORDER_ITEM_NOT_FOUND));

        OrderItemEntity orderItem3 = orderItemRepository.findByOrderAndItem(order, item3Before)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ORDER_ITEM_NOT_FOUND));


        // 응답 DTO 검증
        assertThat(response.getOrderItemList().size()).isEqualTo(3);

        Map<Long, CreateOrderResponse.OrderItem> orderItemMap = response.getOrderItemList().stream()
                .collect(Collectors.toMap(CreateOrderResponse.OrderItem::getItemId, Function.identity()));

        // 실구매금액 검증(DTO)
        assertThat(orderItemMap.get(1000000001L).getPurchasePrice())
                .isEqualTo((item1Before.getPrice() - item1Before.getDiscountPrice()) * 10);
        assertThat(orderItemMap.get(1000000003L).getPurchasePrice())
                .isEqualTo((item2Before.getPrice() - item2Before.getDiscountPrice()) * 20);
        assertThat(orderItemMap.get(1000000004L).getPurchasePrice())
                .isEqualTo((item3Before.getPrice() - item3Before.getDiscountPrice()) * 30);

        // 실구매금액 검증(DB)
        assertThat(orderItem1.getPurchasePrice())
                .isEqualTo((item1Before.getPrice() - item1Before.getDiscountPrice()) * 10);
        assertThat(orderItem2.getPurchasePrice())
                .isEqualTo((item2Before.getPrice() - item2Before.getDiscountPrice()) * 20);
        assertThat(orderItem3.getPurchasePrice())
                .isEqualTo((item3Before.getPrice() - item3Before.getDiscountPrice()) * 30);

        // 주문 상태 검증
        assertThat(orderItem1.getOrderStatus()).isEqualTo(OrderStatus.ORDER);
        assertThat(orderItem2.getOrderStatus()).isEqualTo(OrderStatus.ORDER);
        assertThat(orderItem3.getOrderStatus()).isEqualTo(OrderStatus.ORDER);

        // OrderItem의 orderId가 주문의 orderId와 일치하는지 검증
        assertThat(orderItem1.getOrder().getId()).isEqualTo(order.getId());
        assertThat(orderItem2.getOrder().getId()).isEqualTo(order.getId());
        assertThat(orderItem3.getOrder().getId()).isEqualTo(order.getId());

        // 전체 주문 금액 검증(DB)
        assertThat(order.getTotalPrice()).isEqualTo(total);
        // 전체 주문 금액 검증(DTO)
        assertThat(response.getTotalPrice()).isEqualTo(total);

        // 주문 수량 검증(DTO)
        assertThat(orderItem1.getQuantity()).isEqualTo(orderItemMap.get(1000000001L).getQuantity());
        assertThat(orderItem2.getQuantity()).isEqualTo(orderItemMap.get(1000000003L).getQuantity());
        assertThat(orderItem3.getQuantity()).isEqualTo(orderItemMap.get(1000000004L).getQuantity());

    }

    @Test
    void createOrder_주문_생성_실패_재고부족() {
        /** Given */;
        CreateOrderRequest request = new CreateOrderRequest(List.of(
                new CreateOrderRequest.OrderItem(1000000001L, 99999)
        ));

        /** When & Then */
        BadRequestException e= assertThrows(BadRequestException.class, () -> {orderService.createOrder(request);});

        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.OUT_OF_STOCK);
    }


    @Test
    void cancelOrderItem_주문상품_개별취소_성공() {
        /** Given */
        
        // 주문 생성
        CreateOrderRequest request = new CreateOrderRequest(List.of(
                new CreateOrderRequest.OrderItem(1000000001L, 10),
                new CreateOrderRequest.OrderItem(1000000003L, 20)
        ));

        CreateOrderResponse response = orderService.createOrder(request);

        // 생성된 주문 번호
        Long orderId = response.getOrderId();

        em.flush();
        em.clear();


        // 주문 상품 조회
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ORDER_NOT_FOUND));
        ItemEntity item1 = itemRepository.findById(1000000001L)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ITEM_NOT_FOUND));
        
        // 취소할 상품 개별 조회
        OrderItemEntity itemToCancel = orderItemRepository.findByOrderAndItem(order, item1)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ORDER_ITEM_NOT_FOUND));


        Integer totalBeforeCancel = order.getTotalPrice();              // 취소 전 전체 주문 금액 
        Integer expectedCanceledPrice = itemToCancel.getPurchasePrice();  // 환불 금액
        Integer itemToCancelQuantity = itemToCancel.getQuantity();          // 취소할 상품의 주문 수량
        Integer stockBeforeCancel = item1.getStock();                   // 취소할 상품의 재고

        /** When */
        
        // 주문 상품 개별 취소
        CancelOrderItemRequest cancelRequest = new CancelOrderItemRequest(orderId, itemToCancel.getItem().getId());
        CancelOrderItemResponse cancelResponse = orderService.cancelOrderItem(cancelRequest);

        em.flush();
        em.clear();

        /** Then */
        
        // 주문, 상품, 주문 상품 조회
        OrderEntity updatedOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ORDER_NOT_FOUND));
        ItemEntity updatedItem1 = itemRepository.findById(itemToCancel.getItem().getId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.ITEM_NOT_FOUND));
        OrderItemEntity canceledOrderItem = orderItemRepository.findByOrderAndItem(updatedOrder, updatedItem1)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ORDER_ITEM_NOT_FOUND));

        // 주문 상품 상태 DB 검증
        assertThat(canceledOrderItem.getOrderStatus()).isEqualTo(OrderStatus.CANCELED);

        // 재고 복구 DB 검증
        assertThat(stockBeforeCancel + itemToCancelQuantity).isEqualTo(updatedItem1.getStock());

        // 남은 전체 주문 금액 DB 검증
        assertThat(totalBeforeCancel - expectedCanceledPrice).isEqualTo(updatedOrder.getTotalPrice());

        // 응답 DTO 검증
        assertThat(cancelResponse.getItemId()).isEqualTo(updatedItem1.getId()); // 상품 ID
        assertThat(cancelResponse.getName()).isEqualTo(updatedItem1.getName()); // 상품명
        assertThat(cancelResponse.getPrice()).isEqualTo(updatedItem1.getPrice()); // 판매가격
        assertThat(cancelResponse.getDiscountPrice()).isEqualTo(updatedItem1.getDiscountPrice()); // 할인금액
        assertThat(cancelResponse.getStock()).isEqualTo(updatedItem1.getStock()); // 재고
        assertThat(cancelResponse.getCanceledPrice()).isEqualTo(expectedCanceledPrice); // 환불금액
        assertThat(cancelResponse.getTotalPrice()).isEqualTo(updatedOrder.getTotalPrice()); // 전체 주문 금액

    }

    @Test
    void cancelOrderItem_존재하지_않는_주문번호_실패() {
        /** Given */
        Long invalidOrderId = 999999999L; // 존재하지 않는 주문 ID

        // 존재하지 않는 주문 ID로 DTO 생성
        CancelOrderItemRequest request = new CancelOrderItemRequest(invalidOrderId, 1000000001L);

        /** When & Then */
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {orderService.cancelOrderItem(request);});

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ORDER_NOT_FOUND);
    }

    @Test
    void cancelOrderItem_존재하지_않는_상품_ID_실패() {
        /** Given */
        CreateOrderRequest orderRequest = new CreateOrderRequest(List.of(
                new CreateOrderRequest.OrderItem(1000000001L, 1)
        ));
        CreateOrderResponse response = orderService.createOrder(orderRequest);
        Long orderId = response.getOrderId();

        em.flush();
        em.clear();

        Long invalidItemId = 999999999L; // 존재하지 않는 상품 ID
        
        // 존재하지 않는 상품 ID로 DTO 생성
        CancelOrderItemRequest cancelRequest = new CancelOrderItemRequest(orderId, invalidItemId);

        /** When & Then */
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {orderService.cancelOrderItem(cancelRequest);});

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ITEM_NOT_FOUND);
    }

    @Test
    void cancelOrderItem_이미취소된상품_재취소_실패() {
        /** Given */
        CreateOrderRequest orderRequest = new CreateOrderRequest(List.of(
                new CreateOrderRequest.OrderItem(1000000001L, 1)
        ));
        CreateOrderResponse response = orderService.createOrder(orderRequest);
        Long orderId = response.getOrderId();

        em.flush();
        em.clear();

        /** When & Then */
        // 취소
        CancelOrderItemRequest cancelRequest = new CancelOrderItemRequest(orderId, 1000000001L);
        orderService.cancelOrderItem(cancelRequest);

        em.flush();
        em.clear();

        // 재취소 (예외 발생)
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            orderService.cancelOrderItem(cancelRequest);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ORDER_ITEM_ALREADY_CANCELED);
    }

    @Test
    void getOrder_주문조회_성공() {
        /** Given */
        // 주문 생성
        CreateOrderRequest request = new CreateOrderRequest(List.of(
                new CreateOrderRequest.OrderItem(1000000001L, 10),
                new CreateOrderRequest.OrderItem(1000000003L, 20)
        ));
        CreateOrderResponse createResponse = orderService.createOrder(request);
        Long orderId = createResponse.getOrderId();

        em.flush();
        em.clear();

        /** When */
        
        // 주문 조회
        GetOrderResponse response = orderService.getOrder(orderId);

        /** Then */

        // 주문 엔티티 조회
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ORDER_NOT_FOUND));

        // 주문으로 주문된 상품 리스트 조회
        List<OrderItemEntity> orderItemList = order.getOrderItems();
        
        // 생성된 주문 번호와 조회한 주문 번호가 같은지 검증
        assertThat(response.getOrderId()).isEqualTo(orderId);

        // 전체 주문 금액 검증
        assertThat(response.getTotalPrice()).isEqualTo(order.getTotalPrice());

        // 주문 상품 개수 검증
        assertThat(response.getOrderItemList().size()).isEqualTo(orderItemList.size());

        // 응답 DTO 검증을 위한 Map 생성
        Map<Long, GetOrderResponse.OrderItem> dtoMap = response.getOrderItemList().stream()
                .collect(Collectors.toMap(GetOrderResponse.OrderItem::getItemId, Function.identity()));

        // 응답 DTO와 DB 값 동일한지 검증
        for (OrderItemEntity orderItem : orderItemList) {
            Long itemId = orderItem.getItem().getId();
            GetOrderResponse.OrderItem dto = dtoMap.get(itemId);

            assertThat(dto.getItemId()).isEqualTo(orderItem.getItem().getId());
            assertThat(dto.getName()).isEqualTo(orderItem.getItem().getName());
            assertThat(dto.getPrice()).isEqualTo(orderItem.getItem().getPrice());
            assertThat(dto.getDiscountPrice()).isEqualTo(orderItem.getItem().getDiscountPrice());
            assertThat(dto.getQuantity()).isEqualTo(orderItem.getQuantity());
            assertThat(dto.getPurchasePrice()).isEqualTo(orderItem.getPurchasePrice());
            assertThat(dto.getOrderStatus()).isEqualTo(orderItem.getOrderStatus());
        }
    }


    @Test
    void getOrder_존재하지_않는_주문번호_실패() {
        /** Given */
        Long invalidOrderId = 999999999L;

        /** When & Then */
        // 예외 발생 검증
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {orderService.getOrder(invalidOrderId);});

        // 예외 코드 검증
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ORDER_NOT_FOUND);
    }

}
