package com.grocerystore.grocery_store.repository

import com.grocerystore.grocery_store.model.Brand
import org.springframework.data.jpa.repository.JpaRepository

interface BrandRepository extends JpaRepository<Brand, Long> {
    List<Brand> findAllByOrderByNameAsc()
    Optional<Brand> findByNameIgnoreCase(String name)
}
