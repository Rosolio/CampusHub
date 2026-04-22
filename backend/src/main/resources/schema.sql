CREATE TABLE IF NOT EXISTS users (
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

CREATE TABLE IF NOT EXISTS user_settings (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    notification_enabled TINYINT(1) NOT NULL DEFAULT 1,
    theme VARCHAR(32) NOT NULL DEFAULT 'light',
    language VARCHAR(32) NOT NULL DEFAULT 'zh-CN',
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_settings_user_id (user_id)
);

CREATE TABLE IF NOT EXISTS user_point_records (
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

CREATE TABLE IF NOT EXISTS tasks (
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
    like_count INT NOT NULL DEFAULT 0,
    expires_at DATETIME NULL,
    requester_completed_at DATETIME NULL,
    helper_completed_at DATETIME NULL,
    completed_at DATETIME NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    KEY idx_tasks_requester_id (requester_id),
    KEY idx_tasks_status (status),
    KEY idx_tasks_task_mode (task_mode)
);

SET @add_tasks_category = IF(
    EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'tasks'
          AND COLUMN_NAME = 'category'
    ),
    'SELECT 1',
    'ALTER TABLE tasks ADD COLUMN category VARCHAR(100) NULL AFTER description'
);
PREPARE stmt FROM @add_tasks_category;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_tasks_task_mode = IF(
    EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'tasks'
          AND COLUMN_NAME = 'task_mode'
    ),
    'SELECT 1',
    'ALTER TABLE tasks ADD COLUMN task_mode VARCHAR(32) NOT NULL DEFAULT ''task'' AFTER category'
);
PREPARE stmt FROM @add_tasks_task_mode;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE tasks
SET category = CASE
        WHEN badge_secondary = '校园配送' THEN '跑腿代办'
        WHEN badge_secondary = '闲置交换' THEN '二手闲置'
        WHEN badge_secondary = '信息求助' THEN '打听求助'
        WHEN badge_secondary = '社交互助' THEN '恋爱交友'
        WHEN badge_secondary = '兼职机会' THEN '兼职招聘'
        ELSE COALESCE(category, badge_secondary, '跑腿代办')
    END
WHERE category IS NULL OR category = '';

UPDATE tasks
SET task_mode = CASE
        WHEN category = '跑腿代办' THEN 'task'
        ELSE 'topic'
    END
WHERE task_mode IS NULL OR task_mode = '';

SET @add_tasks_contact_info = IF(
    EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'tasks'
          AND COLUMN_NAME = 'contact_info'
    ),
    'SELECT 1',
    'ALTER TABLE tasks ADD COLUMN contact_info VARCHAR(255) NULL AFTER map_image_url'
);
PREPARE stmt FROM @add_tasks_contact_info;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_tasks_like_count = IF(
    EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'tasks'
          AND COLUMN_NAME = 'like_count'
    ),
    'SELECT 1',
    'ALTER TABLE tasks ADD COLUMN like_count INT NOT NULL DEFAULT 0 AFTER status'
);
PREPARE stmt FROM @add_tasks_like_count;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_tasks_expires_at = IF(
    EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'tasks'
          AND COLUMN_NAME = 'expires_at'
    ),
    'SELECT 1',
    'ALTER TABLE tasks ADD COLUMN expires_at DATETIME NULL AFTER like_count'
);
PREPARE stmt FROM @add_tasks_expires_at;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_tasks_requester_completed_at = IF(
    EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'tasks'
          AND COLUMN_NAME = 'requester_completed_at'
    ),
    'SELECT 1',
    'ALTER TABLE tasks ADD COLUMN requester_completed_at DATETIME NULL AFTER expires_at'
);
PREPARE stmt FROM @add_tasks_requester_completed_at;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_tasks_helper_completed_at = IF(
    EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'tasks'
          AND COLUMN_NAME = 'helper_completed_at'
    ),
    'SELECT 1',
    'ALTER TABLE tasks ADD COLUMN helper_completed_at DATETIME NULL AFTER requester_completed_at'
);
PREPARE stmt FROM @add_tasks_helper_completed_at;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_tasks_completed_at = IF(
    EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'tasks'
          AND COLUMN_NAME = 'completed_at'
    ),
    'SELECT 1',
    'ALTER TABLE tasks ADD COLUMN completed_at DATETIME NULL AFTER helper_completed_at'
);
PREPARE stmt FROM @add_tasks_completed_at;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS task_participants (
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

CREATE TABLE IF NOT EXISTS messages (
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

CREATE TABLE IF NOT EXISTS task_comments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    task_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    parent_id BIGINT NULL,
    content TEXT NOT NULL,
    like_count INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    KEY idx_task_comments_task_id (task_id),
    KEY idx_task_comments_author_id (author_id),
    KEY idx_task_comments_parent_id (parent_id)
);

SET @add_task_comments_like_count = IF(
    EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'task_comments'
          AND COLUMN_NAME = 'like_count'
    ),
    'SELECT 1',
    'ALTER TABLE task_comments ADD COLUMN like_count INT NOT NULL DEFAULT 0 AFTER content'
);
PREPARE stmt FROM @add_task_comments_like_count;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS task_likes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    task_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_task_likes_task_user (task_id, user_id),
    KEY idx_task_likes_task_id (task_id),
    KEY idx_task_likes_user_id (user_id)
);

CREATE TABLE IF NOT EXISTS task_comment_likes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    comment_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_task_comment_likes_comment_user (comment_id, user_id),
    KEY idx_task_comment_likes_comment_id (comment_id),
    KEY idx_task_comment_likes_user_id (user_id)
);

CREATE TABLE IF NOT EXISTS task_reviews (
    id BIGINT NOT NULL AUTO_INCREMENT,
    task_id BIGINT NOT NULL,
    reviewer_id BIGINT NOT NULL,
    reviewee_id BIGINT NOT NULL,
    reviewer_role VARCHAR(32) NOT NULL,
    rating INT NOT NULL,
    content TEXT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_task_reviews_task_reviewer (task_id, reviewer_id),
    KEY idx_task_reviews_task_id (task_id),
    KEY idx_task_reviews_reviewee_id (reviewee_id)
);
