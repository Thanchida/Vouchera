package com.vouchera.backend.mapper;

import java.util.List;

import com.vouchera.backend.dto.company.CompanyResponse;
import com.vouchera.backend.entity.Company;

public class CompanyMapper {

    private CompanyMapper() {
    }

    public static CompanyResponse toResponse(Company company)  {
        return new CompanyResponse(
            company.getId(),
            company.getName(),
            company.getCompanyStatus()
        );
    }

    public static List<CompanyResponse> toResponsesList(List<Company> companies) {
        return companies.stream().map(CompanyMapper::toResponse).toList();
    }
}
