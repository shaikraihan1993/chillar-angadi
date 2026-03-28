package com.grocerystore.grocery_store.controller

import com.grocerystore.grocery_store.model.Order
import com.grocerystore.grocery_store.service.OrderService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/orders")
class OrderController {

    private final OrderService orderService

    OrderController(OrderService orderService) {
        this.orderService = orderService
    }

    @GetMapping
    ResponseEntity<Map<String, Object>> listOrders(
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        try {
            def pageable = PageRequest.of(page, size)
            def ordersPage = orderService.findOrders(search, page, size)
            
            def orders = ordersPage.content.collect { order ->
                [
                    id: order.id,
                    customerName: order.customerName,
                    phoneNumber: order.phoneNumber,
                    address: order.address,
                    paymentMethod: order.paymentMethod,
                    status: order.status,
                    orderTime: order.orderTime,
                    totalAmount: order.totalAmount,
                    notes: order.notes,
                    items: order.items.collect { item ->
                        [
                            productId: item.productId,
                            productName: item.productName,
                            unitPrice: item.unitPrice,
                            quantity: item.quantity,
                            lineTotal: item.lineTotal
                        ]
                    }
                ]
            }

            def response = [
                totalItems: ordersPage.totalElements,
                totalPages: ordersPage.totalPages,
                currentPage: ordersPage.number,
                orders: orders
            ]

            new ResponseEntity<>(response, HttpStatus.OK)
            
        } catch (Exception e) {
            new ResponseEntity<>([error: e.message], HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
    
    @PostMapping
    ResponseEntity<Map<String, Object>> createOrder(@RequestBody Map<String, Object> orderData) {
        try {
            def order = orderService.createOrder(orderData)
            new ResponseEntity<>(order, HttpStatus.CREATED)
        } catch (Exception e) {
            new ResponseEntity<>([error: e.message], HttpStatus.BAD_REQUEST)
        }
    }
    
    @GetMapping("/{id}")
    ResponseEntity<?> getOrder(@PathVariable Long id) {
        try {
            def order = orderService.findById(id)
            new ResponseEntity<>(order, HttpStatus.OK)
        } catch (NoSuchElementException e) {
            new ResponseEntity<>([error: "Order not found"], HttpStatus.NOT_FOUND)
        } catch (Exception e) {
            new ResponseEntity<>([error: e.message], HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
    
    @PutMapping("/{id}/status")
    ResponseEntity<?> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam String status
    ) {
        try {
            def updatedOrder = orderService.updateStatus(id, status)
            new ResponseEntity<>(updatedOrder, HttpStatus.OK)
        } catch (NoSuchElementException e) {
            new ResponseEntity<>([error: "Order not found"], HttpStatus.NOT_FOUND)
        } catch (Exception e) {
            new ResponseEntity<>([error: e.message], HttpStatus.BAD_REQUEST)
        }
    }
    
    @PutMapping("/{id}")
    ResponseEntity<?> updateOrder(
            @PathVariable Long id,
            @RequestBody Map<String, Object> orderData
    ) {
        try {
            def updatedOrder = orderService.updateOrder(id, orderData)
            new ResponseEntity<>(updatedOrder, HttpStatus.OK)
        } catch (NoSuchElementException e) {
            new ResponseEntity<>([error: "Order not found"], HttpStatus.NOT_FOUND)
        } catch (Exception e) {
            new ResponseEntity<>([error: e.message], HttpStatus.BAD_REQUEST)
        }
    }
    
    @DeleteMapping("/{id}")
    ResponseEntity<?> deleteOrder(@PathVariable Long id) {
        try {
            orderService.deleteOrder(id)
            return new ResponseEntity<>(HttpStatus.NO_CONTENT)
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>([error: "Order not found"], HttpStatus.NOT_FOUND)
        } catch (Exception e) {
            return new ResponseEntity<>([error: e.message], HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}
