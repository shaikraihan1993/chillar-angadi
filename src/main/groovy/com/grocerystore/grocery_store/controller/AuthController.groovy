package com.grocerystore.grocery_store.controller

import com.grocerystore.grocery_store.dto.UserProfileDto
import com.grocerystore.grocery_store.model.User
import com.grocerystore.grocery_store.model.enums.ERole
import com.grocerystore.grocery_store.repository.RoleRepository
import com.grocerystore.grocery_store.repository.UserRepository
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.context.SecurityContextRepository
import org.springframework.web.bind.annotation.*

import java.security.Principal

@RestController
@RequestMapping('/api/auth')
@Tag(name = 'Auth', description = 'Sign in, sign up and session status')
class AuthController {

    static final String EMAIL_PATTERN = '^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$'
    static final String PHONE_PATTERN = '^[+]?[0-9. ()-]{10,25}$'

    private final AuthenticationManager authenticationManager
    private final SecurityContextRepository securityContextRepository
    private final UserRepository userRepository
    private final RoleRepository roleRepository
    private final PasswordEncoder passwordEncoder

    AuthController(AuthenticationManager authenticationManager,
                   SecurityContextRepository securityContextRepository,
                   UserRepository userRepository,
                   RoleRepository roleRepository,
                   PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager
        this.securityContextRepository = securityContextRepository
        this.userRepository = userRepository
        this.roleRepository = roleRepository
        this.passwordEncoder = passwordEncoder
    }

    static class LoginRequest {
        String username   // username or email
        String password
        boolean admin     // true when signing in from /admin/login
    }

    static class SignupRequest {
        String firstName
        String lastName
        String email
        String phoneNumber
        String address
        String password
    }

    @PostMapping('/login')
    ResponseEntity<?> login(@RequestBody LoginRequest body, HttpServletRequest request, HttpServletResponse response) {
        String login = body?.username?.trim()
        if (!login || !body.password) {
            return error(HttpStatus.BAD_REQUEST, 'Enter your email and password')
        }
        Authentication auth
        try {
            auth = authenticationManager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(login, body.password))
        } catch (AuthenticationException ignored) {
            return error(HttpStatus.UNAUTHORIZED, 'Incorrect email or password')
        }
        if (body.admin && !auth.authorities*.authority.contains(ERole.ROLE_ADMIN.name())) {
            return error(HttpStatus.FORBIDDEN, 'This account does not have admin access')
        }
        startSession(auth, request, response)
        ResponseEntity.ok(UserProfileDto.from(userRepository.findByUsername(auth.name).get()))
    }

    @PostMapping('/signup')
    ResponseEntity<?> signup(@RequestBody SignupRequest body, HttpServletRequest request, HttpServletResponse response) {
        String firstName = body?.firstName?.trim()
        String lastName = body?.lastName?.trim()
        String email = body?.email?.trim()?.toLowerCase()
        String phone = body?.phoneNumber?.trim()
        String address = body?.address?.trim()

        if (!firstName || !lastName) return error(HttpStatus.BAD_REQUEST, 'Enter your first and last name')
        if (firstName.length() > 50 || lastName.length() > 50) return error(HttpStatus.BAD_REQUEST, 'Names must be 50 characters or fewer')
        if (!email || !(email ==~ EMAIL_PATTERN) || email.length() > 50) return error(HttpStatus.BAD_REQUEST, 'Enter a valid email address')
        if (!phone || !(phone ==~ PHONE_PATTERN)) return error(HttpStatus.BAD_REQUEST, 'Enter a valid phone number (at least 10 digits)')
        if (!address || address.length() < 5 || address.length() > 255) return error(HttpStatus.BAD_REQUEST, 'Enter your delivery address')
        if (!body.password || body.password.length() < 8) return error(HttpStatus.BAD_REQUEST, 'Password must be at least 8 characters')
        if (userRepository.existsByEmail(email) || userRepository.existsByUsername(email)) {
            return error(HttpStatus.CONFLICT, 'An account with this email already exists')
        }

        def role = roleRepository.findByName(ERole.ROLE_CUSTOMER)
            .orElseThrow { new IllegalStateException('Customer role is missing') }
        def user = userRepository.save(new User(
            username: email,
            email: email,
            password: passwordEncoder.encode(body.password),
            firstName: firstName,
            lastName: lastName,
            phoneNumber: phone,
            address: address,
            role: role
        ))

        def auth = authenticationManager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(email, body.password))
        startSession(auth, request, response)
        ResponseEntity.status(HttpStatus.CREATED).body(UserProfileDto.from(user))
    }

    @GetMapping('/me')
    ResponseEntity<?> me(Principal principal) {
        if (!principal) return error(HttpStatus.UNAUTHORIZED, 'Not signed in')
        userRepository.findByUsername(principal.name)
            .map { ResponseEntity.ok(UserProfileDto.from(it)) as ResponseEntity<?> }
            .orElse(error(HttpStatus.UNAUTHORIZED, 'Not signed in'))
    }

    @PostMapping('/logout')
    ResponseEntity<?> logout(HttpServletRequest request) {
        request.getSession(false)?.invalidate()
        SecurityContextHolder.clearContext()
        ResponseEntity.ok([message: 'Signed out'])
    }

    private void startSession(Authentication auth, HttpServletRequest request, HttpServletResponse response) {
        request.getSession(true)
        request.changeSessionId()   // prevent session fixation
        def context = SecurityContextHolder.createEmptyContext()
        context.authentication = auth
        SecurityContextHolder.context = context
        securityContextRepository.saveContext(context, request, response)
    }

    private static ResponseEntity<Map> error(HttpStatus status, String message) {
        ResponseEntity.status(status).body([message: message])
    }
}
