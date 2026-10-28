package com.grocerystore.grocery_store.controller

import com.grocerystore.grocery_store.model.Brand
import com.grocerystore.grocery_store.model.Manufacturer
import com.grocerystore.grocery_store.repository.BrandRepository
import com.grocerystore.grocery_store.repository.ManufacturerRepository
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Brand and manufacturer choices for the product form.
 * GET is public (under /api/v1/enums); POST falls under the admin-only rule.
 */
@RestController
@RequestMapping("/api/v1/enums")
@Tag(name = "Enums", description = "API for retrieving enum values")
class BrandManufacturerController {

    private final BrandRepository brandRepository
    private final ManufacturerRepository manufacturerRepository

    BrandManufacturerController(BrandRepository brandRepository, ManufacturerRepository manufacturerRepository) {
        this.brandRepository = brandRepository
        this.manufacturerRepository = manufacturerRepository
    }

    static class NameRequest {
        String name
    }

    @GetMapping("/brands")
    List<Map> brands() {
        brandRepository.findAllByOrderByNameAsc().collect { [id: it.id, name: it.name] }
    }

    @GetMapping("/manufacturers")
    List<Map> manufacturers() {
        manufacturerRepository.findAllByOrderByNameAsc().collect { [id: it.id, name: it.name] }
    }

    @PostMapping("/brands")
    ResponseEntity<?> addBrand(@RequestBody NameRequest body) {
        String name = clean(body?.name)
        if (!name) return ResponseEntity.badRequest().body([message: 'Enter a brand name (up to 50 characters)'])
        def existing = brandRepository.findByNameIgnoreCase(name)
        if (existing.present) return ResponseEntity.ok([id: existing.get().id, name: existing.get().name])
        def saved = brandRepository.save(new Brand(name: name))
        ResponseEntity.status(HttpStatus.CREATED).body([id: saved.id, name: saved.name])
    }

    @PostMapping("/manufacturers")
    ResponseEntity<?> addManufacturer(@RequestBody NameRequest body) {
        String name = clean(body?.name, 100)
        if (!name) return ResponseEntity.badRequest().body([message: 'Enter a manufacturer name (up to 100 characters)'])
        def existing = manufacturerRepository.findByNameIgnoreCase(name)
        if (existing.present) return ResponseEntity.ok([id: existing.get().id, name: existing.get().name])
        def saved = manufacturerRepository.save(new Manufacturer(name: name))
        ResponseEntity.status(HttpStatus.CREATED).body([id: saved.id, name: saved.name])
    }

    /** products.brand is varchar(50) and products.manufacturer varchar(100), so keep names within those. */
    private static String clean(String raw, int max = 50) {
        String name = raw?.trim()?.replaceAll(/\s+/, ' ')
        name && name.length() <= max ? name : null
    }
}
