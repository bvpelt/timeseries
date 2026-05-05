CREATE TABLE api_keys
(
    id         BIGSERIAL PRIMARY KEY,
    key_value  VARCHAR(128) NOT NULL UNIQUE,
    owner      VARCHAR(100) NOT NULL,
    permission VARCHAR(20)  NOT NULL -- READ, READ_WRITE, NO_ACCESS
        CHECK (permission IN ('READ', 'READ_WRITE', 'NO_ACCESS', 'ADMIN')),
    active     BOOLEAN      NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    expires_at TIMESTAMPTZ
);

CREATE INDEX idx_api_keys_value ON api_keys (key_value);

-- Seed keys for development
INSERT INTO api_keys (key_value, owner, permission)
VALUES ('dev-read-write-key-001', 'developer', 'READ_WRITE'),
       ('dev-read-only-key-001', 'readonly', 'READ'),
       ('dev-no-access-key-001', 'revoked', 'NO_ACCESS'),
       ('bvpelt', 'developer', 'ADMIN');