package com.grocerystore.grocery_store.model

import jakarta.persistence.*

/** A choice for the admin product form's Brand dropdown. Products store the name as text. */
@Entity
@Table(name = "brands")
class Brand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id

    @Column(nullable = false, unique = true, length = 100)
    String name
}
