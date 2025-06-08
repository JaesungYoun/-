package com.ssg.ssg.domain.order.repository;

import com.ssg.ssg.domain.order.entity.ItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<ItemEntity, Long> {



}
