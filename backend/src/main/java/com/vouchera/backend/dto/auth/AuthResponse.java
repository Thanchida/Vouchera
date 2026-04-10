package com.vouchera.backend.dto.auth;

import java.util.UUID;

import com.vouchera.backend.enums.AccountStatus;
import com.vouchera.backend.enums.Role;

public record AuthResponse(
    UUID userId,
    String email,
    Role role,
    AccountStatus accountStatus,
    UUID companyId,
    String message
) {

    public UUID getUserId() {
        return userId;
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

    public UUID getCompanyId() {
        return companyId;
    }

    public String getMessage() {
        return message;
    }
}
