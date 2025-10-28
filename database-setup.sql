-- Job Search Database Setup Script
-- Run this script to create the database and user

-- Create database
CREATE DATABASE jobsearch;

-- Create user
CREATE USER jobsearch_user WITH PASSWORD 'jobsearch_password';

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE jobsearch TO jobsearch_user;

-- Connect to the jobsearch database
\c jobsearch

-- Grant schema privileges
GRANT ALL ON SCHEMA public TO jobsearch_user;