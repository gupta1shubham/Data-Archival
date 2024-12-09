package org.fortinet.securityservice.service;


import jakarta.transaction.Transactional;
import org.fortinet.securityservice.exception.UserNotFoundException;
import org.fortinet.securityservice.model.Role;
import org.fortinet.securityservice.model.User;
import org.fortinet.securityservice.repository.RoleRepository;
import org.fortinet.securityservice.repository.UserRepository;
import org.fortinet.securityservice.security.payload.SignupRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.management.relation.RoleNotFoundException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void createUser(SignupRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Set default role if none provided
        Set<Role> roles = new HashSet<>();
        if (request.getRoles() == null || request.getRoles().isEmpty()) {
            roleRepository.findByName("ROLE_USER")
                    .ifPresent(roles::add);
        } else {
            for (String roleName : request.getRoles()) {
                roleRepository.findByName(normalizeRoleName(roleName))
                        .ifPresent(roles::add);
            }
        }
        user.setRoles(roles);

        userRepository.save(user);
    }

    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public User updateUserRoles(String username, Set<String> roleNames) throws RoleNotFoundException {
        // Validate input
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (roleNames == null || roleNames.isEmpty()) {
            throw new IllegalArgumentException("Role names set cannot be null or empty");
        }

        // Find user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));

        // Convert role names to Role entities and validate
        Set<Role> newRoles = new HashSet<>();
        for (String roleName : roleNames) {
            // Normalize role name
            String normalizedRoleName = normalizeRoleName(roleName);
            
            // Find and add role
            Role role = roleRepository.findByName(normalizedRoleName)
                    .orElseThrow(() -> new RoleNotFoundException("Role not found: " + normalizedRoleName));
            newRoles.add(role);
        }

        // Update user roles
        user.setRoles(newRoles);

        // Save and return updated user
        return userRepository.save(user);
    }

    private String normalizeRoleName(String roleName) {
        // Ensure role name follows the ROLE_ convention
        String normalizedName = roleName.toUpperCase().trim();
        if (!normalizedName.startsWith("ROLE_")) {
            normalizedName = "ROLE_" + normalizedName;
        }
        return normalizedName;
    }
}