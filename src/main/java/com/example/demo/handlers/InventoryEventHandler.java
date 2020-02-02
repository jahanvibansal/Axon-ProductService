package com.example.demo.handlers;

import java.util.List;
import java.util.Optional;

import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.dao.InventoryQueryRepository;
import com.example.demo.events.FindAllEvent;
import com.example.demo.events.FindByInventoryIdEvent;
import com.example.demo.events.InventoryInsertedEvent;
import com.example.demo.events.InventoryUpdatedEvent;
import com.java.dto.Inventory;

@Service
public class InventoryEventHandler {

	@Autowired InventoryQueryRepository rep;
	
	@EventHandler
	public void handleInventoryUpdates (InventoryUpdatedEvent event) {
			rep.updateInventory(new Inventory(event.getProductId(),event.getInventoryId(), event.getQuantity()));
	}
	
	@EventHandler
	public void handleInventoryUpdates (InventoryInsertedEvent event) {
			rep.save(new Inventory(event.getProductId(),event.getInventoryId(), event.getSku(), event.getQuantity()));
	}
	
	@QueryHandler
	public Optional<Inventory> handleFindByInventoryId(FindByInventoryIdEvent event) {
		return rep.findById(event.getInventoryId());
	}
	
	@QueryHandler
	public List<Inventory> handleFindAll(FindAllEvent event) {
		return rep.findAll();
	}
}
