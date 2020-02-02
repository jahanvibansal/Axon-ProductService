package com.example.demo.dao;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.java.dto.Inventory;

public interface InventoryQueryRepository extends MongoRepository<Inventory, String>, InventoryQueryRepsoitoryCustom{

	
	public Optional<Inventory> findByProductId(String productId);
}
