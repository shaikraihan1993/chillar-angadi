package com.grocerystore.grocery_store.dto

class CheckoutRequest {
    String customerName
    String phoneNumber
    String address
    String paymentMethod
    String notes
    List<OrderItem> items
    String customerEmail

    static class OrderItem {
        Long productId
        int quantity
    }
}
