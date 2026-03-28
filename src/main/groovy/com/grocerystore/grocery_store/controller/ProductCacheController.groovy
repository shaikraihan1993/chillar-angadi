package com.grocerystore.grocery_store.controller

import com.grocerystore.grocery_store.service.ProductCacheService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/cache/products")
class ProductCacheController {

    private final ProductCacheService productCacheService

    ProductCacheController(ProductCacheService productCacheService) {
        this.productCacheService = productCacheService
    }

    @PostMapping("/{id}")
    ResponseEntity<Void> saveProductName(
            @PathVariable("id") Long id,
            @RequestParam("name") String name
    ) {
        productCacheService.saveProductName(id, name)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/{id}")
    ResponseEntity<String> getProductName(
            @PathVariable("id") Long id
    ) {
        String name = productCacheService.getProductName(id)
        if (name == null) return ResponseEntity.notFound().build()
        return ResponseEntity.ok(name)
    }
}
