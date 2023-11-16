package com.myrhorodskyi.ordersApi.repository;

import com.myrhorodskyi.ordersApi.model.entity.Order;
import com.myrhorodskyi.ordersApi.model.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByStatusAndCreatedAtBefore(OrderStatus status, LocalDateTime createdAt);

}
