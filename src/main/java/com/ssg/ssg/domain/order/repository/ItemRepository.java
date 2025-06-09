package com.ssg.ssg.domain.order.repository;

import com.ssg.ssg.domain.order.entity.ItemEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<ItemEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM ItemEntity i WHERE i.id IN :ids")
    List<ItemEntity> findAllByIdIn(@Param("ids") List<Long> ids);

    // @Version 어노테이션만 적용해도 낙관적 락 적용됨
    //@Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT i FROM ItemEntity i WHERE i.id = :id")
    Optional<ItemEntity> findByIdWithOptimisticLock(@Param("id") Long id);

}
