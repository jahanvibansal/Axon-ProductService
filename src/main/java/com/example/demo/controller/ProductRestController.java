package com.example.demo.controller;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.axonframework.eventsourcing.eventstore.EventStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.demo.dao.InventoryQueryRepository;
import com.example.demo.dao.ProductRepository;
import com.java.dto.Inventory;
import com.java.dto.Product;

@RestController
public class ProductRestController {

	@Autowired
	private ProductRepository productRepository;
	@Autowired
	InventoryQueryRepository rep;
	@Autowired
	EventStore store;

	private static final Logger LOGGER = LoggerFactory.getLogger(ProductRestController.class);

	// ------------------- Update a Product
	// --------------------------------------------------------
	@RequestMapping(value = "/products/{id}", method = RequestMethod.PUT)
	public ResponseEntity<Resource<Product>> updateProduct(@PathVariable("id") String id,
			@RequestBody Product product) {
		LOGGER.debug("Updating Product with id: " + id);

		Optional<Product> currentProduct = productRepository.findById(id);

		if (!currentProduct.isPresent()) {
			LOGGER.debug("Product with id: " + id + " not found");
			return new ResponseEntity<Resource<Product>>(HttpStatus.NOT_FOUND);
		}
		Product prod = currentProduct.get();
		prod.setName(product.getName());
		prod.setCode(product.getCode());
		prod.setTitle(product.getTitle());
		prod.setDescription(product.getDescription());
		prod.setImgUrl(product.getImgUrl());
		prod.setPrice(product.getPrice());
		prod.setProductCategoryName(product.getProductCategoryName());

		Product newProduct = productRepository.save(prod);

		Resource<Product> productRes = new Resource<Product>(newProduct,
				linkTo(methodOn(ProductRestController.class).getProduct(newProduct.getId())).withSelfRel());
		return new ResponseEntity<Resource<Product>>(productRes, HttpStatus.OK);
	}

	// ------------------- Create a Product
	// --------------------------------------------------------
	@RequestMapping(value = "/products", method = RequestMethod.POST)
	public ResponseEntity<Resource<Product>> postProduct(@RequestBody Product product, UriComponentsBuilder ucBuilder) {
		LOGGER.debug("Creating Product with code: " + product.getCode());

		List<Product> products = productRepository.findByCode(product.getCode());
		if (products.size() > 0) {
			LOGGER.debug("A Product with code " + product.getCode() + " already exist");
			return new ResponseEntity<Resource<Product>>(HttpStatus.CONFLICT);
		}

		Product newProduct = productRepository.save(product);

		HttpHeaders headers = new HttpHeaders();
		headers.setLocation(ucBuilder.path("/products/{id}").buildAndExpand(product.getId()).toUri());
		Resource<Product> productRes = new Resource<Product>(newProduct,
				linkTo(methodOn(ProductRestController.class).getProduct(newProduct.getId())).withSelfRel());
		// return new ResponseEntity<Void>(headers, HttpStatus.CREATED);
		return new ResponseEntity<Resource<Product>>(productRes, headers, HttpStatus.OK);
	}

	// ------------------- Retreive a Product
	// --------------------------------------------------------
	@RequestMapping(value = "/products/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Resource<Product>> getProduct(@PathVariable("id") String id) {

		LOGGER.debug("Fetching Product with id " + id);
		Optional<Product> product = productRepository.findById(id);
		if (!product.isPresent()) {
			LOGGER.debug("Product with id " + id + " not found");
			return new ResponseEntity<Resource<Product>>(HttpStatus.NOT_FOUND);
		}
		Product prod = product.get();
		Resource<Product> productRes = new Resource<Product>(prod, new Link[] {
				linkTo(methodOn(ProductRestController.class).getProduct(prod.getId())).withSelfRel(),
				linkTo(ProductRestController.class).slash("productImg").slash(prod.getImgUrl()).withRel("imgUrl") });
		Optional<Inventory> i = rep.findByProductId(id);
		System.out.println(i.get().getQuantity());
		System.out.println(store.readEvents(i.get().getInventoryId()).asStream().map(s -> s.getPayload())
				.collect(Collectors.toList()));

		return new ResponseEntity<Resource<Product>>(productRes, HttpStatus.OK);
	}

	// ------------------- Retreive all Products
	// --------------------------------------------------------
	@RequestMapping(value = "/products", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<Resources<Resource<Product>>> getAllProducts() {

		LOGGER.debug("ProductRestController.getAllProducts : Start");
		List<Product> products = productRepository.findAll();
		Link links[] = { linkTo(methodOn(ProductRestController.class).getAllProducts()).withSelfRel(),
				linkTo(methodOn(ProductRestController.class).getAllProducts()).withRel("getAllProducts"),
				linkTo(methodOn(ProductRestController.class).getAllProductsByCategory(""))
						.withRel("getAllProductsByCategory"),
				linkTo(methodOn(ProductRestController.class).getAllProductsByName(""))
						.withRel("getAllProductsByName") };
		if (products.isEmpty()) {
			LOGGER.debug("ProductRestController.getAllProducts : products.isEmpty()...");
			return new ResponseEntity<Resources<Resource<Product>>>(HttpStatus.NOT_FOUND);
		}
		List<Resource<Product>> list = new ArrayList<Resource<Product>>();
		addLinksToProduct(products, list);
		Resources<Resource<Product>> productRes = new Resources<Resource<Product>>(list, links);// ,
		LOGGER.debug("ProductRestController.getAllProducts : Ending...");
		return new ResponseEntity<Resources<Resource<Product>>>(productRes, HttpStatus.OK);
	}

	// ------------------- Delete a Product
	// --------------------------------------------------------
	@RequestMapping(value = "/products/{id}", method = RequestMethod.DELETE)
	public ResponseEntity<Product> deleteProduct(@PathVariable("id") String id) {

		LOGGER.debug("Fetching & Deleting Product with id: " + id);
		Optional<Product> product = productRepository.findById(id);
		if (!product.isPresent()) {
			LOGGER.debug("Unable to delete. Product with id: " + id + " not found");
			return new ResponseEntity<Product>(HttpStatus.NOT_FOUND);
		}

		productRepository.deleteById(id);
		return new ResponseEntity<Product>(HttpStatus.NO_CONTENT);
	}

	// ------------------- Delete All Products
	// --------------------------------------------------------
	@RequestMapping(value = "/products", method = RequestMethod.DELETE)
	public ResponseEntity<Product> deleteAllProducts() {

		long count = productRepository.count();
		LOGGER.debug("Deleting " + count + " products");
		productRepository.deleteAll();
		return new ResponseEntity<Product>(HttpStatus.NO_CONTENT);
	}

	// ------------------- Retreive all Products by category
	// --------------------------------------------------------
	@RequestMapping(value = "/productsByCategory", method = RequestMethod.GET, produces = {
			MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<Resources<Resource<Product>>> getAllProductsByCategory(
			@RequestParam("category") String category) {

		LOGGER.debug("ProductRestController.getAllProductsByCategory : Start with param " + category);
		List<Product> products = productRepository.findByProductCategoryName(category);
		Link links[] = {
				// linkTo(methodOn(ProductRestController.class)).slash("/products/category/").withSelfRel()
		};
		if (products.isEmpty()) {
			LOGGER.debug("ProductRestController.getAllProductsByCategory : products.isEmpty()...");
			return new ResponseEntity<Resources<Resource<Product>>>(HttpStatus.NOT_FOUND);
		}
		List<Resource<Product>> list = new ArrayList<Resource<Product>>();
		addLinksToProduct(products, list);
		Resources<Resource<Product>> productRes = new Resources<Resource<Product>>(list, links);// ,
		LOGGER.debug("ProductRestController.getAllProductsByCategory : Ending...");
		return new ResponseEntity<Resources<Resource<Product>>>(productRes, HttpStatus.OK);
	}

	private void addLinksToProduct(List<Product> products, List<Resource<Product>> list) {
		for (Product product : products) {
			list.add(new Resource<Product>(product,
					new Link[] {
							linkTo(methodOn(ProductRestController.class).getProduct(product.getId())).withSelfRel(),
							linkTo(ProductRestController.class).slash("productImg").slash(product.getImgUrl())
									.withRel("imgUrl") }));
		}
	}

	// ------------------- Retreive all Products by name like % %
	// --------------------------------------------------------
	@RequestMapping(value = "/productsByName", method = RequestMethod.GET, produces = {
			MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<Resources<Resource<Product>>> getAllProductsByName(@RequestParam("name") String name) {

		LOGGER.debug("ProductRestController.getAllProductsByName : Start with param " + name);
		List<Product> products = productRepository.findByNameRegex(name);
		Link links[] = {
				// linkTo(methodOn(ProductRestController.class)).slash("/products/category/").withSelfRel()
		};
		if (products.isEmpty()) {
			LOGGER.debug("ProductRestController.getAllProductsByName : products.isEmpty()...");
			return new ResponseEntity<Resources<Resource<Product>>>(HttpStatus.NOT_FOUND);
		}
		List<Resource<Product>> list = new ArrayList<Resource<Product>>();
		addLinksToProduct(products, list);
		Resources<Resource<Product>> productRes = new Resources<Resource<Product>>(list, links);// ,
		LOGGER.debug("ProductRestController.getAllProductsByName : Ending...");
		return new ResponseEntity<Resources<Resource<Product>>>(productRes, HttpStatus.OK);
	}
	// ------------------- Retreiving product image
	// --------------------------------------------------------

}
