package com.flashkart.service;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.springframework.stereotype.Service;

import com.flashkart.model.Order;

@Service
public class IdempotencyService {

	private final Map<String, Order> ordersByKey = new HashMap<>();

	public synchronized Resolution getOrCreate(String key, Supplier<Order> orderSupplier) {
		Order existingOrder = ordersByKey.get(key);
		if (existingOrder != null) {
			return new Resolution(existingOrder, true);
		}

		Order newOrder = orderSupplier.get();
		ordersByKey.put(key, newOrder);
		return new Resolution(newOrder, false);
	}

	public record Resolution(Order order, boolean replay) {
	}
}
