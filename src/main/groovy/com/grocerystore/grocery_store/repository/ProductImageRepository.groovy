package com.grocerystore.grocery_store.repository

import com.grocerystore.grocery_store.model.ProductImage
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    // product_images.product_id exists in the database (with a foreign key to products)
    // but is not mapped on the entity, so it has to be cleaned up with native SQL.

    /** Deletes every image row that belongs to the product, plus the given image id. */
    @Modifying
    @Query(value = "DELETE FROM product_images WHERE product_id = :productId OR id = :imageId", nativeQuery = true)
    int deleteForProduct(@Param("productId") Long productId, @Param("imageId") Long imageId)
}
