package com.example.demo.events;

import lombok.Data;

@Data
public class InventoryUpdatedEvent {

		private final String productId;
		
		private String inventoryId;

		private final Integer quantity;

}
