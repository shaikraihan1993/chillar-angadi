package com.grocerystore.grocery_store.model.enums

enum ProductSubCategory {
    // Fruits & Vegetables
    FRESH_FRUITS('Fresh Fruits', ProductCategory.FRUITS_AND_VEGETABLES),
    FRESH_VEGETABLES('Fresh Vegetables', ProductCategory.FRUITS_AND_VEGETABLES),
    ORGANIC_PRODUCE('Organic Produce', ProductCategory.FRUITS_AND_VEGETABLES),
    HERBS_AND_SEASONINGS('Herbs & Seasonings', ProductCategory.FRUITS_AND_VEGETABLES),

    // Dairy & Eggs
    MILK_AND_CREAM('Milk & Cream', ProductCategory.DAIRY_AND_EGGS),
    CHEESE('Cheese', ProductCategory.DAIRY_AND_EGGS),
    YOGURT('Yogurt', ProductCategory.DAIRY_AND_EGGS),
    EGGS('Eggs', ProductCategory.DAIRY_AND_EGGS),
    BUTTER_AND_MARGARINE('Butter & Margarine', ProductCategory.DAIRY_AND_EGGS),

    // Meat & Seafood
    BEEF('Beef', ProductCategory.MEAT_AND_SEAFOOD),
    POULTRY('Poultry', ProductCategory.MEAT_AND_SEAFOOD),
    PORK('Pork', ProductCategory.MEAT_AND_SEAFOOD),
    SEAFOOD('Seafood', ProductCategory.MEAT_AND_SEAFOOD),
    DELI_MEATS('Deli Meats', ProductCategory.MEAT_AND_SEAFOOD),

    // Bakery
    BREAD('Bread', ProductCategory.BAKERY),
    PASTRIES('Pastries', ProductCategory.BAKERY),
    CAKES('Cakes', ProductCategory.BAKERY),
    COOKIES('Cookies', ProductCategory.BAKERY)

    final String displayName
    final ProductCategory category

    ProductSubCategory(String displayName, ProductCategory category) {
        this.displayName = displayName
        this.category = category
    }

    @Override
    String toString() {
        return displayName
    }

    static List<ProductSubCategory> getByCategory(ProductCategory category) {
        return values().findAll { it.category == category }
    }
}
