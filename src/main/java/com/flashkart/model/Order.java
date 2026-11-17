package com.flashkart.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Order {

	private final String orderId;
	private final OrderStatus status;
}
