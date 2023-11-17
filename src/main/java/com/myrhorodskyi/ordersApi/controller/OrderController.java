package com.myrhorodskyi.ordersApi.controller;

import com.myrhorodskyi.ordersApi.dto.OrderRequest;
import com.myrhorodskyi.ordersApi.model.entity.Order;
import com.myrhorodskyi.ordersApi.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/{id}")
    public Order getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }

    @PostMapping
    public Order createOrder(@RequestBody OrderRequest orderRequest) {
        return orderService.createOrder(orderRequest);
    }

    @DeleteMapping("/{id}")
    public void cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
    }

    @PostMapping("/{orderId}/pay")
    public void payOrder(@PathVariable Long orderId) {
        orderService.payOrder(orderId);
    }
}
