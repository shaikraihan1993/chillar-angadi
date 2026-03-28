package com.grocerystore.grocery_store.model

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*

@Entity
@Table(name = "product_images")
class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id

    @Column(nullable = false)
    String fileName

    @Column(nullable = false, length = 100)
    String fileType

    @Lob
    @JsonIgnore
    @Column(nullable = false, columnDefinition = "LONGBLOB")
    byte[] data

    // No need for a back reference to Product
    // The relationship is managed from the Product side

    @Transient
    String getImageUrl() {
        return "/api/v1/images/" + id
    }
    
    @Column(name = "upload_date", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    Date uploadDate = new Date()
}
