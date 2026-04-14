package com.vouchera.backend.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vouchera.backend.dto.user.AdminCreateUserRequest;
import com.vouchera.backend.dto.user.CustomerRegisterRequest;
import com.vouchera.backend.dto.user.UserResponse;
import com.vouchera.backend.enums.AccountStatus;
import com.vouchera.backend.service.UserService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@RestController
@Validated
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody CustomerRegisterRequest request) {
        UserResponse created = userService.registerCustomer(
            request.getEmail(),
            request.getPassword()
        );
        return ResponseEntity.ok(created);
    }

    @PostMapping("/internal/register")
    public ResponseEntity<UserResponse> registerInternal(@Valid @RequestBody AdminCreateUserRequest request) {
        UserResponse created = userService.registerInternalUser(
            request.getEmail(),
            request.getPassword(),
            request.getRole(),
            request.getCompanyId()
        );
        return ResponseEntity.ok(created);
    }

    @GetMapping
    public Page<UserResponse> listUsers(Pageable pageable) {
        return userService.listUsers(pageable);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @GetMapping("/search")
    public ResponseEntity<UserResponse> findByEmail(@RequestParam @NotBlank String email) {
        return userService.findByEmail(email)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/{userId}/company")
    public ResponseEntity<UserResponse> assignCompany(
        @PathVariable UUID userId,
        @RequestParam @NotNull UUID companyId
    ) {
        return ResponseEntity.ok(userService.assignCompany(userId, companyId));
    }

    @PatchMapping("/{userId}/status")
    public ResponseEntity<UserResponse> updateStatus(
        @PathVariable UUID userId,
        @RequestParam @NotNull AccountStatus status
    ) {
        return ResponseEntity.ok(userService.updateUserStatus(userId, status));
    }
}
