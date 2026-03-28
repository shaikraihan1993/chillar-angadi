package com.grocerystore.grocery_store.model

import com.fasterxml.jackson.annotation.JsonBackReference
import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "order_items")
class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonBackReference
    Order order

    @Column(nullable = false)
    Long productId

    @Column(nullable = false)
    String productName

    @Column(nullable = false, precision = 10, scale = 2)
    BigDecimal unitPrice

    @Column(nullable = false)
    int quantity

    @Column(nullable = false, precision = 12, scale = 2)
    BigDecimal lineTotal
}
