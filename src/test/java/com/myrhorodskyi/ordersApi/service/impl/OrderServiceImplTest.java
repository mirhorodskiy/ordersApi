package com.myrhorodskyi.ordersApi.service.impl;

import com.myrhorodskyi.ordersApi.dto.OrderItemRequest;
import com.myrhorodskyi.ordersApi.dto.OrderRequest;
import com.myrhorodskyi.ordersApi.model.entity.Goods;
import com.myrhorodskyi.ordersApi.model.entity.Order;
import com.myrhorodskyi.ordersApi.model.entity.OrderItem;
import com.myrhorodskyi.ordersApi.model.enums.OrderStatus;
import com.myrhorodskyi.ordersApi.repository.GoodsRepository;
import com.myrhorodskyi.ordersApi.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;


@ExtendWith(SpringExtension.class)
@SpringBootTest
class OrderServiceImplTest {

    private static final long orderId = 1L;

    @Mock
    OrderRepository orderRepository;

    @Mock
    GoodsRepository goodsRepository;

    @InjectMocks
    OrderServiceImpl orderService;

    @Test
    void getAllOrders_ShouldReturnListOfOrders() {
        List<Order> expectedOrderList = Arrays.asList(
                new Order(1L, "John Doe", 100.0, OrderStatus.NEW, LocalDateTime.now(), new ArrayList<>()),
                new Order(2L, "Jane Doe", 150.0, OrderStatus.PAID, LocalDateTime.now(), new ArrayList<>())
        );

        when(orderRepository.findAll()).thenReturn(expectedOrderList);

        List<Order> actualOrderList = orderService.getAllOrders();
        assertEquals(expectedOrderList, actualOrderList);
    }

    @Test
    void getOrderById_ShouldReturnOrder() {
        Order expectedOrder = new Order(orderId, "John Doe", 100.0, OrderStatus.NEW, LocalDateTime.now(), new ArrayList<>());

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(expectedOrder));

        Order actualOrder = orderService.getOrderById(orderId);
        assertEquals(expectedOrder, actualOrder);
    }

    @Test
    void cancelOrder_ShouldCancelOrder() {
        Order order = new Order(orderId, "John Doe", 100.0, OrderStatus.NEW, LocalDateTime.now(), new ArrayList<>());

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        orderService.cancelOrder(orderId);
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    void payOrder_ShouldPayOrder() {
        Order order = new Order(orderId, "John Doe", 100.0, OrderStatus.NEW, LocalDateTime.now(), new ArrayList<>());

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        orderService.payOrder(orderId);
        assertEquals(OrderStatus.PAID, order.getStatus());
    }

    @Test
    void createOrder_ShouldCreateNewOrder() {
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setCustomerName("John Doe");

        OrderItemRequest orderItemRequest = new OrderItemRequest();
        orderItemRequest.setGoodsId(1L);
        orderItemRequest.setQuantity(2);

        orderRequest.setOrderItems(Collections.singletonList(orderItemRequest));

        Goods goods = new Goods();
        goods.setId(1L);
        goods.setName("Iphone");
        goods.setPrice(10.0);
        goods.setQuantity(5);

        when(goodsRepository.findById(1L)).thenReturn(Optional.of(goods));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.setId(1L);
            return savedOrder;
        });

        Order createdOrder = orderService.createOrder(orderRequest);

        assertNotNull(createdOrder);
        assertEquals("John Doe", createdOrder.getCustomerName());
        assertEquals(OrderStatus.NEW, createdOrder.getStatus());
        assertEquals(20.0, createdOrder.getTotalAmount());

        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void deleteUnpaidOrders_ShouldDeleteUnpaidOrders() {
        LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10);

        Goods goods = new Goods();
        goods.setId(1L);
        goods.setName("Product");
        goods.setPrice(10.0);
        goods.setQuantity(5);

        OrderItem orderItem = new OrderItem();
        orderItem.setId(1L);
        orderItem.setGoods(goods);
        orderItem.setQuantity(2);

        Order order = new Order();
        order.setId(1L);
        order.setCustomerName("John Doe");
        order.setTotalAmount(20.0);
        order.setStatus(OrderStatus.NEW);
        order.setCreatedAt(tenMinutesAgo);
        order.setOrderItems(List.of(orderItem));

        List<Order> unpaidOrders = List.of(order);

        when(orderRepository.findByStatusAndCreatedAtBefore(OrderStatus.NEW, tenMinutesAgo)).thenReturn(unpaidOrders);

        orderService.deleteUnpaidOrders();

        verify(orderRepository, times(1)).findByStatusAndCreatedAtBefore(OrderStatus.NEW, tenMinutesAgo);

        verify(orderRepository, times(1)).save(order);

        verify(goodsRepository, times(1)).save(goods);
    }
}