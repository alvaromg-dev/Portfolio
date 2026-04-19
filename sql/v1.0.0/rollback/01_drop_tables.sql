-- Portfolio Database Rollback v1.0.0
-- Script para eliminar todas las tablas y revertir a estado limpio

-- Desactivar constraints de integridad referencial temporalmente
SET session_replication_role = 'replica';

-- Eliminar tablas en orden inverso de dependencias
DROP TABLE IF EXISTS telemetry_logins CASCADE;
DROP TABLE IF EXISTS telemetry_visits CASCADE;
DROP TABLE IF EXISTS aboutme CASCADE;
DROP TABLE IF EXISTS education_highlights CASCADE;
DROP TABLE IF EXISTS education CASCADE;
DROP TABLE IF EXISTS skills CASCADE;
DROP TABLE IF EXISTS project_highlights CASCADE;
DROP TABLE IF EXISTS projects CASCADE;
DROP TABLE IF EXISTS work_highlights CASCADE;
DROP TABLE IF EXISTS works CASCADE;
DROP TABLE IF EXISTS basics CASCADE;
DROP TABLE IF EXISTS users_roles CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS roles CASCADE;
DROP TABLE IF EXISTS languages CASCADE;

-- Reactivar constraints
SET session_replication_role = 'origin';

-- Confirmar
SELECT 'Rollback completed successfully' AS status;
