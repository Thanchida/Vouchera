package com.vouchera.backend.dto.company;

import java.util.UUID;

import com.vouchera.backend.entity.Company;
import com.vouchera.backend.enums.CompanyStatus;

public class CompanyResponse {

    private UUID id;
    private String name;
    private CompanyStatus companyStatus;

    public CompanyResponse(UUID id, String name, CompanyStatus companyStatus) {
        this.id = id;
        this.name = name;
        this.companyStatus = companyStatus;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public CompanyStatus getCompanyStatus() {
        return companyStatus;
    }

    public static CompanyResponse fromEntity(Company company) {
        if (company == null) {
            return null;
        }

        return new CompanyResponse(
            company.getId(),
            company.getName(),
            company.getCompanyStatus()
        );
    }
}