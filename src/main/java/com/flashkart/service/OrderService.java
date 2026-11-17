package com.flashkart.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.flashkart.model.Order;
import com.flashkart.model.OrderStatus;
import com.flashkart.repository.OrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

	private final IdempotencyService idempotencyService;
	private final OrderRepository orderRepository;

	public IdempotencyService.Resolution createOrder(String idempotencyKey) {
		return idempotencyService.getOrCreate(idempotencyKey, () -> {
			Order order = new Order(UUID.randomUUID().toString(), OrderStatus.CREATED);
			return orderRepository.save(order);
		});
	}

	public Optional<Order> getOrder(String orderId) {
		return orderRepository.findById(orderId);
	}
}
