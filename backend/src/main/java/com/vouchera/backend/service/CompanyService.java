package com.vouchera.backend.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.vouchera.backend.dto.company.CompanyResponse;
import com.vouchera.backend.entity.Company;
import com.vouchera.backend.enums.CompanyStatus;
import com.vouchera.backend.exception.BadRequestException;
import com.vouchera.backend.exception.ForbiddenException;
import com.vouchera.backend.exception.NotFoundException;
import com.vouchera.backend.repository.CompanyRepository;

@Service
@Transactional
public class CompanyService {

	private final CompanyRepository companyRepository;

	public CompanyService(CompanyRepository companyRepository) {
		this.companyRepository = companyRepository;
	}

	public CompanyResponse createCompany(String name) {
		String normalizedName = normalizeName(name);

		if (companyRepository.findByNameIgnoreCase(normalizedName).isPresent()) {
			throw new BadRequestException("Company name already exists");
		}

		Company company = new Company(normalizedName);
		return CompanyResponse.fromEntity(companyRepository.save(company));
	}

	@Transactional(readOnly = true)
	public CompanyResponse getCompanyById(UUID companyId) {
		Company company = companyRepository.findById(companyId)
			.orElseThrow(() -> new NotFoundException("Company not found"));
		return CompanyResponse.fromEntity(company);
	}

	@Transactional(readOnly = true)
	public Page<CompanyResponse> listCompanies(Pageable pageable) {
		return companyRepository.findAll(pageable).map(CompanyResponse::fromEntity);
	}

	public CompanyResponse updateCompanyName(UUID companyId, String newName) {
		Company company = getCompanyEntityById(companyId);
		validateCompanyEditable(company);
		String normalizedName = normalizeName(newName);

		companyRepository.findByNameIgnoreCase(normalizedName).ifPresent(existing -> {
			if (!existing.getId().equals(companyId)) {
				throw new BadRequestException("Company name already exists");
			}
		});

		company.setName(normalizedName);
		return CompanyResponse.fromEntity(companyRepository.save(company));
	}

	public CompanyResponse updateCompanyStatus(UUID companyId, CompanyStatus status) {
		if (status == null) {
			throw new BadRequestException("Company status cannot be null");
		}

		Company company = getCompanyEntityById(companyId);
		company.setCompanyStatus(status);
		return CompanyResponse.fromEntity(companyRepository.save(company));
	}

	private Company getCompanyEntityById(UUID companyId) {
		return companyRepository.findById(companyId)
			.orElseThrow(() -> new NotFoundException("Company not found"));
	}

	private void validateCompanyEditable(Company company) {
		if (company.getCompanyStatus() == CompanyStatus.SUSPENDED || company.getCompanyStatus() == CompanyStatus.REJECTED) {
			throw new ForbiddenException("Company status does not allow updates");
		}
	}

	private String normalizeName(String name) {
		if (name == null || name.isBlank()) {
			throw new BadRequestException("Company name cannot be blank");
		}
		return name.trim();
	}
}
