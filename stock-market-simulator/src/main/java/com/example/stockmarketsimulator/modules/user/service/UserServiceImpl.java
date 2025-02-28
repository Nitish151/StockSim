package com.example.stockmarketsimulator.modules.user.service;

import com.example.stockmarketsimulator.modules.user.model.User;
import com.example.stockmarketsimulator.modules.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
@AllArgsConstructor
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

//    @Autowired
//    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder){
//        this.userRepository = userRepository;
//        this.passwordEncoder = passwordEncoder;
//    }

    @Override
    public User createUser(User user) {
        log.info("Creating a new user: {}", user.getEmail());

        if (userRepository.existsByEmail(user.getEmail())) {
            log.warn("❌ User creation failed: Email {} is already in use", user.getEmail());
            throw new DataIntegrityViolationException("User with email " + user.getEmail() + " already exists");
        }

        if (userRepository.existsByUsername(user.getUsername())) {
            log.warn("❌ User creation failed: Username {} is already in use", user.getUsername());
            throw new DataIntegrityViolationException("User with username " + user.getUsername() + " already exists");
        }

        // Ensure roles are properly set
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            log.warn("⚠️ No role provided, defaulting to USER");
            user.setRoles(Set.of(User.Role.USER));  // Use your enum User.Role
        } else {
            log.info("✅ Assigning provided roles: {}", user.getRoles());
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreatedAt(LocalDate.now());

        User savedUser = userRepository.save(user);
        log.info("✅ User created successfully with ID: {}", savedUser.getId());

        return savedUser;
    }







    @Override
    public Page<User> getAllUsers(Pageable pageable){
        log.info("Fetching users");
        return userRepository.findAll(pageable);
    }

    @Override
    public Optional<User> getUserById(Long id){
        log.info("Fetching user with ID: {}", id);
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> updateUser(Long id, User user) {
        log.info("Updating user with ID: {}", id);
        return userRepository.findById(id).map(existingUser -> {
            existingUser.setUsername(user.getUsername());
            existingUser.setEmail(user.getEmail());
            return userRepository.save(existingUser);
        });
    }

    @Override
    public void deleteUser(Long id){
        log.warn("Deleting user with ID: {}", id);
        userRepository.deleteById(id);
    }

    @Override
    public boolean isSelfOrAdmin(Long userId, String username) {
        Optional<User> user = getUserById(userId);
        return user.isPresent() && (user.get().getUsername().equals(username) || isAdmin(username));
    }

    @Override
    public boolean isAdmin(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        return user != null && user.getRoles().contains(User.Role.ADMIN); // Check for enum value
    }


    @Override
    public Optional<User> searchUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public Optional<User> searchUserByUsername(String email) {
        return userRepository.findByUsername(email);
    }

    @Override
    public Optional<User> searchUserByUsernameOrEmail(String usernameOrEmail) {return userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail);}
}
