package com.grocerystore.grocery_store.security

import com.grocerystore.grocery_store.repository.UserRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

/**
 * Loads users by username or email so people can sign in with either.
 * The principal name is always the stored username.
 */
@Service
class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository

    AppUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository
    }

    @Override
    UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        def user = userRepository.findByUsernameOrEmail(login, login)
            .orElseThrow { new UsernameNotFoundException('User not found') }
        String role = user.role?.name?.name() ?: 'ROLE_CUSTOMER'
        org.springframework.security.core.userdetails.User
            .withUsername(user.username)
            .password(user.password)
            .authorities(new SimpleGrantedAuthority(role))
            .build()
    }
}
