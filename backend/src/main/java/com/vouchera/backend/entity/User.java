package com.vouchera.backend.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

import com.vouchera.backend.enums.AccountStatus;
import com.vouchera.backend.enums.Role;
import com.vouchera.backend.util.EmailNormalizer;

import org.hibernate.annotations.CreationTimestamp;


@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false)
    private AccountStatus accountStatus;
    
    public User() {}
    
    public User(String email, String password, Role role, Company company) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        
        setEmail(email);
        setPassword(password);
        this.role = role;
        this.company = company;
        this.accountStatus = AccountStatus.ACTIVE;
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public boolean isMarketing() {
        return role == Role.MARKETING;
    }

    public boolean isCustomer() {
        return role == Role.CUSTOMER;
    }

    public boolean canManageCampaign(Campaign campaign) {
        if (isAdmin()) {
            return true;
        }
        if (isMarketing() && company != null && campaign != null) {
            return company.getId().equals(campaign.getCompany().getId());
        }
        return false;
    }
    
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = EmailNormalizer.normalize(email);
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be blank");
        }
        if (password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }
        this.password = password;
    }
    
    public Role getRole() {
        return role;
    }
    
    public void setRole(Role role) {
        this.role = role;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public AccountStatus getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(AccountStatus accountStatus) {
        this.accountStatus = accountStatus;
    }
}
