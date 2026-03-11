CREATE TABLE companies (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    company_id UUID,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_users_company FOREIGN KEY (company_id) REFERENCES companies(id)
);

CREATE TABLE campaigns (
    id UUID PRIMARY KEY,
    version BIGINT,
    name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    company_id UUID NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL,
    CONSTRAINT fk_campaigns_company FOREIGN KEY (company_id) REFERENCES companies(id)
);

CREATE TABLE voucher_types (
    id UUID PRIMARY KEY,
    version BIGINT,
    campaign_id UUID NOT NULL,
    discount_percent INTEGER NOT NULL,
    total_quota INTEGER NOT NULL,
    remaining_quota INTEGER NOT NULL,
    CONSTRAINT fk_voucher_type_campaign FOREIGN KEY (campaign_id) REFERENCES campaigns(id)
);

CREATE TABLE redemptions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    voucher_type_id UUID NOT NULL,
    redeemed_at TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL,
    used_at TIMESTAMP,
    CONSTRAINT fk_redemption_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_redemption_voucher_type FOREIGN KEY (voucher_type_id) REFERENCES voucher_types(id),
    CONSTRAINT uq_redemptions_user_voucher UNIQUE (user_id, voucher_type_id)
);
