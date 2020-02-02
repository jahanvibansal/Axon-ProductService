package com.example.demo.events;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class InventoryInsertedEvent {
	private final String productId;
	
	private String inventoryId;

	private final String sku;

	private final Integer quantity;

}