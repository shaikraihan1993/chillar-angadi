package com.grocerystore.grocery_store.controller

import com.grocerystore.grocery_store.dto.UserProfileDto
import com.grocerystore.grocery_store.repository.OrderRepository
import com.grocerystore.grocery_store.repository.UserRepository
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

import java.security.Principal

/** The signed-in shopper's own profile and order history. */
@RestController
@RequestMapping('/api/account')
@Tag(name = 'Account', description = 'Signed-in customer data')
class AccountController {

    private final UserRepository userRepository
    private final OrderRepository orderRepository

    AccountController(UserRepository userRepository, OrderRepository orderRepository) {
        this.userRepository = userRepository
        this.orderRepository = orderRepository
    }

    static class ProfileUpdate {
        String firstName
        String lastName
        String phoneNumber
        String address
    }

    @GetMapping('/orders')
    ResponseEntity<?> myOrders(Principal principal) {
        def user = userRepository.findByUsername(principal.name).orElse(null)
        if (!user) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body([message: 'Not signed in'])

        def orders = orderRepository.findByUser_IdOrderByOrderTimeDesc(user.id).collect { order ->
            [
                id           : order.id,
                status       : order.status,
                orderTime    : order.orderTime,
                totalAmount  : order.totalAmount,
                paymentMethod: order.paymentMethod,
                address      : order.address,
                notes        : order.notes,
                items        : order.items.collect { item ->
                    [
                        productId  : item.productId,
                        productName: item.productName,
                        unitPrice  : item.unitPrice,
                        quantity   : item.quantity,
                        lineTotal  : item.lineTotal
                    ]
                }
            ]
        }
        ResponseEntity.ok([orders: orders, count: orders.size()])
    }

    @PutMapping('/profile')
    ResponseEntity<?> updateProfile(@RequestBody ProfileUpdate body, Principal principal) {
        def user = userRepository.findByUsername(principal.name).orElse(null)
        if (!user) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body([message: 'Not signed in'])

        String firstName = body?.firstName?.trim()
        String lastName = body?.lastName?.trim()
        String phone = body?.phoneNumber?.trim()
        String address = body?.address?.trim()
        if (!firstName || !lastName || firstName.length() > 50 || lastName.length() > 50) {
            return ResponseEntity.badRequest().body([message: 'Enter your first and last name'])
        }
        if (!phone || !(phone ==~ AuthController.PHONE_PATTERN)) {
            return ResponseEntity.badRequest().body([message: 'Enter a valid phone number (at least 10 digits)'])
        }
        if (!address || address.length() < 5 || address.length() > 255) {
            return ResponseEntity.badRequest().body([message: 'Enter your delivery address'])
        }

        user.firstName = firstName
        user.lastName = lastName
        user.phoneNumber = phone
        user.address = address
        user.updatedAt = System.currentTimeMillis()
        ResponseEntity.ok(UserProfileDto.from(userRepository.save(user)))
    }
}
