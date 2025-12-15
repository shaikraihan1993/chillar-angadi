package com.grocerystore.grocery_store.service

import com.grocerystore.grocery_store.dto.CheckoutRequest
import com.grocerystore.grocery_store.exception.InsufficientStockException
import com.grocerystore.grocery_store.model.Order
import com.grocerystore.grocery_store.model.OrderItem
import com.grocerystore.grocery_store.model.enums.OrderStatus
import com.grocerystore.grocery_store.model.Product
import com.grocerystore.grocery_store.repository.OrderRepository
import com.grocerystore.grocery_store.repository.ProductRepository
import groovy.util.logging.Slf4j
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Slf4j
@Service
class CheckoutService {
    
    private final ProductRepository productRepository
    private final OrderRepository orderRepository
    
    CheckoutService(ProductRepository productRepository, OrderRepository orderRepository) {
        this.productRepository = productRepository
        this.orderRepository = orderRepository
    }
    
    @Transactional
    Order processCheckout(CheckoutRequest request) {
        if (!request?.items || request.items.isEmpty()) {
            throw new IllegalArgumentException("No items provided for checkout")
        }

        // First, validate stock for all items
        request.items.each { item ->
            Product product = productRepository.findById(item.productId).orElse(null)
            if (!product) {
                throw new IllegalArgumentException("Product not found with id: ${item.productId}")
            }
            if (product.quantity < item.quantity) {
                throw new InsufficientStockException("Insufficient stock for product: ${product.name}")
            }
        }

        if (!request.customerEmail) {
            throw new IllegalArgumentException("Customer email is required")
        }

        Order order = new Order(
                customerName: request.customerName,
                phoneNumber: request.phoneNumber,
                address: request.address,
                email: request.customerEmail,
                paymentMethod: request.paymentMethod,
                notes: request.notes,
                status: OrderStatus.PENDING
        )

        BigDecimal total = BigDecimal.ZERO

        // If all validations pass, update the inventory
        request.items.each { item ->
            Product product = productRepository.findById(item.productId).get()
            product.quantity -= item.quantity
            productRepository.save(product)

            BigDecimal lineTotal = product.price * item.quantity
            total = total.add(lineTotal)

            OrderItem orderItem = new OrderItem(
                    order: order,
                    productId: product.id,
                    productName: product.name,
                    unitPrice: product.price,
                    quantity: item.quantity,
                    lineTotal: lineTotal
            )
            order.items.add(orderItem)
        }

        order.totalAmount = total
        
        def savedOrder = orderRepository.save(order)
        log.info("Order #${savedOrder.id} processed successfully")
        
        return savedOrder
    }
}
