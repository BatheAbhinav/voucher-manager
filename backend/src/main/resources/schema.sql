DROP TABLE IF EXISTS voucher_history CASCADE;
DROP TABLE IF EXISTS voucher_user_mapping CASCADE;
DROP TABLE IF EXISTS voucher CASCADE;
DROP TABLE IF EXISTS app_user CASCADE;
DROP TABLE IF EXISTS org CASCADE;
DROP TABLE IF EXISTS admin CASCADE;

CREATE TABLE admin (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE org (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name          VARCHAR(255) NOT NULL,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE app_user (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id      UUID NOT NULL REFERENCES org(id),
    email       VARCHAR(255) NOT NULL,
    name        VARCHAR(255),
    status      VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    attributes  JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (org_id, email)
);

CREATE TABLE voucher (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id           UUID NOT NULL REFERENCES org(id),
    code             VARCHAR(64) NOT NULL UNIQUE,
    title            VARCHAR(255),
    status           VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    scope            VARCHAR(32) NOT NULL DEFAULT 'FREE',
    starts_at        TIMESTAMPTZ,
    expires_at       TIMESTAMPTZ,
    max_redemptions  INTEGER,
    attributes       JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE voucher_user_mapping (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    voucher_id   UUID NOT NULL REFERENCES voucher(id),
    user_id      UUID NOT NULL REFERENCES app_user(id),
    status       VARCHAR(32) NOT NULL DEFAULT 'ASSIGNED',
    assigned_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    redeemed_at  TIMESTAMPTZ,
    UNIQUE (voucher_id, user_id)
);

CREATE TABLE voucher_history (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    voucher_id  UUID NOT NULL REFERENCES voucher(id),
    user_id     UUID REFERENCES app_user(id),
    event_type  VARCHAR(32) NOT NULL,
    event_data  JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
