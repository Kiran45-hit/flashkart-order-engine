package com.flashkart.controller;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void rejectsMissingOrBlankIdempotencyKey() throws Exception {
		mockMvc.perform(post("/api/v1/orders"))
				.andExpect(status().isBadRequest());

		mockMvc.perform(post("/api/v1/orders").header("Idempotency-Key", "   "))
				.andExpect(status().isBadRequest());
	}

	@Test
	void createsOrderAndReplaysForSameKey() throws Exception {
		String key = UUID.randomUUID().toString();

		MvcResult created = mockMvc.perform(post("/api/v1/orders")
						.header("Idempotency-Key", key))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status", is("CREATED")))
				.andReturn();
		String orderId = com.jayway.jsonpath.JsonPath.read(
				created.getResponse().getContentAsString(), "$.orderId");

		mockMvc.perform(post("/api/v1/orders").header("Idempotency-Key", key))
				.andExpect(status().isOk())
				.andExpect(header().string("X-Idempotent-Replay", "true"))
				.andExpect(jsonPath("$.orderId", is(orderId)));

		mockMvc.perform(get("/api/v1/orders/{orderId}", orderId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.orderId", is(orderId)))
				.andExpect(jsonPath("$.status", is("CREATED")));
	}

	@Test
	void returnsNotFoundForUnknownOrder() throws Exception {
		mockMvc.perform(get("/api/v1/orders/{orderId}", UUID.randomUUID()))
				.andExpect(status().isNotFound());
	}
}
