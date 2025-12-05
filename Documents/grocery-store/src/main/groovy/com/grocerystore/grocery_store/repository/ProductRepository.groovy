package com.grocerystore.grocery_store.repository

import com.grocerystore.grocery_store.model.Product
import com.grocerystore.grocery_store.model.ProductImage
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional

interface ProductRepository extends JpaRepository<Product, Long> {
    // You already get: save, findById, findAll, deleteById, existsById, etc.
    
    /**
     * Find all products with their images using a custom query
     */
    @EntityGraph(attributePaths = ["image"])
    @Query("SELECT p FROM Product p")
    Page<Product> findAllWithImages(Pageable pageable)
    
    @Query("SELECT p FROM Product p WHERE p.image.id = :imageId")
    Product findByImageId(@Param("imageId") Long imageId)
    
    /**
     * Find a ProductImage by its ID
     * @param id The ID of the ProductImage to find
     * @return An Optional containing the ProductImage if found, or empty if not found
     */
    @Query("SELECT pi FROM ProductImage pi WHERE pi.id = :id")
    Optional<ProductImage> findImageById(@Param("id") Long id)
    
    /**
     * Search products by name with pagination (case-insensitive partial match)
     */
    @EntityGraph(attributePaths = ["image"])
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable)
}
