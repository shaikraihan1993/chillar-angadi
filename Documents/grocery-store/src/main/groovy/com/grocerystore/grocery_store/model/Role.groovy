package com.grocerystore.grocery_store.model

import jakarta.persistence.*

@Entity
@Table(name = "roles")
class Role implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ERole name
    
    // Getters and Setters
    Long getId() {
        return id
    }
    
    void setId(Long id) {
        this.id = id
    }
    
    ERole getName() {
        return name
    }
    
    void setName(ERole name) {
        this.name = name
    }
}
