CREATE TABLE api_keys
(
    id         BIGSERIAL PRIMARY KEY,
    key_value  VARCHAR(128) NOT NULL UNIQUE,
    owner      VARCHAR(100) NOT NULL,
    active     BOOLEAN      NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    expires_at TIMESTAMPTZ
);

CREATE INDEX idx_api_keys_value ON api_keys (key_value);

-- Seed keys for development
INSERT INTO api_keys (key_value, owner)
VALUES ('11111111-1111-1111-1111-111111111111', 'read-write'),
       ('2de01979-3ea6-4f18-a7a9-b065f25b252c', 'admin'),
       ('22222222-2222-2222-2222-222222222222', 'readonly'),
       ('33333333-3333-3333-3333-333333333333', 'revoked')
;
