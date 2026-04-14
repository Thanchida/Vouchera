package com.vouchera.backend.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vouchera.backend.dto.company.CompanyResponse;
import com.vouchera.backend.enums.CompanyStatus;
import com.vouchera.backend.service.CompanyService;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@RestController
@Validated
@RequestMapping("/api/companies")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }
    
    @GetMapping
    public Page<CompanyResponse> getCompanies(Pageable pageable) {
        return companyService.listCompanies(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompanyResponse> getCompanyInfo(@PathVariable UUID id) {
        return ResponseEntity.ok(companyService.getCompanyById(id));
    }

    @PostMapping
    public ResponseEntity<CompanyResponse> createCompany(@RequestParam @NotBlank String name) {
        return ResponseEntity.ok(companyService.createCompany(name));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompanyResponse> updateCompanyName(@PathVariable UUID id, @RequestParam @NotBlank String name) {
        return ResponseEntity.ok(companyService.updateCompanyName(id, name));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<CompanyResponse> updateCompanyStatus(
        @PathVariable UUID id,
        @RequestParam @NotNull CompanyStatus status
    ) {
        return ResponseEntity.ok(companyService.updateCompanyStatus(id, status));
    }
}
