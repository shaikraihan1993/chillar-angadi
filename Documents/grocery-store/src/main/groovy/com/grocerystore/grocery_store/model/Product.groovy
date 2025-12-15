package com.grocerystore.grocery_store.model

import com.grocerystore.grocery_store.model.enums.ProductCategory
import com.grocerystore.grocery_store.model.enums.ProductSubCategory
import com.grocerystore.grocery_store.model.enums.ProductUnit
import jakarta.persistence.*
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

@Entity
@Table(name = "products")
class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id

    @NotBlank(message = "Product name is required")
    @Size(max = 100, message = "Name must be less than 100 characters")
    @Column(nullable = false)
    String name

    @Column(columnDefinition = "TEXT")
    String description

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Column(nullable = false, precision = 10, scale = 2)
    BigDecimal price

    @Column(nullable = false)
    Integer quantity = 0

    @NotBlank(message = "Brand is required")
    @Size(max = 50, message = "Brand must be less than 50 characters")
    @Column(nullable = false)
    String brand

    @Size(max = 100, message = "Manufacturer must be less than 100 characters")
    String manufacturer

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    ProductCategory category

    @Enumerated(EnumType.STRING)
    @Column(name = "sub_category", nullable = false)
    ProductSubCategory subCategory

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    ProductUnit unit = ProductUnit.PIECES

    @Column(name = "is_active", nullable = false)
    boolean isActive = true

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "image_id")
    ProductImage image

    @Transient
    Long imageId
    
    @Transient
    Long getImageId() {
        return image?.id ?: imageId
    }
    
    // Helper method to get the display name of the unit
    String getDisplayUnit() {
        return unit?.displayName ?: ''
    }
    
    // Helper method to get the display name of the category
    String getCategoryName() {
        return category?.displayName ?: ''
    }
    
    // Helper method to get the display name of the subcategory
    String getSubCategoryName() {
        return subCategory?.displayName ?: ''
    }
}
