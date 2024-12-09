package org.fortinet.securityservice.service;

import org.fortinet.securityservice.repository.RoleRepository;
import org.fortinet.securityservice.security.payload.RoleRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.fortinet.securityservice.model.Role;

@Service
public class RoleService {
    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Transactional
    public void createRole(RoleRequest request) {
        String normalizedName = normalizeRoleName(request.getName());
        if (roleRepository.findByName(normalizedName).isPresent()) {
            throw new IllegalArgumentException("Role already exists");
        }

        Role role = new Role();
        role.setName(normalizedName);
        
        roleRepository.save(role);
    }

    private String normalizeRoleName(String name) {
        String normalized = name.toUpperCase().trim();
        return normalized.startsWith("ROLE_") ? normalized : "ROLE_" + normalized;
    }
}