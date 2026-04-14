package com.example.sbtemplate.mono.infrastructure.out.jpa.repository;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PortfolioSchemaInitializer {

    private final JdbcTemplate jdbcTemplate;

    public void ensureForeignKeys() {
        if (!tableExists("languages")) {
            return;
        }

        jdbcTemplate.execute("PRAGMA foreign_keys=OFF");
        try {
            migrateTableIfMissingForeignKey(
                "portfolio_basics",
                "language_id",
                "languages",
                "id",
                """
                CREATE TABLE portfolio_basics (
                    id blob not null,
                    email varchar(255) not null,
                    image text not null,
                    label varchar(600) not null,
                    name varchar(120) not null,
                    status varchar(80) not null,
                    summary varchar(2000) not null,
                    language_id blob not null,
                    primary key (id),
                    constraint fk_portfolio_basics_language foreign key (language_id) references languages(id)
                )
                """,
                "id, email, image, label, name, status, summary, language_id"
            );
            jdbcTemplate.execute("CREATE UNIQUE INDEX IF NOT EXISTS uk_portfolio_basics_language ON portfolio_basics(language_id)");

            migrateTableIfMissingForeignKey(
                "portfolio_work_experiences",
                "language_id",
                "languages",
                "id",
                """
                CREATE TABLE portfolio_work_experiences (
                    id blob not null,
                    end_date varchar(20),
                    name varchar(120) not null,
                    position varchar(120) not null,
                    sort_order integer not null,
                    start_date varchar(20) not null,
                    summary varchar(2000) not null,
                    url varchar(512),
                    language_id blob not null,
                    primary key (id),
                    constraint fk_portfolio_work_language foreign key (language_id) references languages(id)
                )
                """,
                "id, end_date, name, position, sort_order, start_date, summary, url, language_id"
            );

            migrateTableIfMissingForeignKey(
                "portfolio_projects",
                "language_id",
                "languages",
                "id",
                """
                CREATE TABLE portfolio_projects (
                    id blob not null,
                    description varchar(2000) not null,
                    name varchar(120) not null,
                    sort_order integer not null,
                    url varchar(512),
                    language_id blob not null,
                    primary key (id),
                    constraint fk_portfolio_projects_language foreign key (language_id) references languages(id)
                )
                """,
                "id, description, name, sort_order, url, language_id"
            );

            migrateTableIfMissingForeignKey(
                "portfolio_skills",
                "language_id",
                "languages",
                "id",
                """
                CREATE TABLE portfolio_skills (
                    id blob not null,
                    name varchar(100) not null,
                    sort_order integer not null,
                    language_id blob not null,
                    primary key (id),
                    constraint fk_portfolio_skills_language foreign key (language_id) references languages(id)
                )
                """,
                "id, name, sort_order, language_id"
            );

            migrateTableIfMissingForeignKey(
                "portfolio_education_entries",
                "language_id",
                "languages",
                "id",
                """
                CREATE TABLE portfolio_education_entries (
                    id blob not null,
                    area varchar(200) not null,
                    end_date varchar(20),
                    institution varchar(150) not null,
                    sort_order integer not null,
                    start_date varchar(20) not null,
                    url varchar(512),
                    language_id blob not null,
                    primary key (id),
                    constraint fk_portfolio_education_language foreign key (language_id) references languages(id)
                )
                """,
                "id, area, end_date, institution, sort_order, start_date, url, language_id"
            );

            migrateTableIfMissingForeignKey(
                "portfolio_profiles",
                "basics_id",
                "portfolio_basics",
                "id",
                """
                CREATE TABLE portfolio_profiles (
                    id blob not null,
                    network varchar(50) not null,
                    sort_order integer not null,
                    url varchar(512) not null,
                    basics_id blob not null,
                    primary key (id),
                    constraint fk_portfolio_profiles_basics foreign key (basics_id) references portfolio_basics(id)
                )
                """,
                "id, network, sort_order, url, basics_id"
            );

            migrateTableIfMissingForeignKey(
                "portfolio_work_highlights",
                "work_id",
                "portfolio_work_experiences",
                "id",
                """
                CREATE TABLE portfolio_work_highlights (
                    work_id blob not null,
                    highlight varchar(500) not null,
                    constraint fk_portfolio_work_highlights_work foreign key (work_id) references portfolio_work_experiences(id)
                )
                """,
                "work_id, highlight"
            );

            migrateTableIfMissingForeignKey(
                "portfolio_project_highlights",
                "project_id",
                "portfolio_projects",
                "id",
                """
                CREATE TABLE portfolio_project_highlights (
                    project_id blob not null,
                    highlight varchar(100) not null,
                    constraint fk_portfolio_project_highlights_project foreign key (project_id) references portfolio_projects(id)
                )
                """,
                "project_id, highlight"
            );

            migrateTableIfMissingForeignKey(
                "portfolio_education_courses",
                "education_id",
                "portfolio_education_entries",
                "id",
                """
                CREATE TABLE portfolio_education_courses (
                    education_id blob not null,
                    course varchar(150) not null,
                    constraint fk_portfolio_education_courses_entry foreign key (education_id) references portfolio_education_entries(id)
                )
                """,
                "education_id, course"
            );
        } finally {
            jdbcTemplate.execute("PRAGMA foreign_keys=ON");
        }
    }

    private void migrateTableIfMissingForeignKey(
        String tableName,
        String columnName,
        String referencedTable,
        String referencedColumn,
        String createTableSql,
        String copyColumnsSql
    ) {
        if (!tableExists(tableName) || hasForeignKey(tableName, columnName, referencedTable, referencedColumn)) {
            return;
        }

        String backupTable = tableName + "_old_fk";
        jdbcTemplate.execute("DROP TABLE IF EXISTS " + backupTable);
        jdbcTemplate.execute("ALTER TABLE " + tableName + " RENAME TO " + backupTable);
        jdbcTemplate.execute(createTableSql);
        jdbcTemplate.execute("INSERT INTO " + tableName + " (" + copyColumnsSql + ") SELECT " + copyColumnsSql + " FROM " + backupTable);
        jdbcTemplate.execute("DROP TABLE " + backupTable);

        log.info("Added FK {}.{} -> {}.{}", tableName, columnName, referencedTable, referencedColumn);
    }

    private boolean tableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(1) FROM sqlite_master WHERE type='table' AND name = ?",
            Integer.class,
            tableName
        );
        return count != null && count > 0;
    }

    private boolean hasForeignKey(String tableName, String columnName, String referencedTable, String referencedColumn) {
        List<Map<String, Object>> foreignKeys = jdbcTemplate.queryForList("PRAGMA foreign_key_list('" + tableName + "')");
        for (Map<String, Object> foreignKey : foreignKeys) {
            String from = Objects.toString(foreignKey.get("from"), "");
            String table = Objects.toString(foreignKey.get("table"), "");
            String to = Objects.toString(foreignKey.get("to"), "");

            if (from.equalsIgnoreCase(columnName)
                && table.equalsIgnoreCase(referencedTable)
                && to.equalsIgnoreCase(referencedColumn)) {
                return true;
            }
        }
        return false;
    }
}
