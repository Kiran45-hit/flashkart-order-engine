package com.flashkart.repository;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Repository;

import com.flashkart.model.Order;

@Repository
public class OrderRepository {

	private final ConcurrentMap<String, Order> orders = new ConcurrentHashMap<>();

	public Order save(Order order) {
		orders.put(order.getOrderId(), order);
		return order;
	}

	public Optional<Order> findById(String orderId) {
		return Optional.ofNullable(orders.get(orderId));
	}
}
