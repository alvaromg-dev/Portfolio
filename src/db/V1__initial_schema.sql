CREATE TABLE IF NOT EXISTS languages (
    id blob NOT NULL,
    code varchar(5) NOT NULL UNIQUE,
    name varchar(30) NOT NULL,
    is_default INTEGER NOT NULL DEFAULT 0,
    enabled INTEGER NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS roles (
    id blob NOT NULL,
    code varchar(50) NOT NULL UNIQUE,
    description varchar(255),
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS users (
    id blob NOT NULL,
    avatar varchar(255),
    created_at timestamp NOT NULL,
    deleted_at timestamp,
    email varchar(255) NOT NULL UNIQUE,
    family_names varchar(40),
    given_names varchar(20) NOT NULL,
    nif varchar(10) NOT NULL,
    password varchar(255) NOT NULL,
    phone varchar(20),
    updated_at timestamp NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS portfolio_basics (
    id blob NOT NULL,
    email varchar(255) NOT NULL,
    image varchar(255) NOT NULL,
    label varchar(600) NOT NULL,
    name varchar(120) NOT NULL,
    status varchar(80) NOT NULL,
    summary varchar(2000) NOT NULL,
    language_id blob NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_portfolio_basics_language FOREIGN KEY (language_id) REFERENCES languages(id)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_portfolio_basics_language ON portfolio_basics(language_id);

CREATE TABLE IF NOT EXISTS portfolio_work_experiences (
    id blob NOT NULL,
    end_date varchar(20),
    name varchar(120) NOT NULL,
    position varchar(120) NOT NULL,
    sort_order integer NOT NULL,
    start_date varchar(20) NOT NULL,
    summary varchar(2000) NOT NULL,
    url varchar(512),
    language_id blob NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_portfolio_work_language FOREIGN KEY (language_id) REFERENCES languages(id)
);

CREATE TABLE IF NOT EXISTS portfolio_projects (
    id blob NOT NULL,
    description varchar(2000) NOT NULL,
    name varchar(120) NOT NULL,
    sort_order integer NOT NULL,
    url varchar(512),
    language_id blob NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_portfolio_projects_language FOREIGN KEY (language_id) REFERENCES languages(id)
);

CREATE TABLE IF NOT EXISTS portfolio_skills (
    id blob NOT NULL,
    name varchar(100) NOT NULL,
    sort_order integer NOT NULL,
    language_id blob NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_portfolio_skills_language FOREIGN KEY (language_id) REFERENCES languages(id)
);

CREATE TABLE IF NOT EXISTS portfolio_education_entries (
    id blob NOT NULL,
    area varchar(200) NOT NULL,
    end_date varchar(20),
    institution varchar(150) NOT NULL,
    sort_order integer NOT NULL,
    start_date varchar(20) NOT NULL,
    url varchar(512),
    language_id blob NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_portfolio_education_language FOREIGN KEY (language_id) REFERENCES languages(id)
);

CREATE TABLE IF NOT EXISTS portfolio_profiles (
    id blob NOT NULL,
    network varchar(50) NOT NULL,
    sort_order integer NOT NULL,
    url varchar(512) NOT NULL,
    basics_id blob NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_portfolio_profiles_basics FOREIGN KEY (basics_id) REFERENCES portfolio_basics(id)
);

CREATE TABLE IF NOT EXISTS portfolio_work_highlights (
    work_id blob NOT NULL,
    highlight varchar(500) NOT NULL,
    CONSTRAINT fk_portfolio_work_highlights_work FOREIGN KEY (work_id) REFERENCES portfolio_work_experiences(id)
);

CREATE TABLE IF NOT EXISTS portfolio_project_highlights (
    project_id blob NOT NULL,
    highlight varchar(100) NOT NULL,
    CONSTRAINT fk_portfolio_project_highlights_project FOREIGN KEY (project_id) REFERENCES portfolio_projects(id)
);

CREATE TABLE IF NOT EXISTS portfolio_education_courses (
    education_id blob NOT NULL,
    course varchar(150) NOT NULL,
    CONSTRAINT fk_portfolio_education_courses_entry FOREIGN KEY (education_id) REFERENCES portfolio_education_entries(id)
);

CREATE TABLE IF NOT EXISTS telemetry_visits (
    id blob NOT NULL,
    accept_language varchar(255),
    browser varchar(64),
    city varchar(128),
    country varchar(64),
    device_type varchar(32),
    ip_address varchar(64),
    operating_system varchar(64),
    path varchar(255),
    query varchar(1000),
    region varchar(128),
    source varchar(512),
    user_agent varchar(2000),
    visited_at varchar(40) NOT NULL,
    visitor_id varchar(64) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS users_roles (
    user_id blob NOT NULL,
    role_id blob NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_users_roles_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_users_roles_role FOREIGN KEY (role_id) REFERENCES roles(id)
);

CREATE TABLE IF NOT EXISTS telemetry_logins (
    id blob NOT NULL,
    logged_at varchar(40) NOT NULL,
    username varchar(255) NOT NULL,
    city varchar(128),
    country varchar(64),
    ip_address varchar(64),
    region varchar(128),
    user_id blob,
    PRIMARY KEY (id),
    CONSTRAINT fk_telemetry_logins_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS idx_telemetry_visits_visited_at ON telemetry_visits(visited_at);
CREATE INDEX IF NOT EXISTS idx_telemetry_visits_visitor_id_visited_at ON telemetry_visits(visitor_id, visited_at);
CREATE INDEX IF NOT EXISTS idx_telemetry_visits_ip_ua_visited_at ON telemetry_visits(ip_address, user_agent, visited_at);
CREATE INDEX IF NOT EXISTS idx_telemetry_logins_user_id ON telemetry_logins(user_id);
CREATE INDEX IF NOT EXISTS idx_telemetry_logins_logged_at ON telemetry_logins(logged_at);
CREATE INDEX IF NOT EXISTS idx_telemetry_logins_username_logged_at ON telemetry_logins(username, logged_at);
