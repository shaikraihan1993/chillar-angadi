package com.grocerystore.grocery_store.controller

import com.grocerystore.grocery_store.model.enums.ProductCategory
import com.grocerystore.grocery_store.model.enums.ProductSubCategory
import com.grocerystore.grocery_store.model.enums.ProductUnit
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/enums")
@Tag(name = "Enums", description = "API for retrieving enum values")
class EnumController {

    @GetMapping("/product-categories")
    @Operation(summary = "Get all product categories")
    Map<String, Object> getProductCategories() {
        return ProductCategory.values().collectEntries { enumValue ->
            [(enumValue.name()): [
                displayName: enumValue.displayName,
                name: enumValue.name(),
                value: enumValue.toString()
            ]]
        }
    }

    @GetMapping("/product-subcategories")
    @Operation(summary = "Get all product subcategories")
    Map<String, Object> getProductSubcategories() {
        return ProductSubCategory.values().collectEntries { enumValue ->
            [(enumValue.name()): [
                displayName: enumValue.displayName,
                name: enumValue.name(),
                value: enumValue.toString(),
                category: enumValue.category.name()
            ]]
        }
    }

    @GetMapping("/product-units")
    @Operation(summary = "Get all product units")
    Map<String, Object> getProductUnits() {
        return ProductUnit.values().collectEntries { enumValue ->
            [(enumValue.name()): [
                displayName: enumValue.displayName,
                name: enumValue.name(),
                value: enumValue.toString(),
                code: enumValue.code
            ]]
        }
    }
}
