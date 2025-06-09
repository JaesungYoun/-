package com.ssg.ssg.domain.order.service;

import com.ssg.ssg.domain.order.dto.request.CancelOrderItemRequest;
import com.ssg.ssg.domain.order.dto.request.CreateOrderRequest;
import com.ssg.ssg.domain.order.dto.response.CancelOrderItemResponse;
import com.ssg.ssg.domain.order.dto.response.CreateOrderResponse;
import com.ssg.ssg.domain.order.dto.response.GetOrderResponse;
import com.ssg.ssg.domain.order.entity.ItemEntity;
import com.ssg.ssg.domain.order.entity.OrderEntity;
import com.ssg.ssg.domain.order.entity.OrderItemEntity;
import com.ssg.ssg.domain.order.repository.ItemRepository;
import com.ssg.ssg.domain.order.repository.OrderItemRepository;
import com.ssg.ssg.domain.order.repository.OrderRepository;
import com.ssg.ssg.global.code.ErrorCode;
import com.ssg.ssg.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class OrderService {

    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    /**
     * 1. 주문 생성
     * @param request
     * @return
     */
    @Transactional
    public CreateOrderResponse createOrder(CreateOrderRequest request) {

        // item 조회 쿼리 횟수 감소를 위해 id 리스트로 만든 후 IN 쿼리 사용
        List<Long> itemIds = request.getOrderItemList().stream()
                .map(CreateOrderRequest.OrderItem::getItemId)
                .toList();

        // IN 쿼리 조회
        List<ItemEntity> items = itemRepository.findAllByIdIn(itemIds);

        // List → Map 변환
        Map<Long, ItemEntity> itemMap = items.stream()
                .collect(Collectors.toMap(ItemEntity::getId, Function.identity()));

        // 주문 상품을 담을 리스트
        List<OrderItemEntity> orderItems = new ArrayList<>();
        
        for (CreateOrderRequest.OrderItem dto : request.getOrderItemList()) {
            ItemEntity item = itemMap.get(dto.getItemId());
            if (item == null) {
                throw new NotFoundException(ErrorCode.ITEM_NOT_FOUND);
            }

            // 재고 차감
            item.decreaseStock(dto.getQuantity());

            // 상품별 1개당 실구매 금액 계산 (할인 금액 적용)
            Integer purchasePrice = item.getPurchasePrice();

            // 주문 상품 엔티티 생성
            OrderItemEntity orderItem = OrderItemEntity.createOrderItem(item, dto.getQuantity(), purchasePrice);
            // 리스트에 주문 상품 담기
            orderItems.add(orderItem);
        }

        // 주문 엔티티 생성
        OrderEntity order = OrderEntity.createOrder(orderItems);

        // 주문 상품 리스트 저장
        orderItemRepository.saveAll(orderItems);

        // 주문 저장
        orderRepository.save(order);

        return CreateOrderResponse.toOrderDto(order);
    }

    /**
     * 2. 주문 상품 개별 취소
     * @param request
     * @return
     */
    @Transactional
    public CancelOrderItemResponse cancelOrderItem(CancelOrderItemRequest request) {
        OrderEntity order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.ORDER_NOT_FOUND));

        ItemEntity item = itemRepository.findByIdWithOptimisticLock(request.getItemId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.ITEM_NOT_FOUND));

        OrderItemEntity orderItem = orderItemRepository.findByOrderAndItem(order, item)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ORDER_ITEM_NOT_FOUND));


        // 취소 진행 (논리 삭제) , 환불 금액 리턴
        Integer canceledPrice = orderItem.cancel();

        // 주문 전체 금액 재계산
        order.calculateTotalPrice();

        return CancelOrderItemResponse.toDto(orderItem.getItem(), canceledPrice, order.getTotalPrice());
    }

    /**
     * 3. 주문 상품 조회
     * @param orderId
     * @return
     */
    @Transactional(readOnly = true)
    public GetOrderResponse getOrder(Long orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ORDER_NOT_FOUND));

        return GetOrderResponse.toDto(order);
    }

}
