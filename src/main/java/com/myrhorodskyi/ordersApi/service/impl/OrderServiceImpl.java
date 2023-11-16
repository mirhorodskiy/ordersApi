package com.myrhorodskyi.ordersApi.service.impl;

import com.myrhorodskyi.ordersApi.dto.OrderItemRequest;
import com.myrhorodskyi.ordersApi.dto.OrderRequest;
import com.myrhorodskyi.ordersApi.model.entity.Goods;
import com.myrhorodskyi.ordersApi.model.entity.Order;
import com.myrhorodskyi.ordersApi.model.entity.OrderItem;
import com.myrhorodskyi.ordersApi.model.enums.OrderStatus;
import com.myrhorodskyi.ordersApi.repository.GoodsRepository;
import com.myrhorodskyi.ordersApi.repository.OrderRepository;
import com.myrhorodskyi.ordersApi.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final GoodsRepository goodsRepository;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository, GoodsRepository goodsRepository) {
        this.orderRepository = orderRepository;
        this.goodsRepository = goodsRepository;
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public Order getOrderById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

//    @Override
//    public Order createOrder(Order order) {
//        order.setStatus(OrderStatus.NEW);
//        order.setCreatedAt(LocalDateTime.now());
//        updateGoodsQuantities(order, false);
//        return orderRepository.save(order);
//    }

    @Override
    public Order createOrder(OrderRequest orderRequest) {
        List<OrderItemRequest> orderItemRequests = orderRequest.getOrderItems();

        double totalAmount = orderItemRequests.stream()
                .mapToDouble(orderItem -> {
                    Goods goods = goodsRepository.findById(orderItem.getGoodsId()).orElse(null);
                    if (goods != null) {
                        return goods.getPrice() * orderItem.getQuantity();
                    }
                    return 0.0;
                })
                .sum();

        Order order = new Order();
        order.setCustomerName(orderRequest.getCustomerName());
        order.setOrderItems(createOrderItems(orderItemRequests, order));
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.NEW);
        order.setCreatedAt(LocalDateTime.now());

        updateGoodsQuantities(order, false);

        return orderRepository.save(order);
    }

    private List<OrderItem> createOrderItems(List<OrderItemRequest> orderItemRequests, Order order) {
        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemRequest orderItemRequest : orderItemRequests) {
            Goods goods = goodsRepository.findById(orderItemRequest.getGoodsId()).orElse(null);
            if (goods != null) {
                OrderItem orderItem = new OrderItem();
                orderItem.setGoods(goods);
                orderItem.setQuantity(orderItemRequest.getQuantity());
                orderItem.setOrder(order);
                orderItems.add(orderItem);
            }
        }
        return orderItems;
    }



    @Override
    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }

    @Override
    public void payOrder(Long orderId) {
        Optional<Order> orderOptional = orderRepository.findById(orderId);
        if (orderOptional.isPresent()) {
            Order order = orderOptional.get();
            order.setStatus(OrderStatus.PAID);
            orderRepository.save(order);
        }
    }

    @Scheduled(fixedRate = 600000)
    @Transactional
    public void deleteUnpaidOrders() {
        LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10);
        List<Order> unpaidOrders = orderRepository.findByStatusAndCreatedAtBefore(OrderStatus.NEW, tenMinutesAgo);

        for (Order order : unpaidOrders) {
            updateGoodsQuantities(order, true);
            orderRepository.delete(order);
        }
    }

    private void updateGoodsQuantities(Order order, boolean restore) {
        List<OrderItem> orderItems = order.getOrderItems();

        for (OrderItem orderItem : orderItems) {
            Goods goods = orderItem.getGoods();
            int quantity = orderItem.getQuantity();
            goods.setQuantity(restore ? goods.getQuantity() + quantity : goods.getQuantity() - quantity);
            goodsRepository.save(goods);
        }
    }

}
