-- =============================================================================
-- V2__seed_api_keys.sql
-- Seeds initial API keys for development / testing.
--
-- API keys are stored as SHA-256 hashes.
-- The actual raw keys below are used only during development.
--
-- RAW KEY                        PERMISSION    USE CASE
-- ---------------------------------------------------------------------------
-- dev-read-write-key-12345       READ_WRITE    Full access (admin/backend)
-- dev-read-only-key-67890        READ          Read-only consumers
-- dev-revoked-key-00000          NO_ACCESS     Revoked / disabled key
--
-- In production: generate keys with `openssl rand -hex 32` and hash them
-- with: echo -n "YOUR_KEY" | sha256sum
-- Then insert only the hash.
-- =============================================================================

INSERT INTO api_key (key_hash, description, permission, active)
VALUES
    -- SHA-256 of 'dev-read-write-key-12345'
    (encode(sha256('dev-read-write-key-12345'::bytea), 'hex'),
     'Development full-access key',
     'READ_WRITE',
     TRUE),

    -- SHA-256 of 'dev-read-only-key-67890'
    (encode(sha256('dev-read-only-key-67890'::bytea), 'hex'),
     'Development read-only key',
     'READ',
     TRUE),

    -- SHA-256 of 'dev-revoked-key-00000'
    (encode(sha256('dev-revoked-key-00000'::bytea), 'hex'),
     'Revoked / no-access key',
     'NO_ACCESS',
     FALSE);