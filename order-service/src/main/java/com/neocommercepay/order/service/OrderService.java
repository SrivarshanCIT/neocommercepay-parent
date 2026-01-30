package com.neocommercepay.order.service;

import com.neocommercepay.common.exception.NotFoundException;
import com.neocommercepay.order.entity.Order;
import com.neocommercepay.order.entity.OrderItem;
import com.neocommercepay.order.entity.OrderStatusHistory;
import com.neocommercepay.order.event.OrderEventProducer;
import com.neocommercepay.order.repository.OrderRepository;
import com.neocommercepay.order.repository.OrderStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository statusHistoryRepository;
    private final OrderEventProducer orderEventProducer;

    @Transactional
    public Order createOrder(Long userId, List<OrderItem> items) {
        BigDecimal totalAmount = items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = Order.builder()
                .userId(userId)
                .totalAmount(totalAmount)
                .status(Order.OrderStatus.PENDING)
                .items(items)
                .build();

        items.forEach(item -> item.setOrder(order));

        Order savedOrder = orderRepository.save(order);

        OrderStatusHistory history = OrderStatusHistory.builder()
                .orderId(savedOrder.getId())
                .newStatus(Order.OrderStatus.PENDING)
                .build();
        statusHistoryRepository.save(history);

        orderEventProducer.publishOrderCreated(savedOrder);
        log.info("Order created: {}", savedOrder.getId());

        return savedOrder;
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found with id: " + id));
    }

    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    public List<Order> getOrdersByStatus(Order.OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    @Transactional
    public Order updateOrderStatus(Long id, Order.OrderStatus newStatus) {
        Order order = getOrderById(id);
        Order.OrderStatus oldStatus = order.getStatus();

        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);

        OrderStatusHistory history = OrderStatusHistory.builder()
                .orderId(order.getId())
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .build();
        statusHistoryRepository.save(history);

        orderEventProducer.publishOrderUpdated(updatedOrder, oldStatus, newStatus);
        log.info("Order {} status updated: {} -> {}", id, oldStatus, newStatus);

        return updatedOrder;
    }

    @Transactional
    public Order cancelOrder(Long id, String reason) {
        Order order = getOrderById(id);

        if (order.getStatus() == Order.OrderStatus.SHIPPED ||
            order.getStatus() == Order.OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot cancel order in status: " + order.getStatus());
        }

        Order.OrderStatus oldStatus = order.getStatus();
        order.setStatus(Order.OrderStatus.CANCELLED);
        Order cancelledOrder = orderRepository.save(order);

        OrderStatusHistory history = OrderStatusHistory.builder()
                .orderId(order.getId())
                .oldStatus(oldStatus)
                .newStatus(Order.OrderStatus.CANCELLED)
                .build();
        statusHistoryRepository.save(history);

        orderEventProducer.publishOrderCancelled(order.getId(), order.getUserId(), reason);
        log.info("Order cancelled: {}", id);

        return cancelledOrder;
    }

    public List<OrderStatusHistory> getOrderHistory(Long orderId) {
        return statusHistoryRepository.findByOrderId(orderId);
    }
}
