-- =============================================================================
-- V1__create_bitemporal_schema.sql
-- Bitemporal schema following Richard Snodgrass's model.
--
-- Each table stores TWO independent time axes:
--   valid_time    (VT): when the fact is true in reality  [user-controlled]
--   transaction_time (TT): when the fact was recorded in the DB [system-controlled]
--
-- Sentinel value for "open" / "until changed": 9999-12-31 23:59:59 UTC
-- A row is the "current" version when transaction_to = INFINITY.
-- =============================================================================

-- Enable UUID generation
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Reusable sentinel constant (used in CHECK constraints and index predicates)
-- Note: referenced as a literal in indexes below.

-- =============================================================================
-- PERSON
-- =============================================================================
CREATE TABLE person (
    -- Surrogate PK (internal only, never exposed in the API)
                        id                  BIGSERIAL               PRIMARY KEY,
    -- Stable business identifier (same UUID across all versions of a person)
                        uid                 UUID                    NOT NULL DEFAULT gen_random_uuid(),

    -- Business attributes
                        first_name          VARCHAR(100)            NOT NULL,
                        last_name           VARCHAR(100)            NOT NULL,
                        date_of_birth       DATE                    NOT NULL,

    -- Valid Time: when this fact was true in reality
                        valid_from          TIMESTAMPTZ             NOT NULL DEFAULT NOW(),
                        valid_to            TIMESTAMPTZ             NOT NULL DEFAULT '9999-12-31 23:59:59+00'::TIMESTAMPTZ,

    -- Transaction Time: when this record was written to the DB (system-controlled)
                        transaction_from    TIMESTAMPTZ             NOT NULL DEFAULT NOW(),
                        transaction_to      TIMESTAMPTZ             NOT NULL DEFAULT '9999-12-31 23:59:59+00'::TIMESTAMPTZ,

                        created_at          TIMESTAMPTZ             NOT NULL DEFAULT NOW(),

    -- Valid period must be valid
                        CONSTRAINT person_valid_period_check CHECK (valid_from < valid_to),
    -- Transaction period must be valid
                        CONSTRAINT person_transaction_period_check CHECK (transaction_from < transaction_to)
);

-- Index for point-in-time queries: WHERE uid = ? AND vt_contains AND tt_contains
CREATE INDEX idx_person_bitemporal ON person
    (uid, valid_from, valid_to, transaction_from, transaction_to);

-- Fast lookup of current versions (transaction_to = INFINITY)
CREATE INDEX idx_person_current ON person (uid, valid_from, valid_to)
    WHERE transaction_to = '9999-12-31 23:59:59+00'::TIMESTAMPTZ;

-- =============================================================================
-- ADDRESS
-- =============================================================================
CREATE TABLE address (
                         id                  BIGSERIAL               PRIMARY KEY,
                         uid                 UUID                    NOT NULL DEFAULT gen_random_uuid(),

                         street              VARCHAR(200)            NOT NULL,
                         house_number        VARCHAR(20)             NOT NULL,
                         postal_code         VARCHAR(20)             NOT NULL,
                         city                VARCHAR(100)            NOT NULL,

                         valid_from          TIMESTAMPTZ             NOT NULL DEFAULT NOW(),
                         valid_to            TIMESTAMPTZ             NOT NULL DEFAULT '9999-12-31 23:59:59+00'::TIMESTAMPTZ,
                         transaction_from    TIMESTAMPTZ             NOT NULL DEFAULT NOW(),
                         transaction_to      TIMESTAMPTZ             NOT NULL DEFAULT '9999-12-31 23:59:59+00'::TIMESTAMPTZ,

                         created_at          TIMESTAMPTZ             NOT NULL DEFAULT NOW(),

                         CONSTRAINT address_valid_period_check CHECK (valid_from < valid_to),
                         CONSTRAINT address_transaction_period_check CHECK (transaction_from < transaction_to)
);

CREATE INDEX idx_address_bitemporal ON address
    (uid, valid_from, valid_to, transaction_from, transaction_to);

CREATE INDEX idx_address_current ON address (uid, valid_from, valid_to)
    WHERE transaction_to = '9999-12-31 23:59:59+00'::TIMESTAMPTZ;

-- =============================================================================
-- AGREEMENT
-- =============================================================================
CREATE TABLE agreement (
                           id                  BIGSERIAL               PRIMARY KEY,
                           uid                 UUID                    NOT NULL DEFAULT gen_random_uuid(),

                           title               VARCHAR(200)            NOT NULL,
                           description         TEXT,
                           state               VARCHAR(10)             NOT NULL DEFAULT 'NEW',

                           valid_from          TIMESTAMPTZ             NOT NULL DEFAULT NOW(),
                           valid_to            TIMESTAMPTZ             NOT NULL DEFAULT '9999-12-31 23:59:59+00'::TIMESTAMPTZ,
                           transaction_from    TIMESTAMPTZ             NOT NULL DEFAULT NOW(),
                           transaction_to      TIMESTAMPTZ             NOT NULL DEFAULT '9999-12-31 23:59:59+00'::TIMESTAMPTZ,

                           created_at          TIMESTAMPTZ             NOT NULL DEFAULT NOW(),

                           CONSTRAINT agreement_state_check CHECK (state IN ('NEW', 'CHANGED', 'READY')),
                           CONSTRAINT agreement_valid_period_check CHECK (valid_from < valid_to),
                           CONSTRAINT agreement_transaction_period_check CHECK (transaction_from < transaction_to)
);

CREATE INDEX idx_agreement_bitemporal ON agreement
    (uid, valid_from, valid_to, transaction_from, transaction_to);

CREATE INDEX idx_agreement_current ON agreement (uid, valid_from, valid_to)
    WHERE transaction_to = '9999-12-31 23:59:59+00'::TIMESTAMPTZ;

-- =============================================================================
-- PERSON_ADDRESS  (N:M relation, itself bitemporal)
-- A person can have one HOME and multiple WORK addresses at any valid time.
-- =============================================================================
CREATE TABLE person_address (
                                id                  BIGSERIAL               PRIMARY KEY,
                                uid                 UUID                    NOT NULL DEFAULT gen_random_uuid(),

                                person_uid          UUID                    NOT NULL,
                                address_uid         UUID                    NOT NULL,
                                address_type        VARCHAR(4)              NOT NULL,   -- 'HOME' | 'WORK'

                                valid_from          TIMESTAMPTZ             NOT NULL DEFAULT NOW(),
                                valid_to            TIMESTAMPTZ             NOT NULL DEFAULT '9999-12-31 23:59:59+00'::TIMESTAMPTZ,
                                transaction_from    TIMESTAMPTZ             NOT NULL DEFAULT NOW(),
                                transaction_to      TIMESTAMPTZ             NOT NULL DEFAULT '9999-12-31 23:59:59+00'::TIMESTAMPTZ,

                                created_at          TIMESTAMPTZ             NOT NULL DEFAULT NOW(),

                                CONSTRAINT person_address_type_check CHECK (address_type IN ('HOME', 'WORK')),
                                CONSTRAINT person_address_valid_period_check CHECK (valid_from < valid_to),
                                CONSTRAINT person_address_transaction_period_check CHECK (transaction_from < transaction_to)
);

CREATE INDEX idx_person_address_person ON person_address (person_uid);
CREATE INDEX idx_person_address_address ON person_address (address_uid);
CREATE INDEX idx_person_address_bitemporal ON person_address
    (person_uid, address_uid, valid_from, valid_to, transaction_from, transaction_to);

-- Partial index: current HOME relations per person (for uniqueness enforcement in app logic)
CREATE INDEX idx_person_address_current_home ON person_address (person_uid, valid_from, valid_to)
    WHERE address_type = 'HOME'
      AND transaction_to = '9999-12-31 23:59:59+00'::TIMESTAMPTZ;

-- =============================================================================
-- PERSON_AGREEMENT  (N:M relation, itself bitemporal)
-- =============================================================================
CREATE TABLE person_agreement (
                                  id                  BIGSERIAL               PRIMARY KEY,
                                  uid                 UUID                    NOT NULL DEFAULT gen_random_uuid(),

                                  person_uid          UUID                    NOT NULL,
                                  agreement_uid       UUID                    NOT NULL,

                                  valid_from          TIMESTAMPTZ             NOT NULL DEFAULT NOW(),
                                  valid_to            TIMESTAMPTZ             NOT NULL DEFAULT '9999-12-31 23:59:59+00'::TIMESTAMPTZ,
                                  transaction_from    TIMESTAMPTZ             NOT NULL DEFAULT NOW(),
                                  transaction_to      TIMESTAMPTZ             NOT NULL DEFAULT '9999-12-31 23:59:59+00'::TIMESTAMPTZ,

                                  created_at          TIMESTAMPTZ             NOT NULL DEFAULT NOW(),

                                  CONSTRAINT person_agreement_valid_period_check CHECK (valid_from < valid_to),
                                  CONSTRAINT person_agreement_transaction_period_check CHECK (transaction_from < transaction_to)
);

CREATE INDEX idx_person_agreement_person ON person_agreement (person_uid);
CREATE INDEX idx_person_agreement_agreement ON person_agreement (agreement_uid);
CREATE INDEX idx_person_agreement_bitemporal ON person_agreement
    (person_uid, agreement_uid, valid_from, valid_to, transaction_from, transaction_to);

-- =============================================================================
-- API_KEY  (authentication + authorization)
-- =============================================================================
CREATE TABLE api_key (
                         id                  BIGSERIAL               PRIMARY KEY,
                         uid                 UUID                    NOT NULL DEFAULT gen_random_uuid(),

    -- Store only the SHA-256 hash of the key, never the raw value
                         key_hash            VARCHAR(64)             NOT NULL UNIQUE,

                         description         VARCHAR(255),
                         permission          VARCHAR(12)             NOT NULL DEFAULT 'NO_ACCESS',
                         active              BOOLEAN                 NOT NULL DEFAULT TRUE,

                         created_at          TIMESTAMPTZ             NOT NULL DEFAULT NOW(),
                         expires_at          TIMESTAMPTZ,            -- NULL = never expires

                         CONSTRAINT api_key_permission_check CHECK (permission IN ('READ', 'READ_WRITE', 'NO_ACCESS'))
);

CREATE INDEX idx_api_key_hash ON api_key (key_hash) WHERE active = TRUE;

-- =============================================================================
-- AUDIT / HELPER VIEWS  (current state only)
-- =============================================================================

-- Current persons (transaction-current, valid now)
CREATE VIEW v_person_current AS
SELECT *
FROM person
WHERE transaction_to = '9999-12-31 23:59:59+00'::TIMESTAMPTZ
  AND valid_from   <= NOW()
  AND valid_to      > NOW();

-- Current addresses
CREATE VIEW v_address_current AS
SELECT *
FROM address
WHERE transaction_to = '9999-12-31 23:59:59+00'::TIMESTAMPTZ
  AND valid_from   <= NOW()
  AND valid_to      > NOW();

-- Current agreements
CREATE VIEW v_agreement_current AS
SELECT *
FROM agreement
WHERE transaction_to = '9999-12-31 23:59:59+00'::TIMESTAMPTZ
  AND valid_from   <= NOW()
  AND valid_to      > NOW();

-- Current person-address links
CREATE VIEW v_person_address_current AS
SELECT pa.uid,
       pa.person_uid,
       pa.address_uid,
       pa.address_type,
       pa.valid_from,
       pa.valid_to,
       a.street,
       a.house_number,
       a.postal_code,
       a.city
FROM person_address pa
         JOIN address a ON a.uid = pa.address_uid
    AND a.transaction_to = '9999-12-31 23:59:59+00'::TIMESTAMPTZ
    AND a.valid_from   <= NOW()
    AND a.valid_to      > NOW()
WHERE pa.transaction_to = '9999-12-31 23:59:59+00'::TIMESTAMPTZ
  AND pa.valid_from   <= NOW()
  AND pa.valid_to      > NOW();