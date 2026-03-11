package com.vouchera.backend.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.vouchera.backend.enums.RedemptionStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "redemptions",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "voucher_type_id"})
    },
    indexes = {
        @Index(
            name = "idx_redemption_voucher_type_status_redeemed_at",
            columnList = "voucher_type_id, status, redeemed_at"
        ),
        @Index(
            name = "idx_redemption_user_redeemed_at",
            columnList = "user_id, redeemed_at"
        )
    }
)
public class Redemption {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "user_id", 
        nullable = false, 
        foreignKey = @ForeignKey(name = "fk_redemption_user")
    )
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "voucher_type_id", 
        nullable = false, 
        foreignKey = @ForeignKey(name = "fk_redemption_voucher_type")
    )
    private VoucherType voucherType;

    @Column(nullable = false, updatable = false)
    private LocalDateTime redeemedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RedemptionStatus status;

    @PrePersist
    protected void onCreate() {
        redeemedAt = LocalDateTime.now();
        if (status == null) {
            status = RedemptionStatus.CLAIMED;
        }
    }

    @Column(nullable = true, updatable = true)
    private LocalDateTime usedAt;

    public Redemption() {}

    public Redemption(User user, VoucherType voucherType) {
        this.user = user;
        this.voucherType = voucherType;
    }

    public void markUsed() {
        if (this.status != RedemptionStatus.CLAIMED) {
            throw new IllegalStateException(
                "Redemption cannot be marked as USED because current status is " + this.status
            );
        }

        this.status = RedemptionStatus.USED;
        this.usedAt = LocalDateTime.now();
    }

    public void markExpired() {
        if (status != RedemptionStatus.CLAIMED) {
            throw new IllegalStateException(
                "Only CLAIMED redemptions can be expired, current status: " + status
            );
        }
        status = RedemptionStatus.EXPIRED;
    }

    public boolean isTerminal() {
        return status == RedemptionStatus.USED || status == RedemptionStatus.EXPIRED;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public VoucherType getVoucherType() {
        return voucherType;
    }

    public void setVoucherType(VoucherType voucherType) {
        this.voucherType = voucherType;
    }

    public LocalDateTime getRedeemedAt() {
        return redeemedAt;
    }

    public void setRedeemedAt(LocalDateTime redeemedAt) {
        this.redeemedAt = redeemedAt;
    }

    public RedemptionStatus getStatus() {
        return status;
    }

    public void setStatus(RedemptionStatus status) {
        this.status = status;
    }

    public LocalDateTime getUsedAt() {
        return usedAt;
    }

    public void setUsedAt(LocalDateTime used_at) {
        this.usedAt = used_at;
    }
}
