package com.grocerystore.grocery_store.controller

import com.grocerystore.grocery_store.dto.CheckoutRequest
import com.grocerystore.grocery_store.model.Order
import com.grocerystore.grocery_store.repository.UserRepository
import com.grocerystore.grocery_store.service.CheckoutService

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import java.security.Principal

@RestController
@RequestMapping('/api/v1/checkout')
@Tag(name = 'Checkout', description = 'Checkout and order processing')
class CheckoutController {

    private final CheckoutService checkoutService
    private final UserRepository userRepository

    CheckoutController(CheckoutService checkoutService, UserRepository userRepository) {
        this.checkoutService = checkoutService
        this.userRepository = userRepository
    }

    @Operation(summary = 'Process checkout', description = 'Process the order and update inventory')
    @ApiResponse(responseCode = '200', description = 'Order processed successfully')
    @ApiResponse(responseCode = '400', description = 'Insufficient stock or invalid request')
    @PostMapping
    ResponseEntity<Map<String, Object>> checkout(@RequestBody CheckoutRequest request, Principal principal) {
        // Guests have no principal; signed-in customers get the order linked to their account
        def user = principal ? userRepository.findByUsername(principal.name).orElse(null) : null
        Order order = checkoutService.processCheckout(request, user)
        Map<String, Object> response = [
                orderId   : order.id,
                status    : order.status,
                orderTime : order.orderTime,
                totalAmount: order.totalAmount
        ]
        return ResponseEntity.ok(response)
    }

}
