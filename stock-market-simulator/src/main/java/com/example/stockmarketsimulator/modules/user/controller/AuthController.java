package com.example.stockmarketsimulator.modules.user.controller;

import com.example.stockmarketsimulator.modules.user.dto.ApiResponse;
import com.example.stockmarketsimulator.modules.user.dto.AuthResponseDTO;
import com.example.stockmarketsimulator.modules.user.dto.LoginRequest;
import com.example.stockmarketsimulator.modules.user.model.User;
import com.example.stockmarketsimulator.modules.user.security.JwtUtil;
import com.example.stockmarketsimulator.modules.user.service.UserService;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> authenticateUser(
            @RequestBody @Valid LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsernameOrEmail(), loginRequest.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtil.generateToken(loginRequest.getUsernameOrEmail());
            AuthResponseDTO authResponse = new AuthResponseDTO(jwt);

            return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(),"Login successful", authResponse));
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(HttpStatus.UNAUTHORIZED.value(), "Invalid username or password", null));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<User>> createUser(@Valid @RequestBody User user) {
        log.info("📩 Received request to create user: {}", user.getEmail());

        try {
            User savedUser = userService.createUser(user);
            log.info("✅ User created successfully with ID: {}", savedUser.getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(HttpStatus.CREATED.value(), "User created successfully", savedUser));

        } catch (DataIntegrityViolationException e) {
            log.warn("❌ User creation failed due to duplicate email or username: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(HttpStatus.CONFLICT.value(), e.getMessage(), null));

        } catch (ConstraintViolationException e) {
            log.warn("❌ User creation failed due to invalid data: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), "Invalid user data provided", null));

        } catch (Exception e) {
            log.error("🔥 Unexpected error occurred while creating user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Something went wrong!", null));
        }
    }

}
