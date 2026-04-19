-- Portfolio Database Schema v1.0.0
-- PostgreSQL DDL for Portfolio Application

-- Languages table
CREATE TABLE IF NOT EXISTS languages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(5) NOT NULL UNIQUE,
    name VARCHAR(30) NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT false,
    enabled BOOLEAN NOT NULL DEFAULT true
);

-- Roles table
CREATE TABLE IF NOT EXISTS roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
);

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(40),
    password VARCHAR(255) NOT NULL
);

-- Users Roles junction table
CREATE TABLE IF NOT EXISTS users_roles (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_users_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_users_roles_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Portfolio Basics table
CREATE TABLE IF NOT EXISTS basics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(120) NOT NULL,
    image TEXT NOT NULL,
    summary VARCHAR(600) NOT NULL,
    status VARCHAR(80) NOT NULL,
    email VARCHAR(255) NOT NULL,
    linkedIn VARCHAR(255),
    github VARCHAR(255),
    language_id UUID NOT NULL,
    CONSTRAINT fk_ basics_language FOREIGN KEY (language_id) REFERENCES languages(id) ON DELETE CASCADE
);

-- About me table
CREATE TABLE IF NOT EXISTS aboutme (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    summary VARCHAR(2000) NOT NULL,
    language_id UUID NOT NULL UNIQUE,
    CONSTRAINT fk_aboutme_language FOREIGN KEY (language_id) REFERENCES languages(id) ON DELETE CASCADE
);

-- Portfolio Work Experiences table
CREATE TABLE IF NOT EXISTS works (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(120) NOT NULL,
    url VARCHAR(512),
    position VARCHAR(120) NOT NULL,
    summary VARCHAR(2000) NOT NULL,
    start_date VARCHAR(20) NOT NULL,
    end_date VARCHAR(20),
    language_id UUID NOT NULL,
    CONSTRAINT fk_ work_language FOREIGN KEY (language_id) REFERENCES languages(id) ON DELETE CASCADE
);

-- Portfolio Work Highlights table (related to work experiences)
CREATE TABLE IF NOT EXISTS work_highlights (
    work_id UUID NOT NULL,
    highlight VARCHAR(500) NOT NULL,
    CONSTRAINT pk_ work_highlights PRIMARY KEY (work_id, highlight),
    CONSTRAINT fk_ work_highlights_work FOREIGN KEY (work_id) REFERENCES  works(id) ON DELETE CASCADE
);

-- Portfolio Projects table
CREATE TABLE IF NOT EXISTS projects (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(120) NOT NULL,
    url VARCHAR(512),
    summary VARCHAR(2000) NOT NULL,
    language_id UUID NOT NULL,
    CONSTRAINT fk_ projects_language FOREIGN KEY (language_id) REFERENCES languages(id) ON DELETE CASCADE
);

-- Portfolio Project Highlights table (related to projects)
CREATE TABLE IF NOT EXISTS project_highlights (
    project_id UUID NOT NULL,
    highlight VARCHAR(100) NOT NULL,
    CONSTRAINT pk_ project_highlights PRIMARY KEY (project_id, highlight),
    CONSTRAINT fk_ project_highlights_project FOREIGN KEY (project_id) REFERENCES  projects(id) ON DELETE CASCADE
);

-- Portfolio Skills table
CREATE TABLE IF NOT EXISTS skills (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    language_id UUID NOT NULL,
    CONSTRAINT fk_ skills_language FOREIGN KEY (language_id) REFERENCES languages(id) ON DELETE CASCADE
);

-- Portfolio Education Entries table
CREATE TABLE IF NOT EXISTS education (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    institution VARCHAR(150) NOT NULL,
    url VARCHAR(512),
    area VARCHAR(200) NOT NULL,
    start_date VARCHAR(20) NOT NULL,
    end_date VARCHAR(20),
    language_id UUID NOT NULL,
    CONSTRAINT fk_ education_language FOREIGN KEY (language_id) REFERENCES languages(id) ON DELETE CASCADE
);

-- Portfolio Education Highlights table (related to education entries)
CREATE TABLE IF NOT EXISTS education_highlights (
    education_id UUID NOT NULL,
    highlight VARCHAR(150) NOT NULL,
    CONSTRAINT pk_ education_highlights PRIMARY KEY (education_id, highlight),
    CONSTRAINT fk_ education_highlights_entry FOREIGN KEY (education_id) REFERENCES  education(id) ON DELETE CASCADE
);

-- Telemetry Visits table
CREATE TABLE IF NOT EXISTS telemetry_visits (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    accept_language VARCHAR(255),
    browser VARCHAR(64),
    city VARCHAR(128),
    country VARCHAR(64),
    device_type VARCHAR(32),
    ip_address VARCHAR(64),
    operating_system VARCHAR(64),
    path VARCHAR(255),
    query VARCHAR(1000),
    region VARCHAR(128),
    source VARCHAR(512),
    user_agent VARCHAR(2000),
    visited_at VARCHAR(40) NOT NULL,
    visitor_id VARCHAR(64) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_telemetry_visits_visited_at ON telemetry_visits(visited_at);
CREATE INDEX IF NOT EXISTS idx_telemetry_visits_visitor_id_visited_at ON telemetry_visits(visitor_id, visited_at);
CREATE INDEX IF NOT EXISTS idx_telemetry_visits_ip_ua_visited_at ON telemetry_visits(ip_address, user_agent, visited_at);

-- Telemetry Logins table
CREATE TABLE IF NOT EXISTS telemetry_logins (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    logged_at VARCHAR(40) NOT NULL,
    username VARCHAR(255) NOT NULL,
    city VARCHAR(128),
    country VARCHAR(64),
    ip_address VARCHAR(64),
    region VARCHAR(128),
    user_id UUID,
    CONSTRAINT fk_telemetry_logins_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_telemetry_logins_user_id ON telemetry_logins(user_id);
CREATE INDEX IF NOT EXISTS idx_telemetry_logins_logged_at ON telemetry_logins(logged_at);
CREATE INDEX IF NOT EXISTS idx_telemetry_logins_username_logged_at ON telemetry_logins(username, logged_at);
