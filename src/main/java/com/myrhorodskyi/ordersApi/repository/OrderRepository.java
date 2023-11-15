package com.myrhorodskyi.ordersApi.repository;

import com.myrhorodskyi.ordersApi.model.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
