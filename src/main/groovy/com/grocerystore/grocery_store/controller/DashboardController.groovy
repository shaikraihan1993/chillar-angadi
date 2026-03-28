package com.grocerystore.grocery_store.controller

import com.grocerystore.grocery_store.repository.OrderRepository
import com.grocerystore.grocery_store.repository.ProductRepository
import com.grocerystore.grocery_store.repository.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/dashboard")
class DashboardController {

    private final OrderRepository orderRepository
    private final ProductRepository productRepository
    private final UserRepository userRepository

    DashboardController(OrderRepository orderRepository, ProductRepository productRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository
        this.productRepository = productRepository
        this.userRepository = userRepository
    }

    @GetMapping("/stats")
    ResponseEntity<Map<String, Object>> getStats() {
        try {
            def stats = [
                totalOrders: orderRepository.count(),
                totalProducts: productRepository.count(),
                totalUsers: userRepository.count()
            ]
            new ResponseEntity<>(stats, HttpStatus.OK)
        } catch (Exception e) {
            new ResponseEntity<>([error: e.message], HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}
