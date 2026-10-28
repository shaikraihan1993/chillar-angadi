package com.grocerystore.grocery_store.repository

import com.grocerystore.grocery_store.model.Manufacturer
import org.springframework.data.jpa.repository.JpaRepository

interface ManufacturerRepository extends JpaRepository<Manufacturer, Long> {
    List<Manufacturer> findAllByOrderByNameAsc()
    Optional<Manufacturer> findByNameIgnoreCase(String name)
}
