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
    COOKIES('Cookies', ProductCategory.BAKERY),

    // Beverages
    WATER('Water', ProductCategory.BEVERAGES),
    SOFT_DRINKS('Soft Drinks', ProductCategory.BEVERAGES),
    JUICES('Juices', ProductCategory.BEVERAGES),
    COFFEE_AND_TEA('Coffee & Tea', ProductCategory.BEVERAGES),
    ENERGY_DRINKS('Energy Drinks', ProductCategory.BEVERAGES),

    // Snacks
    CHIPS_AND_CRISPS('Chips & Crisps', ProductCategory.SNACKS),
    NUTS_AND_SEEDS('Nuts & Seeds', ProductCategory.SNACKS),
    POPCORN('Popcorn', ProductCategory.SNACKS),
    CRACKERS('Crackers', ProductCategory.SNACKS),
    CANDY_AND_CHOCOLATE('Candy & Chocolate', ProductCategory.SNACKS),

    // Frozen Foods
    FROZEN_MEALS('Frozen Meals', ProductCategory.FROZEN_FOODS),
    ICE_CREAM('Ice Cream', ProductCategory.FROZEN_FOODS),
    FROZEN_VEGETABLES('Frozen Vegetables', ProductCategory.FROZEN_FOODS),
    FROZEN_MEAT('Frozen Meat', ProductCategory.FROZEN_FOODS),
    FROZEN_PIZZA('Frozen Pizza', ProductCategory.FROZEN_FOODS),

    // Pantry
    PASTA_AND_NOODLES('Pasta & Noodles', ProductCategory.PANTRY),
    RICE_AND_GRAINS('Rice & Grains', ProductCategory.PANTRY),
    CANNED_GOODS('Canned Goods', ProductCategory.PANTRY),
    SAUCES_AND_CONDIMENTS('Sauces & Condiments', ProductCategory.PANTRY),
    COOKING_OIL('Cooking Oil', ProductCategory.PANTRY),
    BAKING_SUPPLIES('Baking Supplies', ProductCategory.PANTRY),

    // Household
    CLEANING_SUPPLIES('Cleaning Supplies', ProductCategory.HOUSEHOLD),
    LAUNDRY('Laundry', ProductCategory.HOUSEHOLD),
    PAPER_PRODUCTS('Paper Products', ProductCategory.HOUSEHOLD),
    KITCHEN_SUPPLIES('Kitchen Supplies', ProductCategory.HOUSEHOLD),

    // Personal Care
    BATH_AND_BODY('Bath & Body', ProductCategory.PERSONAL_CARE),
    HAIR_CARE('Hair Care', ProductCategory.PERSONAL_CARE),
    ORAL_CARE('Oral Care', ProductCategory.PERSONAL_CARE),
    SKIN_CARE('Skin Care', ProductCategory.PERSONAL_CARE),
    HEALTH_AND_WELLNESS('Health & Wellness', ProductCategory.PERSONAL_CARE)

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
