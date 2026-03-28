package com.grocerystore.grocery_store.exception

class ResourceNotFoundException extends RuntimeException {
    ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue))
        this.resourceName = resourceName
        this.fieldName = fieldName
        this.fieldValue = fieldValue
    }

    private final String resourceName
    private final String fieldName
    private final Object fieldValue

    String getResourceName() {
        return resourceName
    }

    String getFieldName() {
        return fieldName
    }

    Object getFieldValue() {
        return fieldValue
    }
}
