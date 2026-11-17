package com.flashkart.controller;

import java.net.URI;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flashkart.model.Order;
import com.flashkart.service.IdempotencyService;
import com.flashkart.service.OrderService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

	private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";
	private static final String IDEMPOTENT_REPLAY_HEADER = "X-Idempotent-Replay";

	private final OrderService orderService;

	@PostMapping
	public ResponseEntity<Order> createOrder(
			@RequestHeader(value = IDEMPOTENCY_KEY_HEADER, required = false) String idempotencyKey) {
		if (idempotencyKey == null || idempotencyKey.isBlank()) {
			return ResponseEntity.badRequest().build();
		}

		IdempotencyService.Resolution resolution = orderService.createOrder(idempotencyKey);
		if (resolution.replay()) {
			return ResponseEntity.ok()
					.header(IDEMPOTENT_REPLAY_HEADER, "true")
					.body(resolution.order());
		}

		URI location = URI.create("/api/v1/orders/" + resolution.order().getOrderId());
		return ResponseEntity.created(location).body(resolution.order());
	}

	@GetMapping("/{orderId}")
	public ResponseEntity<Order> getOrder(@PathVariable String orderId) {
		return orderService.getOrder(orderId)
				.map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
	}
}
