package com.grocerystore.grocery_store.dto

import com.grocerystore.grocery_store.model.Product

class ProductDto {
    Long id
    String name
    String description
    BigDecimal price
    Integer quantity
    String brand
    String manufacturer
    String category
    String subCategory
    String unit
    Boolean isActive
    Long imageId
    
    static ProductDto from(Product product) {
        if (!product) return null
        
        new ProductDto(
            id: product.id,
            name: product.name,
            description: product.description,
            price: product.price,
            quantity: product.quantity,
            brand: product.brand,
            manufacturer: product.manufacturer,
            category: product.category?.toString(),
            subCategory: product.subCategory?.toString(),
            unit: product.unit?.toString(),
            isActive: product.isActive,
            imageId: product.imageId
        )
    }
    
    static List<ProductDto> fromList(List<Product> products) {
        products?.collect { from(it) } ?: []
    }
}
