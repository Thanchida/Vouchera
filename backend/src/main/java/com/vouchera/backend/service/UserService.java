package com.vouchera.backend.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vouchera.backend.dto.UserResponse;
import com.vouchera.backend.entity.Company;
import com.vouchera.backend.entity.User;
import com.vouchera.backend.enums.AccountStatus;
import com.vouchera.backend.enums.CompanyStatus;
import com.vouchera.backend.enums.Role;
import com.vouchera.backend.exception.BadRequestException;
import com.vouchera.backend.exception.ForbiddenException;
import com.vouchera.backend.exception.NotFoundException;
import com.vouchera.backend.mapper.UserMapper;
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

    public UserResponse registerCustomer(String email, String password) {
        return registerNewUser(email, password, Role.CUSTOMER, null);
    }

    public UserResponse registerInternalUser(String email, String password, Role role, UUID companyId) {
        if (role != Role.MARKETING) {
            throw new BadRequestException("Internal registration supports only MARKETING users");
        }

        return registerNewUser(email, password, role, companyId);
    }

    private UserResponse registerNewUser(String email, String password, Role role, UUID companyId) {
        String normalizedEmail = EmailNormalizer.normalize(email);

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new BadRequestException("Email already exists");
        }

        Company company = resolveCompanyForRole(role, companyId);

        String hashedPassword = passwordEncoder.encode(password);

        User user = new User(normalizedEmail, hashedPassword, role, company);
        try {
            User saved = userRepository.save(user);
            return UserMapper.toResponse(saved);
        } catch (DataIntegrityViolationException ex) {
            throw new BadRequestException("Email already exists");
        }
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listUsers() {
        return UserMapper.toResponseList(userRepository.findAll());
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found"));
        return UserMapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    public Optional<UserResponse> findByEmail(String email) {
        return userRepository.findByEmail(EmailNormalizer.normalize(email))
            .map(UserMapper::toResponse);
    }

    public UserResponse assignCompany(UUID userId, UUID companyId) {
        User user = getUserEntityById(userId);
        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> new NotFoundException("Company not found"));

        validateActiveUser(user);

        if (user.getRole() != Role.MARKETING) {
            throw new ForbiddenException("Only MARKETING users can be assigned to a company");
        }

        validateActiveCompany(company);

        user.setCompany(company);
        User saved = userRepository.save(user);
        return UserMapper.toResponse(saved);
    }

    public UserResponse updateUserStatus(UUID userId, AccountStatus status) {
        User user = getUserEntityById(userId);

        user.setAccountStatus(status);
        User saved = userRepository.save(user);
        return UserMapper.toResponse(saved);
    }

    private User getUserEntityById(UUID userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found"));
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
}
