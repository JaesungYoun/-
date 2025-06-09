package com.ssg.ssg.domain.order.facade;

import com.ssg.ssg.domain.order.dto.request.CancelOrderItemRequest;
import com.ssg.ssg.domain.order.dto.response.CancelOrderItemResponse;
import com.ssg.ssg.domain.order.service.OrderService;
import com.ssg.ssg.global.code.ErrorCode;
import com.ssg.ssg.global.exception.ConcurrencyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderServiceFacade {

    private final OrderService orderService;

    public CancelOrderItemResponse cancelOrderItem(CancelOrderItemRequest request) throws InterruptedException {
        int retryCount = 0;
        int maxRetry = 5;

        while (true) {
            try {
                return orderService.cancelOrderItem(request);
            } catch (OptimisticLockingFailureException e) {
                retryCount++;
                log.info("낙관적 락 충돌 발생, 재시도 {}회", retryCount);
                Thread.sleep(50);
            }
        }
        //throw new ConcurrencyException(ErrorCode.CONCURRENCY_EXCEPTION);
    }

}
