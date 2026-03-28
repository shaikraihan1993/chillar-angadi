package com.grocerystore.grocery_store.model.enums

enum ProductCategory {
    FRUITS_AND_VEGETABLES('Fruits & Vegetables'),
    DAIRY_AND_EGGS('Dairy & Eggs'),
    MEAT_AND_SEAFOOD('Meat & Seafood'),
    BAKERY('Bakery'),
    BEVERAGES('Beverages'),
    SNACKS('Snacks'),
    FROZEN_FOODS('Frozen Foods'),
    PANTRY('Pantry'),
    HOUSEHOLD('Household'),
    PERSONAL_CARE('Personal Care')

    final String displayName

    ProductCategory(String displayName) {
        this.displayName = displayName
    }

    @Override
    String toString() {
        return displayName
    }
}
