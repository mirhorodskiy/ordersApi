package com.myrhorodskyi.ordersApi.service.impl;

import com.myrhorodskyi.ordersApi.dto.OrderItemRequest;
import com.myrhorodskyi.ordersApi.dto.OrderRequest;
import com.myrhorodskyi.ordersApi.exception.SearchRuntimeException;
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
        return orderRepository.findById(id).orElseThrow(() -> new SearchRuntimeException("Order not found with id: " + id));
    }

    @Override
    public Order createOrder(OrderRequest orderRequest) {
        String customerName = orderRequest.getCustomerName();
        List<OrderItemRequest> orderItemRequests = orderRequest.getOrderItems();

        if (customerName == null || orderItemRequests == null || orderItemRequests.isEmpty()) {
            throw new IllegalArgumentException("Invalid order request");
        }


        double totalAmount = orderItemRequests.stream()
                .mapToDouble(orderItem -> {
                    Long goodsId = orderItem.getGoodsId();
                    int quantity = orderItem.getQuantity();

                    Goods goods = goodsRepository.findById(goodsId).orElseThrow(() ->
                            new SearchRuntimeException("Goods not found with id: " + goodsId));

                    if (goods.getQuantity() < quantity) {
                        throw new IllegalStateException("Not enough quantity available for goods with id: " + goodsId);
                    }

                    return goods.getPrice() * quantity;
                })
                .sum();

        Order order = new Order();
        order.setCustomerName(customerName);
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
            Long goodsId = orderItemRequest.getGoodsId();
            int quantity = orderItemRequest.getQuantity();

            Goods goods = goodsRepository.findById(goodsId).orElseThrow(() ->
                    new SearchRuntimeException("Goods not found with id: " + goodsId));

            OrderItem orderItem = new OrderItem();
            orderItem.setGoods(goods);
            orderItem.setQuantity(quantity);
            orderItem.setOrder(order);
            orderItems.add(orderItem);
        }
        return orderItems;
    }


    @Override
    public void cancelOrder(Long id) {
        Optional<Order> optionalOrder = orderRepository.findById(id);

        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();

            if (order.getStatus().equals(OrderStatus.NEW)) {
                updateGoodsQuantities(order, true);
                order.setStatus(OrderStatus.CANCELLED);
                orderRepository.save(order);
            } else {
                throw new IllegalStateException("Can only cancel orders with status NEW");
            }
        } else {
            throw new SearchRuntimeException("Order not found with id: " + id);
        }
    }

    @Override
    public void payOrder(Long orderId) {
        Optional<Order> orderOptional = orderRepository.findById(orderId);
        if (orderOptional.isPresent() && orderOptional.get().getStatus().equals(OrderStatus.NEW)) {
            Order order = orderOptional.get();
            order.setStatus(OrderStatus.PAID);
            orderRepository.save(order);
        } else {
            throw new SearchRuntimeException("Order not found with id: " + orderId);
        }
    }

    @Scheduled(fixedRate = 600000)
    @Transactional()
    public void deleteUnpaidOrders() {
        LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10);
        List<Order> unpaidOrders = orderRepository.findByStatusAndCreatedAtBefore(OrderStatus.NEW, tenMinutesAgo);

        for (Order order : unpaidOrders) {
            updateGoodsQuantities(order, true);
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
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
