package com.grocerystore.grocery_store.repository

import com.grocerystore.grocery_store.model.enums.ERole
import com.grocerystore.grocery_store.model.Role
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(ERole name)
}
