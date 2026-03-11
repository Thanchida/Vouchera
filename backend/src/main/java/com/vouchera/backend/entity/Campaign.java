package com.vouchera.backend.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.vouchera.backend.enums.CampaignStatus;

import jakarta.persistence.Column;
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

    @OneToMany(mappedBy = "campaign", fetch = FetchType.LAZY)
    private List<VoucherType> voucherTypes;

    @Column(nullable = false, name = "start_time")
    private LocalDateTime startTime;

    @Column(nullable = false, name = "end_time")
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CampaignStatus status;

    public Campaign() {}

    public Campaign(String name, String description, Company company, LocalDateTime startTime, LocalDateTime endTime, CampaignStatus status) {

        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("Campaign endTime must be after startTime");
        }

        this.name = name;
        this.description = description;
        this.company = company;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
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
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
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
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public CampaignStatus getStatus() {
        return status;
    }

    public void setStatus(CampaignStatus status) {
        this.status = status;
    }
}
