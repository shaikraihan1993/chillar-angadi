package com.grocerystore.grocery_store.repository

import com.grocerystore.grocery_store.model.Order
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByCustomerNameContainingIgnoreCaseOrPhoneNumberContainingIgnoreCase(String customerName, String phoneNumber, Pageable pageable)
}
