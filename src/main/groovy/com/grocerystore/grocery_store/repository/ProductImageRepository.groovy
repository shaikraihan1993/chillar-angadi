package com.grocerystore.grocery_store.repository

import com.grocerystore.grocery_store.model.ProductImage
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    // No custom methods needed as we're using the unidirectional relationship
    // from Product to ProductImage
}
