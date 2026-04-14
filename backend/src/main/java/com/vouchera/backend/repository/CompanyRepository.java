package com.vouchera.backend.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vouchera.backend.entity.Company;

public interface CompanyRepository extends JpaRepository<Company, UUID> {

	Optional<Company> findByNameIgnoreCase(String name);
    
}
