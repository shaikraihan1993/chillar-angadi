package com.grocerystore.grocery_store.model.enums

enum ProductUnit {
    KILOGRAM('kg', 'Kilogram'),
    GRAM('g', 'Gram'),
    LITER('L', 'Liter'),
    MILLILITER('ml', 'Milliliter'),
    PIECES('pcs', 'Pieces'),
    PACK('pack', 'Pack'),
    BOTTLE('bottle', 'Bottle'),
    BOX('box', 'Box'),
    CAN('can', 'Can'),
    BAG('bag', 'Bag')

    final String code
    final String displayName

    ProductUnit(String code, String displayName) {
        this.code = code
        this.displayName = displayName
    }

    @Override
    String toString() {
        return displayName
    }

    static ProductUnit fromCode(String code) {
        return values().find { it.code.equalsIgnoreCase(code) }
    }
}
