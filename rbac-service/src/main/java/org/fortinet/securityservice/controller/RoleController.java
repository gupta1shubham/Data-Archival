package org.fortinet.securityservice.controller;

import jakarta.validation.Valid;
import org.fortinet.securityservice.security.payload.RoleRequest;
import org.fortinet.securityservice.service.RoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/roles")
public class RoleController {
    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping
    public ResponseEntity<?> createRole(@Valid @RequestBody RoleRequest request) {
        roleService.createRole(request);
        return ResponseEntity.ok().body("Role created successfully");
    }
}