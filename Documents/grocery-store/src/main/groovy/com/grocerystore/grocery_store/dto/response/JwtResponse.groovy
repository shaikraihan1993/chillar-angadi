package com.grocerystore.grocery_store.dto.response

class JwtResponse {
    String token
    String type = "Bearer"
    Long id
    String username
    String email
    List<String> roles

    JwtResponse(String accessToken, Long id, String username, String email, List<String> roles) {
        this.token = accessToken
        this.id = id
        this.username = username
        this.email = email
        this.roles = roles
    }
}
