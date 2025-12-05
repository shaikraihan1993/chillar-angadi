package com.grocerystore.grocery_store.service

import com.grocerystore.grocery_store.model.Order
import com.grocerystore.grocery_store.model.OrderItem
import com.grocerystore.grocery_store.repository.OrderRepository
import groovy.util.logging.Slf4j
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Slf4j
@Service
class OrderService {

    private final OrderRepository orderRepository

    OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository
    }

    Page<Order> findOrders(String search, int page, int size) {
        int pageIndex = Math.max(page, 0)
        int pageSize = size > 0 ? size : 10
        Pageable pageable = PageRequest.of(pageIndex, pageSize, Sort.by(Sort.Direction.DESC, "orderTime"))

        if (search && !search.trim().isEmpty()) {
            String term = search.trim()
            return orderRepository.findByCustomerNameContainingIgnoreCaseOrPhoneNumberContainingIgnoreCase(term, term, pageable)
        }

        return orderRepository.findAll(pageable)
    }
    
    @Transactional
    Order createOrder(Order order) {
        return orderRepository.save(order)
    }
    
    @Transactional
    void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new NoSuchElementException("Order not found with id: $id")
        }
        orderRepository.deleteById(id)
    }
    
    @Transactional
    Map<String, Object> updateOrder(Long id, Map<String, Object> orderData) {
        def order = orderRepository.findById(id)
            .orElseThrow { new NoSuchElementException("Order not found with id: $id") }
            
        // Update order fields
        order.customerName = orderData.customerName as String
        order.phoneNumber = orderData.phoneNumber as String
        order.address = orderData.address as String
        order.paymentMethod = orderData.paymentMethod as String
        order.status = orderData.status as String
        order.notes = orderData.notes as String
        
        // Clear existing items
        order.items.clear()
        
        // Add updated items
        if (orderData.items) {
            orderData.items.each { itemData ->
                def item = new OrderItem(
                    id: itemData.id ? itemData.id as Long : null,
                    productId: itemData.productId as Long,
                    productName: itemData.productName as String,
                    unitPrice: itemData.unitPrice as BigDecimal,
                    quantity: itemData.quantity as Integer,
                    lineTotal: (itemData.unitPrice as BigDecimal) * (itemData.quantity as Integer)
                )
                order.addToItems(item)
            }
        }
        
        // Recalculate total amount
        order.totalAmount = order.items.sum { it.lineTotal } ?: BigDecimal.ZERO
        
        def savedOrder = orderRepository.save(order)
        
        // Return the updated order in the same format as the GET endpoint
        return [
            id: savedOrder.id,
            customerName: savedOrder.customerName,
            phoneNumber: savedOrder.phoneNumber,
            address: savedOrder.address,
            paymentMethod: savedOrder.paymentMethod,
            status: savedOrder.status,
            orderTime: savedOrder.orderTime,
            totalAmount: savedOrder.totalAmount,
            notes: savedOrder.notes,
            items: savedOrder.items.collect { item ->
                [
                    id: item.id,
                    productId: item.productId,
                    productName: item.productName,
                    unitPrice: item.unitPrice,
                    quantity: item.quantity,
                    lineTotal: item.lineTotal
                ]
            }
        ]
    }
    
    Order updateStatus(Long id, String status) {
        def order = orderRepository.findById(id)
            .orElseThrow { new NoSuchElementException("Order not found with id: $id") }
        order.status = status
        return orderRepository.save(order)
    }
    
    Optional<Order> findById(Long id) {
        return orderRepository.findById(id)
    }
}
