package com.vouchera.backend.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vouchera.backend.enums.CampaignStatus;

import jakarta.persistence.Column;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;


@Entity
@Table(
    name = "campaigns",
    indexes = {
        @Index(name = "idx_campaign_company_status_start", columnList = "company_id, status, start_time")
    }
)
public class Campaign {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Version
    private Long version;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @OneToMany(mappedBy = "campaign", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<VoucherType> voucherTypes = new ArrayList<>();

    @Column(nullable = false, name = "start_time")
    private LocalDateTime startTime;

    @Column(nullable = false, name = "end_time")
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CampaignStatus status;

    public Campaign() {}

    public Campaign(String name, String description, Company company, LocalDateTime startTime, LocalDateTime endTime, CampaignStatus status) {
        setName(name);
        setDescription(description);
        setCompany(company);
        setSchedule(startTime, endTime);
        setStatus(status);
    }

    public boolean isActiveAt(LocalDateTime now) {
        return status == CampaignStatus.ACTIVE && 
               !now.isBefore(startTime) && 
               !now.isAfter(endTime);
    }

    public void resume(LocalDateTime now) {
        if (status != CampaignStatus.PAUSED) {
            throw new IllegalStateException("Campaign must be PAUSED to resume");
        }
        if (now.isAfter(endTime)) {
            throw new IllegalStateException("Cannot resume campaign after end time");
        }
        status = CampaignStatus.ACTIVE;
    }

    public void pause() {
        if (status != CampaignStatus.ACTIVE) {
            throw new IllegalStateException("Only active campaign can pause");
        }
        status = CampaignStatus.PAUSED;
    }

    public void end() {
        if (status == CampaignStatus.ENDED) {
            throw new IllegalStateException("Campaign is already ended");
        }
        status = CampaignStatus.ENDED;
    }

    public boolean canBeEdited() {
        return status == CampaignStatus.ACTIVE || status == CampaignStatus.PAUSED;
    }

    public void updateDetails(String name, String description, LocalDateTime startTime, LocalDateTime endTime) {
        if (!canBeEdited()) {
            throw new IllegalStateException("Only ACTIVE or PAUSED campaigns can be edited");
        }
        setName(name);
        setDescription(description);
        setSchedule(startTime, endTime);
    }

    private void setSchedule(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Campaign startTime and endTime are required");
        }
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("Campaign endTime must be after startTime");
        }
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Campaign name cannot be blank");
        }
        this.name = name.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Campaign description cannot be blank");
        }
        this.description = description.trim();
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        if (company == null) {
            throw new IllegalArgumentException("Campaign company cannot be null");
        }
        this.company = company;
    }

    public List<VoucherType> getVoucherTypes() {
    return voucherTypes;
    }

    public void setVoucherTypes(List<VoucherType> voucherTypes) {
        this.voucherTypes = voucherTypes;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        setSchedule(startTime, this.endTime);
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        setSchedule(this.startTime, endTime);
    }

    public CampaignStatus getStatus() {
        return status;
    }

    public void setStatus(CampaignStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Campaign status cannot be null");
        }
        this.status = status;
    }
}
