package com.grocerystore.grocery_store.model

import com.fasterxml.jackson.annotation.JsonManagedReference
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.*
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

@Entity
@Table(name = "products")
@Schema(description = "Represents a product in the grocery store inventory")
class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "The unique identifier of the product", example = "1", readOnly = true)
    Long id

    @NotBlank(message = "Product name is required")
    @Column(nullable = false)
    @Schema(description = "Name of the product", example = "Organic Apples", required = true)
    String name

    @Schema(description = "Detailed description of the product", example = "Fresh organic apples from local farms")
    String description

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Column(nullable = false)
    @Schema(description = "Price of the product in USD", example = "2.99", required = true)
    BigDecimal price

    @Schema(description = "Available quantity in stock", example = "100", defaultValue = "0")
    Integer quantity

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "image_id")
    @Schema(description = "Product image")
    ProductImage image
    
    // Helper method to get the image URL
    @Transient
    @Schema(description = "URL of the product image")
    String getMainImageUrl() {
        image?.imageUrl
    }
}

//
//@Entity
//@Table(name = "products")
//class Product {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    Long id
//
//    @NotBlank(message = "Product name is required")
//    @Column(nullable = false)
