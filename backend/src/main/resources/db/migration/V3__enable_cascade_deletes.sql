-- 1) Company delete should remove child campaigns automatically.
ALTER TABLE campaigns
    DROP CONSTRAINT fk_campaigns_company;

ALTER TABLE campaigns
    ADD CONSTRAINT fk_campaigns_company
    FOREIGN KEY (company_id)
    REFERENCES companies(id)
    ON DELETE CASCADE;

-- 2) Campaign delete should remove child voucher types automatically.
ALTER TABLE voucher_types
    DROP CONSTRAINT fk_voucher_type_campaign;

ALTER TABLE voucher_types
    ADD CONSTRAINT fk_voucher_type_campaign
    FOREIGN KEY (campaign_id)
    REFERENCES campaigns(id)
    ON DELETE CASCADE;

-- 3) VoucherType delete should remove child redemptions automatically.
ALTER TABLE redemptions
    DROP CONSTRAINT fk_redemption_voucher_type;

ALTER TABLE redemptions
    ADD CONSTRAINT fk_redemption_voucher_type
    FOREIGN KEY (voucher_type_id)
    REFERENCES voucher_types(id)
    ON DELETE CASCADE;

-- Optional safety for company deletion: users should not block deleting a company.
-- company_id is nullable, so ON DELETE SET NULL is the least disruptive behavior.
ALTER TABLE users
    DROP CONSTRAINT fk_users_company;

ALTER TABLE users
    ADD CONSTRAINT fk_users_company
    FOREIGN KEY (company_id)
    REFERENCES companies(id)
    ON DELETE SET NULL;
