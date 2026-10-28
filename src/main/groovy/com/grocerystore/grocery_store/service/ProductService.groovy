package com.grocerystore.grocery_store.service

import com.grocerystore.grocery_store.exception.ResourceNotFoundException
import com.grocerystore.grocery_store.model.Product
import com.grocerystore.grocery_store.model.ProductImage
import com.grocerystore.grocery_store.repository.ProductImageRepository
import com.grocerystore.grocery_store.repository.ProductRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import java.util.NoSuchElementException

@Service
class ProductService {

    private final ProductRepository productRepository
    private final ProductImageRepository productImageRepository

    ProductService(ProductRepository productRepository, ProductImageRepository productImageRepository) {
        this.productRepository = productRepository
        this.productImageRepository = productImageRepository
    }


    @Transactional
    Product create(Product product) {
        // If imageId is set, fetch and associate the ProductImage
        if (product.imageId) {
            ProductImage image = findImageById(product.imageId)
            if (image) {
                product.image = image
            }
        }
        productRepository.save(product)
    }

    Page<Product> findAll(Pageable pageable) {
        productRepository.findAllWithImages(pageable)
    }

    List<Product> findAll() {
        productRepository.findAll()
    }

    /**
     * Find products with filters
     */
    Page<Product> findWithFilters(String name, List<String> categories, List<String> brands, boolean inStockOnly, Pageable pageable) {
        return productRepository.findWithFilters(
            name?.trim() ?: null,
            categories?.isEmpty() ? null : categories,
            brands?.isEmpty() ? null : brands,
            inStockOnly,
            pageable
        )
    }

    /**
     * Get all unique brands
     */
    List<String> findAllBrands() {
        return productRepository.findAllBrands()
    }
    
    /**
     * Search products by name with pagination
     * @param name Product name or part of name to search for
     * @param pageable Pagination information
     * @return Page of matching products
     */
    Page<Product> findByNameContaining(String name, Pageable pageable) {
        if (name == null || name.trim().isEmpty()) {
            return findAll(pageable)
        }
        return productRepository.findByNameContainingIgnoreCase(name, pageable)
    }
    
    /**
     * Check if a product has an image
     * @param product The product to check
     * @return true if the product has an image, false otherwise
     */
    boolean hasImage(Product product) {
        return product.image != null
    }
    
    /**
     * Find a product by its ID
     * @param id The ID of the product to find
     * @return The found product
     * @throws ResourceNotFoundException if no product is found
     */
    @Transactional(readOnly = true)
    Product findById(Long id) {
        return productRepository.findById(id)
            .orElseThrow({ new ResourceNotFoundException("Product", "id", id) })
    }
    
    /**
     * Find a product by its associated image ID
     * @param imageId The ID of the image
     * @return The product that has the specified image, or null if not found
     */
    @Transactional(readOnly = true)
    Product findByImageId(Long imageId) {
        return productRepository.findByImageId(imageId)
    }
    
    /**
     * Find an image by its ID
     * @param id The ID of the image to find
     * @return The found image, or null if not found
     */
    @Transactional(readOnly = true)
    ProductImage findImageById(Long id) {
        return productRepository.findImageById(id).orElse(null)
    }
    
    /**
     * Save a product
     * @param product The product to save
     * @return The saved product
     */
    @Transactional
    Product save(Product product) {
        return productRepository.save(product)
    }
    
    /**
     * Update a product with the given updates
     * @param id The ID of the product to update
     * @param updates Map containing the fields to update
     * @return The updated product
     */
    @Transactional
    Product updateProduct(Long id, Map<String, Object> updates) {
        Product existing = findById(id)
        
        // Only update fields that are present in the updates map
        if (updates.name != null) {
            existing.name = updates.name
        }
        if (updates.description != null) {
            existing.description = updates.description
        }
        if (updates.price != null) {
            existing.price = new BigDecimal(updates.price.toString())
        }
        if (updates.quantity != null) {
            existing.quantity = updates.quantity as Integer
        }
        
        // Handle image update if imageId is provided
        if (updates.imageId != null) {
            def imageId = updates.imageId as Long
            def image = productRepository.findImageById(imageId)
                .orElseThrow { new NoSuchElementException("Image with ID ${imageId} not found") }
            existing.image = image
        }
        
        return save(existing)
        
        return productRepository.save(product)
    }

    /**
     * Deletes a product and its images. Past orders are unaffected: order lines keep
     * their own copy of the product name and price.
     */
    @Transactional
    void delete(Long id) {
        Product product = findById(id)   // throws ResourceNotFoundException if missing
        Long imageId = product.image?.id
        // products.image_id -> product_images and product_images.product_id -> products point at
        // each other, so unlink the image, remove the image rows, then remove the product.
        product.image = null
        productRepository.saveAndFlush(product)
        productImageRepository.deleteForProduct(id, imageId ?: -1L)
        productRepository.delete(product)
    }
}
