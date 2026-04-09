package com.vouchera.backend.service;

import java.util.UUID;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.vouchera.backend.dto.AuthResponse;
import com.vouchera.backend.dto.CurrentUserInfo;
import com.vouchera.backend.dto.UserResponse;
import com.vouchera.backend.entity.User;
import com.vouchera.backend.enums.AccountStatus;
import com.vouchera.backend.enums.Role;
import com.vouchera.backend.exception.BadRequestException;
import com.vouchera.backend.exception.ForbiddenException;
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

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        new SecurityContextLogoutHandler().logout(request, response, auth);

        Cookie sessionCookie = new Cookie("JSESSIONID", "");
        sessionCookie.setPath("/");
        sessionCookie.setMaxAge(0);
        sessionCookie.setHttpOnly(true);
        response.addCookie(sessionCookie);
    }

    public UserResponse getCurrentUser() {
        return UserResponse.fromEntity(getCurrentUserEntity());
    }

    public CurrentUserInfo getCurrentUserInfo() {
        User user = getCurrentUserEntity();
        UUID companyId = user.getCompany() != null ? user.getCompany().getId() : null;
        return new CurrentUserInfo(
            user.getId(),
            user.getEmail(),
            user.getRole(),
            user.getAccountStatus(),
            companyId
        );
    }

    private User getCurrentUserEntity() {
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