package com.vouchera.backend.entity;

import jakarta.persistence.*;
import java.util.UUID;


@Entity
@Table(
    name = "companies",
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

    public Company() {}

    public Company(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Company name cannot be blank");
        }
        this.name = name.trim();
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}