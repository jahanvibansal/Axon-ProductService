package com.java.dto;

import org.springframework.data.annotation.Id;

import lombok.Data;
@Data
public class ProductCategory {

	@Id
	private String id;
	private String name;
	private String title;
	private String description;
	private String imgUrl;



}