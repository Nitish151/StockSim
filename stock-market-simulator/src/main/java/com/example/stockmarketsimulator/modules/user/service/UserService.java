package com.example.stockmarketsimulator.modules.user.service;

import com.example.stockmarketsimulator.modules.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Optional;

public interface UserService {

    void deposit(String username, BigDecimal amount);

    boolean withdraw(String username, BigDecimal amount);

    BigDecimal getUserBalance(String username);

    User getUserOrThrow(String username);

    User createUser(User user);
    Page<User> getAllUsers(Pageable pageable);
    Optional<User> getUserById(Long id);
    Optional<User> updateUser(Long id, User user);
    void deleteUser(Long id);

    boolean isSelfOrAdmin(Long userId, String username);

    boolean isAdmin(String username);

    Optional<User> searchUserByEmail(String email);
    Optional<User> searchUserByUsername(String email);

    Optional<User> searchUserByUsernameOrEmail(String usernameOrEmail);
}
