package com.myrhorodskyi.ordersApi.service;

import com.myrhorodskyi.ordersApi.dto.OrderRequest;
import com.myrhorodskyi.ordersApi.model.entity.Order;

import java.util.List;

public interface OrderService {

    List<Order> getAllOrders();

    Order getOrderById(Long id);

    Order createOrder(OrderRequest orderRequest);

    void deleteOrder(Long id);

    void payOrder(Long orderId);
}
