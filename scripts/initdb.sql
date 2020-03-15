CREATE DATABASE piplanning;
\connect piplanning;
CREATE USER agile LOGIN;

-- RDS
GRANT agile TO postgres;
ALTER USER agile WITH PASSWORD '';

CREATE SCHEMA agile AUTHORIZATION agile;
