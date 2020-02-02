package com.example.demo.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.java.dto.Inventory;

public class InventoryQueryRepositoryImpl implements InventoryQueryRepsoitoryCustom{

	@Autowired 
	private  MongoTemplate mongoTemplate;
	public void updateInventory(Inventory inventory) {
		Query query = new Query(Criteria.where("inventoryId").is(inventory.getInventoryId()).and("productId").is(inventory.getProductId()));
		Update update = new Update();
		update.inc("quantity", -inventory.getQuantity());
		mongoTemplate.updateFirst(query, update, Inventory.class);
	}
}
