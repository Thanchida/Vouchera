package com.vouchera.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.vouchera.backend.entity.User;
import com.vouchera.backend.enums.Role;
import com.vouchera.backend.repository.UserRepository;
import com.vouchera.backend.util.EmailNormalizer;

@Component
public class AdminBootstrap implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminBootstrap.class);

    private final UserRepository userRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public AdminBootstrap(
        UserRepository userRepository,
        org.springframework.security.crypto.password.PasswordEncoder passwordEncoder,
        @Value("${vouchera.bootstrap.admin-email:}") String adminEmail,
        @Value("${vouchera.bootstrap.admin-password:}") String adminPassword
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        boolean adminAlreadyExists = userRepository.existsByRole(Role.ADMIN);
        if (adminAlreadyExists) {
            return;
        }

        if (adminEmail == null || adminEmail.isBlank() || adminPassword == null || adminPassword.isBlank()) {
            logger.warn("Admin bootstrap skipped because vouchera.bootstrap.admin-email/admin-password are not configured");
            return;
        }

        String normalizedEmail = EmailNormalizer.normalize(adminEmail);
        if (userRepository.existsByEmail(normalizedEmail)) {
            logger.warn("Admin bootstrap skipped because email {} is already in use", normalizedEmail);
            return;
        }

        User admin = new User(normalizedEmail, passwordEncoder.encode(adminPassword), Role.ADMIN, null);
        userRepository.save(admin);
        logger.info("Bootstrapped initial admin account for {}", normalizedEmail);
    }
}
