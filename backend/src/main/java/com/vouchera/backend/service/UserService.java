package com.vouchera.backend.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vouchera.backend.entity.Company;
import com.vouchera.backend.entity.User;
import com.vouchera.backend.enums.AccountStatus;
import com.vouchera.backend.enums.CompanyStatus;
import com.vouchera.backend.enums.Role;
import com.vouchera.backend.exception.BadRequestException;
import com.vouchera.backend.exception.ForbiddenException;
import com.vouchera.backend.exception.NotFoundException;
import com.vouchera.backend.repository.CompanyRepository;
import com.vouchera.backend.repository.UserRepository;
import com.vouchera.backend.util.EmailNormalizer;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, CompanyRepository companyRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerCustomer(String email, String password) {
        return registerNewUser(email, password, Role.CUSTOMER, null);
    }

    public User registerInternalUser(String email, String password, Role role, UUID companyId) {
        if (role == null) {
            throw new BadRequestException("Role cannot be null");
        }

        if (role != Role.MARKETING) {
            throw new BadRequestException("Internal registration supports only MARKETING users");
        }

        return registerNewUser(email, password, role, companyId);
    }

    private User registerNewUser(String email, String password, Role role, UUID companyId) {
        String normalizedEmail = EmailNormalizer.normalize(email);
        validatePassword(password);

        if (role == null) {
            throw new BadRequestException("Role cannot be null");
        }

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new BadRequestException("Email already exists");
        }

        Company company = resolveCompanyForRole(role, companyId);

        String hashedPassword = passwordEncoder.encode(password);

        User user = new User(normalizedEmail, hashedPassword, role, company);
        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            throw new BadRequestException("Email already exists");
        }
    }

    @Transactional(readOnly = true)
    public List<User> listUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public User getUserById(UUID userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(EmailNormalizer.normalize(email));
    }

    public User assignCompany(UUID userId, UUID companyId) {
        User user = getUserById(userId);
        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> new NotFoundException("Company not found"));

        validateActiveUser(user);

        if (user.getRole() != Role.MARKETING) {
            throw new ForbiddenException("Only MARKETING users can be assigned to a company");
        }

        validateActiveCompany(company);

        user.setCompany(company);
        return userRepository.save(user);
    }

    public User updateUserStatus(UUID userId, AccountStatus status) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found"));

        user.setAccountStatus(status);

        return userRepository.save(user);
    }

    private Company resolveCompanyForRole(Role role, UUID companyId) {
        if (role == Role.MARKETING) {
            if (companyId == null) {
                throw new BadRequestException("MARKETING users must belong to a company");
            }
            Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new NotFoundException("Company not found, register company first"));
            validateActiveCompany(company);
            return company;
        }

        if (companyId != null) {
            if (role == Role.CUSTOMER) {
                throw new BadRequestException("CUSTOMER users cannot be assigned to a company");
            }
            if (role == Role.ADMIN) {
                throw new BadRequestException("ADMIN cannot belong to a company");
            }
        }

        return null;
    }

    private void validateActiveUser(User user) {
        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new ForbiddenException("Only ACTIVE users can be assigned to a company");
        }
    }

    private void validateActiveCompany(Company company) {
        if (company.getCompanyStatus() != CompanyStatus.ACTIVE) {
            throw new ForbiddenException("Company must be ACTIVE");
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new BadRequestException("Password cannot be blank");
        }

        if (password.length() < 8) {
            throw new BadRequestException("Password must be at least 8 characters");
        }
    }
}
