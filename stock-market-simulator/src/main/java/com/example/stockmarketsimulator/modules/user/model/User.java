package com.example.stockmarketsimulator.modules.user.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.security.core.userdetails.UserDetails;

import javax.management.relation.Role;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Entity // Database Entity
@Table(name = "users") // Maps this entity to the "users" table
@Getter // auto-generate getters
@Setter // auto-generate setters
@NoArgsConstructor // Generates a no-args constructor
@AllArgsConstructor // Generates an all-args constructor
@Builder // Enables builder pattern for creating objects
public class User {

    @Id // Marks this as the primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Auto-generated user ID (because of the annotations)

    @NotBlank(message = "Username cannot be blank")
    @Size(min = 3, max = 50, message = "Name must be between 3 and 50 characters")
    @Pattern(regexp = "^\\S+$", message = "Username cannot contain spaces")
    @Column(nullable = false, unique = true)
    private String username;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    @Column(unique = true)
    private String email;

    @Column
    private BigDecimal balance = BigDecimal.valueOf(0.0);

    @Column(name = "reserved_balance")
    private BigDecimal reservedBalance = BigDecimal.ZERO;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<Role> roles;

    private LocalDate createdAt = LocalDate.now();

    public enum Role{
        ADMIN, USER
    }
}
