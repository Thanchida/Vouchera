-- Performance indexes for common queries
CREATE INDEX idx_campaign_company_status_start ON campaigns (company_id, status, start_time);
CREATE INDEX idx_voucher_type_campaign_remaining ON voucher_types (campaign_id, remaining_quota);
CREATE INDEX idx_redemption_voucher_type_status_redeemed_at ON redemptions (voucher_type_id, status, redeemed_at);
CREATE INDEX idx_redemption_user_redeemed_at ON redemptions (user_id, redeemed_at);
CREATE INDEX idx_companies_name ON companies (name);

-- Business rule constraints (enforced at database level)
ALTER TABLE campaigns
    ADD CONSTRAINT chk_campaign_time_range CHECK (end_time > start_time);

ALTER TABLE voucher_types
    ADD CONSTRAINT chk_voucher_discount CHECK (discount_percent BETWEEN 1 AND 100),
    ADD CONSTRAINT chk_voucher_quota_positive CHECK (total_quota > 0),
    ADD CONSTRAINT chk_voucher_remaining_valid CHECK (remaining_quota >= 0 AND remaining_quota <= total_quota);
