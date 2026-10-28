package com.grocerystore.grocery_store.dto

import com.grocerystore.grocery_store.model.User

/** What the browser is allowed to see about a signed-in user (never the password hash). */
class UserProfileDto {
    Long id
    String username
    String email
    String firstName
    String lastName
    String phoneNumber
    String address
    String role
    Long memberSince

    static UserProfileDto from(User user) {
        new UserProfileDto(
            id: user.id,
            username: user.username,
            email: user.email,
            firstName: user.firstName,
            lastName: user.lastName,
            phoneNumber: user.phoneNumber,
            address: user.address,
            role: user.role?.name?.name() ?: 'ROLE_CUSTOMER',
            memberSince: user.createdAt
        )
    }
}
