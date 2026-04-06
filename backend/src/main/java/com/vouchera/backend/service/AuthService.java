package com.vouchera.backend.service;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.vouchera.backend.dto.AuthResponse;
import com.vouchera.backend.entity.User;
import com.vouchera.backend.enums.AccountStatus;
import com.vouchera.backend.enums.Role;
import com.vouchera.backend.exception.BadRequestException;
import com.vouchera.backend.exception.ForbiddenException;
import com.vouchera.backend.exception.NotFoundException;
import com.vouchera.backend.repository.UserRepository;
import com.vouchera.backend.util.EmailNormalizer;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    public AuthResponse login(String email, String password) {
        if (password == null || password.isBlank()) {
            throw new BadRequestException("Password cannot be blank");
        }

        String normalizedEmail = EmailNormalizer.normalize(email);
        User user = userRepository.findByEmail(normalizedEmail)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new ForbiddenException("User account is not active");
        }

        if (user.getRole() == Role.MARKETING && user.getCompany() == null) {
            throw new ForbiddenException("MARKETING user must be assigned to a company");
        }

        UUID companyId = user.getCompany() != null ? user.getCompany().getId() : null;
        return new AuthResponse(
            user.getId(),
            user.getEmail(),
            user.getRole(),
            user.getAccountStatus(),
            companyId,
            "Login successful"
        );

    }

    public void logout(UUID userId) {
        if (userId == null) {
            throw new BadRequestException("userId cannot be null");
        }

        userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found"));

        SecurityContextHolder.clearContext();

    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No authenticated user in context");
        }

        String principalName = authentication.getName();
        if (principalName == null || principalName.isBlank() || "anonymousUser".equals(principalName)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No authenticated user in context");
        }

        return userRepository.findByEmail(EmailNormalizer.normalize(principalName))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        
    }
}