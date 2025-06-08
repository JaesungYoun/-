package com.ssg.ssg.domain.order.repository;

import com.ssg.ssg.domain.order.entity.ItemEntity;
import com.ssg.ssg.domain.order.entity.OrderEntity;
import com.ssg.ssg.domain.order.entity.OrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItemEntity, Long> {

    Optional<OrderItemEntity> findByOrderAndItem(OrderEntity order, ItemEntity item);

}
