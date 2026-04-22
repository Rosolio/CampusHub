CREATE DATABASE IF NOT EXISTS campusaid CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE campusaid;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS messages;
DROP TABLE IF EXISTS user_point_records;
DROP TABLE IF EXISTS task_comments;
DROP TABLE IF EXISTS task_participants;
DROP TABLE IF EXISTS tasks;
DROP TABLE IF EXISTS user_settings;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    student_id VARCHAR(64) NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    avatar_url VARCHAR(500) NULL,
    major VARCHAR(100) NULL,
    score DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    points INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_student_id (student_id),
    UNIQUE KEY uk_users_email (email)
);

CREATE TABLE user_settings (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    notification_enabled TINYINT(1) NOT NULL DEFAULT 1,
    theme VARCHAR(32) NOT NULL DEFAULT 'light',
    language VARCHAR(32) NOT NULL DEFAULT 'zh-CN',
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_settings_user_id (user_id)
);

CREATE TABLE user_point_records (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    points INT NOT NULL,
    change_type VARCHAR(64) NOT NULL,
    description VARCHAR(255) NOT NULL,
    reference_type VARCHAR(64) NULL,
    reference_id BIGINT NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    KEY idx_user_point_records_user_id (user_id),
    KEY idx_user_point_records_change_type (change_type),
    KEY idx_user_point_records_created_at (created_at)
);

CREATE TABLE tasks (
    id BIGINT NOT NULL AUTO_INCREMENT,
    requester_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(100) NULL,
    task_mode VARCHAR(32) NOT NULL DEFAULT 'task',
    badge_primary VARCHAR(100) NULL,
    badge_secondary VARCHAR(100) NULL,
    location_text VARCHAR(255) NULL,
    time_text VARCHAR(255) NULL,
    reward_title VARCHAR(100) NULL,
    reward_text VARCHAR(255) NULL,
    impact_title VARCHAR(100) NULL,
    impact_text VARCHAR(255) NULL,
    map_image_url VARCHAR(500) NULL,
    contact_info VARCHAR(255) NULL,
    status VARCHAR(32) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    KEY idx_tasks_requester_id (requester_id),
    KEY idx_tasks_status (status),
    KEY idx_tasks_task_mode (task_mode)
);

CREATE TABLE task_participants (
    id BIGINT NOT NULL AUTO_INCREMENT,
    task_id BIGINT NOT NULL,
    participant_id BIGINT NOT NULL,
    role VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    KEY idx_task_participants_task_id (task_id),
    KEY idx_task_participants_participant_id (participant_id)
);

CREATE TABLE messages (
    id BIGINT NOT NULL AUTO_INCREMENT,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    task_id BIGINT NULL,
    content TEXT NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    KEY idx_messages_receiver_id (receiver_id),
    KEY idx_messages_sender_id (sender_id),
    KEY idx_messages_task_id (task_id),
    KEY idx_messages_status (status)
);

CREATE TABLE task_comments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    task_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    parent_id BIGINT NULL,
    content TEXT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    KEY idx_task_comments_task_id (task_id),
    KEY idx_task_comments_author_id (author_id),
    KEY idx_task_comments_parent_id (parent_id)
);

INSERT INTO users (id, student_id, name, email, password, avatar_url, major, score, points, created_at, updated_at)
VALUES
    (1, '20239999', '测试用户一', 'user1@example.com', '$2y$10$pDyLu1we/8IENxGEMGmBvOr4sKtzrDSfwG2TeailJfy2ZP05iFjZu', NULL, 'Computer Science', 10.00, 100, NOW(), NOW()),
    (2, '20239998', '测试用户二', 'user2@example.com', '$2y$10$pDyLu1we/8IENxGEMGmBvOr4sKtzrDSfwG2TeailJfy2ZP05iFjZu', NULL, 'Mathematics', 8.50, 80, NOW(), NOW()),
    (3, '20230001', '测试用户', 'test@example.com', '$2y$10$pDyLu1we/8IENxGEMGmBvOr4sKtzrDSfwG2TeailJfy2ZP05iFjZu', NULL, 'Software Engineering', 0.00, 0, NOW(), NOW());

INSERT INTO user_settings (id, user_id, notification_enabled, theme, language, updated_at)
VALUES
    (1, 1, 1, 'light', 'zh-CN', NOW()),
    (2, 2, 1, 'light', 'zh-CN', NOW()),
    (3, 3, 1, 'light', 'zh-CN', NOW());

ALTER TABLE users AUTO_INCREMENT = 4;
ALTER TABLE user_settings AUTO_INCREMENT = 4;

SET FOREIGN_KEY_CHECKS = 1;
