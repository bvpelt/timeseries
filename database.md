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


-- 1. Grant connection rights to the database
GRANT CONNECT ON DATABASE timeseries TO testuser;

-- 2. Grant usage and the ability to CREATE objects in the target schema (default is usually 'public')
GRANT USAGE, CREATE ON SCHEMA public TO testuser;

-- 3. (Optional but recommended) Grant rights on existing sequences/tables if Flyway needs to alter them
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO testuser;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO testuser;
```

```bash
sudo -u postgres psql timeseries
psql (18.3 (Ubuntu 18.3-1.pgdg24.04+1))
Type "help" for help.

timeseries=# 
```

```sql
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION
```
`

```bash
mvn flyway:migrate -Dflyway.url=jdbc:postgresql://localhost:5432/timeseries -Dflyway.user=testuser -Dflyway.password=12345
 
 
 
mvn flyway:info -Dflyway.url=jdbc:postgresql://localhost:5432/timeseries -Dflyway.user=testuser -Dflyway.password=12345
[INFO] Scanning for projects...
[INFO] 
[INFO] ------------------------< com.bsoft:timeseries >------------------------
[INFO] Building timeseries 0.0.1-SNAPSHOT
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- flyway-maven-plugin:11.14.1:info (default-cli) @ timeseries ---
[INFO] Database: jdbc:postgresql://localhost:5432/timeseries (PostgreSQL 18.3)
[INFO] Schema version: 2
[INFO] 
[INFO] +-----------+---------+--------------------------+------+---------------------+---------+----------+
| Category  | Version | Description              | Type | Installed On        | State   | Undoable |
+-----------+---------+--------------------------+------+---------------------+---------+----------+
| Versioned | 1       | create bitemporal schema | SQL  | 2026-05-04 12:50:49 | Success | No       |
| Versioned | 2       | seed api keys            | SQL  | 2026-05-04 12:50:49 | Success | No       |
+-----------+---------+--------------------------+------+---------------------+---------+----------+

[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  2.225 s
[INFO] Finished at: 2026-05-04T12:53:31+02:00
[INFO] ------------------------------------------------------------------------

mvn flyway:validate -Dflyway.url=jdbc:postgresql://localhost:5432/timeseries -Dflyway.user=testuser -Dflyway.password=12345
[INFO] Scanning for projects...
[INFO] 
[INFO] ------------------------< com.bsoft:timeseries >------------------------
[INFO] Building timeseries 0.0.1-SNAPSHOT
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- flyway-maven-plugin:11.14.1:validate (default-cli) @ timeseries ---
[INFO] Database: jdbc:postgresql://localhost:5432/timeseries (PostgreSQL 18.3)
[INFO] Successfully validated 2 migrations (execution time 00:00.038s)
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  2.443 s
[INFO] Finished at: 2026-05-04T12:54:34+02:00
[INFO] ------------------------------------------------------------------------

mvn flyway:repair -Dflyway.url=jdbc:postgresql://localhost:5432/timeseries -Dflyway.user=testuser -Dflyway.password=12345
[INFO] Scanning for projects...
[INFO] 
[INFO] ------------------------< com.bsoft:timeseries >------------------------
[INFO] Building timeseries 0.0.1-SNAPSHOT
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- flyway-maven-plugin:11.14.1:repair (default-cli) @ timeseries ---
[INFO] Database: jdbc:postgresql://localhost:5432/timeseries (PostgreSQL 18.3)
[INFO] Repair of failed migration in Schema History table "public"."flyway_schema_history" not necessary. No failed migration detected.
[INFO] Successfully repaired schema history table "public"."flyway_schema_history" (execution time 00:00.065s).
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  2.353 s
[INFO] Finished at: 2026-05-04T12:55:03+02:00
[INFO] ------------------------------------------------------------------------

 ```