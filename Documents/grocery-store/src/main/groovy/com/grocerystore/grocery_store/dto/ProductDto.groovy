package com.grocerystore.grocery_store.dto

import com.grocerystore.grocery_store.model.Product

class ProductDto {
    Long id
    String name
    String description
    BigDecimal price
    Integer quantity
    String imageUrl
    
    static ProductDto from(Product product) {
        if (!product) return null
        
        new ProductDto(
            id: product.id,
            name: product.name,
            description: product.description,
            price: product.price,
            quantity: product.quantity,
            imageUrl: product.mainImageUrl
        )
    }
    
    static List<ProductDto> fromList(List<Product> products) {
        products?.collect { from(it) } ?: []
    }
}
