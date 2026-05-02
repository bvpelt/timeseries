# Database creation

```bash
sudo -u postgres psql
```

```sql

-- Maak de database aan
CREATE DATABASE timeseries;

-- Maak de rollen/users aan
CREATE ROLE bvpelt WITH LOGIN;
CREATE ROLE rwservice WITH LOGIN PASSWORD 'je_wachtwoord_hier';
CREATE ROLE rservice WITH LOGIN PASSWORD 'je_wachtwoord_hier';

-- Maak bvpelt de eigenaar van de database
ALTER DATABASE timeseries OWNER TO bvpelt;

\c timeseries

-- Toegang tot de database en het schema
GRANT CONNECT ON DATABASE timeseries TO rwservice;
GRANT USAGE ON SCHEMA public TO rwservice;

-- Rechten voor bestaande tabellen
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO rwservice;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO rwservice;

-- Rechten voor toekomstige tabellen (automatisch)
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO rwservice;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT USAGE, SELECT ON SEQUENCES TO rwservice;

-- Toegang tot de database en het schema
GRANT CONNECT ON DATABASE timeseries TO rservice;
GRANT USAGE ON SCHEMA public TO rservice;

-- Rechten voor bestaande tabellen
GRANT SELECT ON ALL TABLES IN SCHEMA public TO rservice;

-- Rechten voor toekomstige tabellen (automatisch)
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO rservice;
```