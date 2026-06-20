-- ============================================
-- Smart Parking System - Database Initialization
-- ============================================
-- This script runs on first MySQL container startup.
-- The database is already created by the MYSQL_DATABASE env var,
-- so this just grants proper permissions.

CREATE DATABASE IF NOT EXISTS smart_parking_db;

CREATE USER IF NOT EXISTS 'parking_user'@'%' IDENTIFIED BY 'parking_pass';
GRANT ALL PRIVILEGES ON smart_parking_db.* TO 'parking_user'@'%';
FLUSH PRIVILEGES;
