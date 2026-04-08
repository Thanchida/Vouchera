package com.vouchera.backend.dto;

import java.util.UUID;

import com.vouchera.backend.enums.AccountStatus;
import com.vouchera.backend.enums.Role;

public record CurrentUserInfo(
    UUID userId,
    String email,
    Role role,
    AccountStatus accountStatus,
    UUID companyId
) {

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public boolean isMarketing() {
        return role == Role.MARKETING;
    }
}