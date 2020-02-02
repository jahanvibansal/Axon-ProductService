package com.example.demo.handlers;

import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.dao.InventoryQueryRepository;
import com.example.demo.events.InventoryInsertedEvent;
import com.example.demo.events.InventoryUpdatedEvent;
import com.java.dto.Inventory;

@Service
public class InventoryEventHandler {

	@Autowired InventoryQueryRepository rep;
	
	@EventHandler
	void handleInventoryUpdates (InventoryUpdatedEvent event) {
			rep.updateInventory(new Inventory(event.getProductId(),event.getInventoryId(), event.getQuantity()));
	}
	
	@EventHandler
	void handleInventoryUpdates (InventoryInsertedEvent event) {
			rep.save(new Inventory(event.getProductId(),event.getInventoryId(), event.getSku(), event.getQuantity()));
	}
}
