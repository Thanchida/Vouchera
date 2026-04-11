package com.vouchera.backend.controller;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vouchera.backend.dto.auth.AuthResponse;
import com.vouchera.backend.dto.auth.LoginRequest;
import com.vouchera.backend.dto.user.UserResponse;
import com.vouchera.backend.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final HttpSessionSecurityContextRepository securityContextRepository;

    public AuthController(
        AuthService authService,
        HttpSessionSecurityContextRepository securityContextRepository
    ) {
        this.authService = authService;
        this.securityContextRepository = securityContextRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        AuthResponse response = authService.login(request.getEmail(), request.getPassword());

        Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(
            response.getEmail(),
            null,
            java.util.List.of(new SimpleGrantedAuthority("ROLE_" + response.getRole().name()))
        );
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);

        // trigger CSRF cookie
        CsrfToken token = (CsrfToken) httpRequest.getAttribute(CsrfToken.class.getName());
        if (token != null) {
            token.getToken();
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(request, response);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me() {
        return ResponseEntity.ok(authService.getCurrentUser());
    }

    @GetMapping("/csrf")
    public ResponseEntity<Void> csrf(CsrfToken csrfToken, HttpServletResponse response) {
        response.setHeader("X-CSRF-TOKEN", csrfToken.getToken());
        csrfToken.getToken();
        return ResponseEntity.noContent().build();
    }
}
