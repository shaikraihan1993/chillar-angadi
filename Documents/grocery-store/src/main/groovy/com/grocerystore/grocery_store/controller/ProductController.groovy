package com.grocerystore.grocery_store.controller

import com.grocerystore.grocery_store.dto.ProductDto
import com.grocerystore.grocery_store.model.Product
import com.grocerystore.grocery_store.model.ProductImage
import com.grocerystore.grocery_store.service.ProductService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/products")
class ProductController {

    private final ProductService productService

    ProductController(ProductService productService) {
        this.productService = productService
    }

    @PostMapping
    ResponseEntity<?> create(@RequestBody Product product) {
        try {
            if (!product.name || product.price == null || product.quantity == null) {
                return new ResponseEntity<>([error: 'Name, price, and quantity are required'], HttpStatus.BAD_REQUEST)
            }
            def created = productService.create(product)
            return new ResponseEntity<>(ProductDto.from(created), HttpStatus.CREATED)
        } catch (Exception e) {
            return new ResponseEntity<>([error: e.message ?: 'Failed to create product'], HttpStatus.BAD_REQUEST)
        }
    }

    @GetMapping
    ResponseEntity<Map<String, Object>> listProducts(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size)
            Page<Product> result = name ? 
                productService.findByNameContaining(name, pageable) : 
                productService.findAll(pageable)

            def response = [
                products: ProductDto.fromList(result.content),
                currentPage: result.number,
                totalItems: result.totalElements,
                totalPages: result.totalPages
            ]
            
            new ResponseEntity<>(response, HttpStatus.OK)
        } catch (Exception e) {
            new ResponseEntity<>([error: e.message], HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @GetMapping("/{id}")
    ResponseEntity<ProductDto> getProduct(@PathVariable Long id) {
        try {
            def product = productService.findById(id)
            new ResponseEntity<>(ProductDto.from(product), HttpStatus.OK)
        } catch (NoSuchElementException e) {
            new ResponseEntity<>(HttpStatus.NOT_FOUND)
        } catch (Exception e) {
            new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @PutMapping("/{id}")
    ResponseEntity<?> updateProduct(
            @PathVariable Long id,
            @RequestBody Map updates
    ) {
        try {
            // Remove imageUrl from updates if it exists since it's read-only
            String imageUrl = updates.remove('imageUrl')

            // If imageUrl was provided, extract the image ID and update the product's image
            if (imageUrl) {
                try {
                    // Extract the image ID from the URL (assuming format: /api/v1/images/75)
                    Long imageId = imageUrl.split('/').last() as Long
                    ProductImage image = productService.findImageById(imageId)

                    if (image == null) {
                        return new ResponseEntity<>([error: "Image with ID ${imageId} not found"], HttpStatus.NOT_FOUND)
                    }

                    // Set the image on the product
                    def product = productService.findById(id)
                    product.image = image
                    productService.save(product)
                } catch (NumberFormatException e) {
                    return new ResponseEntity<>([error: "Invalid image URL format"], HttpStatus.BAD_REQUEST)
                } catch (Exception e) {
                    return new ResponseEntity<>([error: "Error updating product image: ${e.message}"], HttpStatus.INTERNAL_SERVER_ERROR)
                }
            }

            // Update other product properties
            if (!updates.isEmpty()) {
                try {
                    def product = productService.findById(id)
                    updates.each { key, value ->
                        if (value != null && key != 'id') { // Prevent changing the ID
                            product[key] = value
                        }
                    }
                    productService.save(product)
                } catch (Exception e) {
                    return new ResponseEntity<>([error: "Error updating product: ${e.message}"], HttpStatus.BAD_REQUEST)
                }
            }

            def updatedProduct = productService.findById(id)
            return new ResponseEntity<>(ProductDto.from(updatedProduct), HttpStatus.OK)
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>([error: "Product not found: ${e.message}"], HttpStatus.NOT_FOUND)
        } catch (Exception e) {
            return new ResponseEntity<>([error: "Failed to update product: ${e.message}"], HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        try {
            productService.delete(id)
            new ResponseEntity<>(HttpStatus.NO_CONTENT)
        } catch (NoSuchElementException e) {
            new ResponseEntity<>(HttpStatus.NOT_FOUND)
        } catch (Exception e) {
            new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}
