package com.grocerystore.grocery_store.model

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonManagedReference
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "orders")
class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id

    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id", nullable = false)
    @JoinColumn(name = "user_id", nullable = true)
    @JsonBackReference
    User user

    // Store customer details at the time of order
    @Column(nullable = false)
    String customerName

    @Column(nullable = false, length = 20)
    String phoneNumber

    @Column(nullable = false, length = 500)
    String address

    @Column(nullable = false, length = 500)
    String email

    @Column(nullable = false, length = 50)
    String paymentMethod

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    OrderStatus status = OrderStatus.PENDING

    @Column(nullable = false)
    Instant orderTime = Instant.now()

    @Column(nullable = false, precision = 12, scale = 2)
    BigDecimal totalAmount = BigDecimal.ZERO

    @Column(length = 500)
    String notes

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    List<OrderItem> items = []
    
    // Add an item to this order
    def addToItems(OrderItem item) {
        if (item != null) {
            item.order = this
            items.add(item)
        }
        return this
    }
    
    // Remove an item from this order
    def removeFromItems(OrderItem item) {
        if (item != null) {
            items.remove(item)
            item.order = null
        }
        return this
    }
    
    // Clear all items from this order
    def clearItems() {
        items.each { it.order = null }
        items.clear()
        return this
    }
}
