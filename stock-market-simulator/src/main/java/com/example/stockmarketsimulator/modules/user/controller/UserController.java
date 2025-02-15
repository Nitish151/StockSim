package com.example.stockmarketsimulator.modules.user.controller;

import com.example.stockmarketsimulator.modules.user.dto.ApiResponse;
import com.example.stockmarketsimulator.modules.user.exception.UserNotFoundException;
import com.example.stockmarketsimulator.modules.user.model.User;
import com.example.stockmarketsimulator.modules.user.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService){
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<User>>> getAllUsers(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable){
        log.info("Fetching all users with pagination: page={}, size={}, sort={}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        Page<User> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Users fetched successfully", users));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable Long id) {
        log.info("Received request to get user with ID: {}", id);

        return userService.getUserById(id)
                .map(user -> {
                    log.info("✅ User found: {}", user.getEmail());
                    return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "User found", user));
                })
                .orElseThrow(() -> {
                    log.warn("❌ User with ID {} not found", id);
                    return new UserNotFoundException("User not found with id: " + id);
                });
    }


    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> updateUser(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates,
            Principal principal) {

        log.info("Received request to update user with ID: {}", id);

        // Ensure user is authenticated
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(HttpStatus.UNAUTHORIZED.value(), "User not authenticated", null));
        }

        // Ensure user can only update their own details or an admin is making the change
        if (!userService.isSelfOrAdmin(id, principal.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(HttpStatus.FORBIDDEN.value(), "Unauthorized!", null));
        }

        // Fetch the existing user
        Optional<User> existingUserOpt = userService.getUserById(id);
        if (existingUserOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(HttpStatus.NOT_FOUND.value(), "User not found", null));
        }

        User existingUser = existingUserOpt.get();

        // Prevent role updates
        if (updates.containsKey("roles")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(HttpStatus.FORBIDDEN.value(), "You cannot change user roles", null));
        }

        // Apply partial updates
        updates.forEach((key, value) -> {
            switch (key) {
                case "username" -> existingUser.setUsername(value.toString());
                case "email" -> existingUser.setEmail(value.toString());
            }
        });

        // Save the updated user
        Optional<User> updatedUser = userService.updateUser(id, existingUser);

        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "User successfully updated", updatedUser.get()));
    }



    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Long id, Principal principal) {
        log.warn("Received request to delete user with ID: {}", id);
        if (!userService.isSelfOrAdmin(id, principal.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(HttpStatus.FORBIDDEN.value(), "Unauthorized!", null));
        }
        userService.deleteUser(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "User deleted successfully", null));
    }


    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Optional<User>>> searchUsers(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email) {

        log.info("Received search request with username: {} and email: {}", username, email);
        if (username == null && email == null) {
            log.warn("No search parameter provided");
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), "No search parameter provided", Optional.empty()));
        }

        Optional<User> user = Optional.empty();

        if (username != null) {
            user = userService.searchUserByUsername(username);
            log.info("Searching user by username: {}", username);
            } else if (email != null) {
            user = userService.searchUserByEmail(email);
            log.info("Searching user by email: {}", email);
            }

        if (user.isEmpty()) {
            log.warn("No user found for given search criteria");
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(HttpStatus.NOT_FOUND.value(), "No user found", Optional.empty()));
        }

        log.info("User found: {}", user);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "User found", user));
    }

}