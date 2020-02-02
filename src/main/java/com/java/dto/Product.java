
package com.java.dto;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document
public class Product {

	@Id
	private String id;
	private String name;
	private String code;
	private String title;
	private String description;
	private String imgUrl;
	private Double price;
	private String productCategoryName;

	
}
