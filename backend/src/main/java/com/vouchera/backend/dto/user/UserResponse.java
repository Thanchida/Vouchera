package com.vouchera.backend.dto.user;

import java.time.LocalDateTime;
import java.util.UUID;

import com.vouchera.backend.dto.company.CompanyResponse;
import com.vouchera.backend.entity.User;
import com.vouchera.backend.enums.AccountStatus;
import com.vouchera.backend.enums.Role;

public class UserResponse {

    private UUID id;
    private String email;
    private Role role;
    private AccountStatus accountStatus;
    private LocalDateTime createdAt;
    private CompanyResponse company;

    public UserResponse(
        UUID id,
        String email,
        Role role,
        AccountStatus accountStatus,
        LocalDateTime createdAt,
        CompanyResponse company
    ) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.accountStatus = accountStatus;
        this.createdAt = createdAt;
        this.company = company;
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    public AccountStatus getAccountStatus() {
        return accountStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public CompanyResponse getCompany() {
        return company;
    }

    public static UserResponse fromEntity(User user) {
        if (user == null) {
            return null;
        }

        return new UserResponse(
            user.getId(),
            user.getEmail(),
            user.getRole(),
            user.getAccountStatus(),
            user.getCreatedAt(),
            CompanyResponse.fromEntity(user.getCompany())
        );
    }
}