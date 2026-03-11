package com.vouchera.backend.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;


@Entity
@Table(
    name = "voucher_types",
    indexes = {@Index(name="idx_voucher_type_campaign_remaining", columnList="campaign_id, remaining_quota")}
)
public class VoucherType {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "campaign_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_voucher_type_campaign")
    )
    private Campaign campaign;

    @Column(nullable = false)
    private Integer discountPercent;

    @Column(nullable = false)
    private Integer totalQuota;

    @Column(nullable = false)
    private Integer remainingQuota;

    @Version
    private Long version;

    public VoucherType() {}

    public VoucherType(Campaign campaign, Integer discountPercent, Integer totalQuota) {
        if (campaign == null) {
            throw new IllegalArgumentException("Campaign cannot be null");
        }
        if (discountPercent == null || discountPercent < 1 || discountPercent > 100) {
            throw new IllegalArgumentException("Discount percent must be between 1 and 100");
        }
        if (totalQuota == null || totalQuota <= 0) {
            throw new IllegalArgumentException("Total quota must be greater than 0");
        }
        this.campaign = campaign;
        this.discountPercent = discountPercent;
        this.totalQuota = totalQuota;
        this.remainingQuota = totalQuota;
    }

    public boolean isAvailable() {
        return remainingQuota > 0;
    }

    public void claimOne() {
        if (!isAvailable()) {
            throw new IllegalStateException("Voucher sold out");
        }
        remainingQuota--;
    }

    public void restoreOne() {
        if (remainingQuota >= totalQuota) {
            throw new IllegalStateException("Cannot restore: already at maximum quota");
        }
        remainingQuota++;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Campaign getCampaign() {
        return campaign;
    }

    public void setCampaign(Campaign campaign) {
        this.campaign = campaign;
    }

    public Integer getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(Integer discountPercent) {
        this.discountPercent = discountPercent;
    }

    public Integer getTotalQuota() {
        return totalQuota;
    }

    public void setTotalQuota(Integer totalQuota) {
        this.totalQuota = totalQuota;
    }

    public Integer getRemainingQuota() {
        return remainingQuota;
    }

    public void setRemainingQuota(Integer remainingQuota) {
        this.remainingQuota = remainingQuota;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
