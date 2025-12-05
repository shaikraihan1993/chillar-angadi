package com.grocerystore.grocery_store.exception

class InsufficientStockException extends RuntimeException {
    InsufficientStockException(String message) {
        super(message)
    }
}
