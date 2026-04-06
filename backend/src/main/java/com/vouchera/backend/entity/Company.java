package com.vouchera.backend.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vouchera.backend.enums.CompanyStatus;


@Entity
@Table(
    name = "companies",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_companies_name", columnNames = "name")
    },
    indexes = {
        @Index(name = "idx_companies_name", columnList = "name")
    }
)
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Campaign> campaigns = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "company_status", nullable = false)
    private CompanyStatus companyStatus;

    public Company() {}

    public Company(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Company name cannot be blank");
        }
        this.name = name.trim();
        this.companyStatus = CompanyStatus.PENDING;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Company name cannot be blank");
        }
        this.name = name.trim();
    }

    public List<Campaign> getCampaigns() {
        return campaigns;
    }

    public void setCampaigns(List<Campaign> campaigns) {
        this.campaigns = campaigns;
    }

    public CompanyStatus getCompanyStatus() {
        return companyStatus;
    }

    public void setCompanyStatus(CompanyStatus companyStatus) {
        this.companyStatus = companyStatus;
    }
}