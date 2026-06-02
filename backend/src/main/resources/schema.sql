CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    student_id VARCHAR(64) NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    avatar_url MEDIUMTEXT NULL,
    major VARCHAR(100) NULL,
    role VARCHAR(32) NOT NULL DEFAULT 'USER',
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    disabled_reason VARCHAR(255) NULL,
    score DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    points INT NOT NULL DEFAULT 0,
    last_login_at DATETIME NULL,
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

CREATE TABLE IF NOT EXISTS announcements (
    id BIGINT NOT NULL AUTO_INCREMENT,
    author_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    pinned TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    KEY idx_announcements_author_id (author_id),
    KEY idx_announcements_pinned_created_at (pinned, created_at)
);

CREATE TABLE IF NOT EXISTS feedback (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    type VARCHAR(32) NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'open',
    priority VARCHAR(32) NOT NULL DEFAULT 'NORMAL',
    admin_reply TEXT NULL,
    admin_id BIGINT NULL,
    handled_at DATETIME NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    KEY idx_feedback_user_id (user_id),
    KEY idx_feedback_status_created_at (status, created_at)
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
    review_status VARCHAR(32) NOT NULL DEFAULT 'approved',
    review_note VARCHAR(255) NULL,
    reviewed_by BIGINT NULL,
    reviewed_at DATETIME NULL,
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
    KEY idx_tasks_task_mode (task_mode),
    KEY idx_tasks_review_status (review_status)
);

SET @add_users_role = IF(
    EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'role'
    ),
    'SELECT 1',
    'ALTER TABLE users ADD COLUMN role VARCHAR(32) NOT NULL DEFAULT ''USER'' AFTER major'
);
PREPARE stmt FROM @add_users_role;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_users_status = IF(
    EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'status'
    ),
    'SELECT 1',
    'ALTER TABLE users ADD COLUMN status VARCHAR(32) NOT NULL DEFAULT ''ACTIVE'' AFTER role'
);
PREPARE stmt FROM @add_users_status;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_users_disabled_reason = IF(
    EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'disabled_reason'
    ),
    'SELECT 1',
    'ALTER TABLE users ADD COLUMN disabled_reason VARCHAR(255) NULL AFTER status'
);
PREPARE stmt FROM @add_users_disabled_reason;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_users_last_login_at = IF(
    EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'last_login_at'
    ),
    'SELECT 1',
    'ALTER TABLE users ADD COLUMN last_login_at DATETIME NULL AFTER points'
);
PREPARE stmt FROM @add_users_last_login_at;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @modify_users_avatar_url = IF(
    EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'users'
          AND COLUMN_NAME = 'avatar_url'
          AND DATA_TYPE <> 'mediumtext'
    ),
    'ALTER TABLE users MODIFY COLUMN avatar_url MEDIUMTEXT NULL',
    'SELECT 1'
);
PREPARE stmt FROM @modify_users_avatar_url;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @normalize_users = IF(
    EXISTS (
        SELECT 1
        FROM users
        WHERE role IS NULL OR role = '' OR status IS NULL OR status = ''
        LIMIT 1
    ),
    'UPDATE users SET role = COALESCE(NULLIF(role, ''''), ''USER''), status = COALESCE(NULLIF(status, ''''), ''ACTIVE'')',
    'SELECT 1'
);
PREPARE stmt FROM @normalize_users;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

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

SET @normalize_tasks_category = IF(
    EXISTS (
        SELECT 1
        FROM tasks
        WHERE category IS NULL OR category = ''
        LIMIT 1
    ),
    'UPDATE tasks SET category = CASE
            WHEN badge_secondary = ''校园配送'' THEN ''跑腿代办''
            WHEN badge_secondary = ''闲置交换'' THEN ''二手闲置''
            WHEN badge_secondary = ''信息求助'' THEN ''打听求助''
            WHEN badge_secondary = ''社交互助'' THEN ''恋爱交友''
            WHEN badge_secondary = ''兼职机会'' THEN ''兼职招聘''
            ELSE COALESCE(category, badge_secondary, ''跑腿代办'')
        END
    WHERE category IS NULL OR category = ''''',
    'SELECT 1'
);
PREPARE stmt FROM @normalize_tasks_category;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @normalize_tasks_mode = IF(
    EXISTS (
        SELECT 1
        FROM tasks
        WHERE task_mode IS NULL OR task_mode = ''
        LIMIT 1
    ),
    'UPDATE tasks SET task_mode = CASE
            WHEN category IN (''跑腿代办'', ''学习辅导'') THEN ''task''
            ELSE ''topic''
        END
    WHERE task_mode IS NULL OR task_mode = ''''',
    'SELECT 1'
);
PREPARE stmt FROM @normalize_tasks_mode;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

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

SET @add_tasks_review_status = IF(
    EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'tasks'
          AND COLUMN_NAME = 'review_status'
    ),
    'SELECT 1',
    'ALTER TABLE tasks ADD COLUMN review_status VARCHAR(32) NOT NULL DEFAULT ''approved'' AFTER contact_info'
);
PREPARE stmt FROM @add_tasks_review_status;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_tasks_review_note = IF(
    EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'tasks'
          AND COLUMN_NAME = 'review_note'
    ),
    'SELECT 1',
    'ALTER TABLE tasks ADD COLUMN review_note VARCHAR(255) NULL AFTER review_status'
);
PREPARE stmt FROM @add_tasks_review_note;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_tasks_reviewed_by = IF(
    EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'tasks'
          AND COLUMN_NAME = 'reviewed_by'
    ),
    'SELECT 1',
    'ALTER TABLE tasks ADD COLUMN reviewed_by BIGINT NULL AFTER review_note'
);
PREPARE stmt FROM @add_tasks_reviewed_by;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_tasks_reviewed_at = IF(
    EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'tasks'
          AND COLUMN_NAME = 'reviewed_at'
    ),
    'SELECT 1',
    'ALTER TABLE tasks ADD COLUMN reviewed_at DATETIME NULL AFTER reviewed_by'
);
PREPARE stmt FROM @add_tasks_reviewed_at;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE tasks
SET review_status = COALESCE(NULLIF(review_status, ''), 'approved');

SET @create_announcements_table = IF(
    EXISTS (
        SELECT 1 FROM information_schema.TABLES
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'announcements'
    ),
    'SELECT 1',
    'CREATE TABLE announcements (
        id BIGINT NOT NULL AUTO_INCREMENT,
        author_id BIGINT NOT NULL,
        title VARCHAR(255) NOT NULL,
        content TEXT NOT NULL,
        pinned TINYINT(1) NOT NULL DEFAULT 1,
        created_at DATETIME NOT NULL,
        updated_at DATETIME NOT NULL,
        PRIMARY KEY (id),
        KEY idx_announcements_author_id (author_id),
        KEY idx_announcements_pinned_created_at (pinned, created_at)
    )'
);
PREPARE stmt FROM @create_announcements_table;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @create_feedback_table = IF(
    EXISTS (
        SELECT 1 FROM information_schema.TABLES
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'feedback'
    ),
    'SELECT 1',
    'CREATE TABLE feedback (
        id BIGINT NOT NULL AUTO_INCREMENT,
        user_id BIGINT NOT NULL,
        type VARCHAR(32) NOT NULL,
        title VARCHAR(255) NOT NULL,
        content TEXT NOT NULL,
        status VARCHAR(32) NOT NULL DEFAULT ''open'',
        priority VARCHAR(32) NOT NULL DEFAULT ''NORMAL'',
        admin_reply TEXT NULL,
        admin_id BIGINT NULL,
        handled_at DATETIME NULL,
        created_at DATETIME NOT NULL,
        updated_at DATETIME NOT NULL,
        PRIMARY KEY (id),
        KEY idx_feedback_user_id (user_id),
        KEY idx_feedback_status_created_at (status, created_at)
    )'
);
PREPARE stmt FROM @create_feedback_table;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_feedback_priority = IF(
    EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'feedback'
          AND COLUMN_NAME = 'priority'
    ),
    'SELECT 1',
    'ALTER TABLE feedback ADD COLUMN priority VARCHAR(32) NOT NULL DEFAULT ''NORMAL'' AFTER status'
);
PREPARE stmt FROM @add_feedback_priority;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE feedback
SET priority = CASE
        WHEN type IN ('ACCOUNT_REPORT', 'CONTENT_REPORT', 'TASK_DISPUTE') THEN 'HIGH'
        WHEN type = 'BUG' THEN 'HIGH'
        WHEN type = 'SUGGESTION' THEN 'NORMAL'
        ELSE 'NORMAL'
    END
WHERE priority IS NULL OR priority = '';

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

SET @add_messages_receiver_status_created = IF(
    EXISTS (
        SELECT 1
        FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'messages'
          AND INDEX_NAME = 'idx_messages_receiver_status_created'
    ),
    'SELECT 1',
    'ALTER TABLE messages ADD INDEX idx_messages_receiver_status_created (receiver_id, status, created_at)'
);
PREPARE stmt FROM @add_messages_receiver_status_created;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_messages_sender_created = IF(
    EXISTS (
        SELECT 1
        FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'messages'
          AND INDEX_NAME = 'idx_messages_sender_created'
    ),
    'SELECT 1',
    'ALTER TABLE messages ADD INDEX idx_messages_sender_created (sender_id, created_at)'
);
PREPARE stmt FROM @add_messages_sender_created;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS user_login_logs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    login_type VARCHAR(32) NOT NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    KEY idx_user_login_logs_user_id (user_id),
    KEY idx_user_login_logs_created_at (created_at)
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

SET @add_users_verified_status = IF(
    EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'verified_status'
    ),
    'SELECT 1',
    'ALTER TABLE users ADD COLUMN verified_status VARCHAR(20) NOT NULL DEFAULT ''NONE'' AFTER status'
);
PREPARE stmt FROM @add_users_verified_status;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS user_verifications (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL DEFAULT 'STUDENT',
    real_name VARCHAR(50) NULL,
    student_id VARCHAR(50) NULL,
    image_urls JSON NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reviewer_id BIGINT NULL,
    reject_reason VARCHAR(500) NULL,
    reviewed_at DATETIME NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    KEY idx_user_verifications_user_id (user_id),
    KEY idx_user_verifications_status (status)
);

CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    reference_type VARCHAR(50) NULL,
    reference_id BIGINT NULL,
    is_read TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    KEY idx_notifications_user_id_read (user_id, is_read),
    KEY idx_notifications_created_at (created_at)
);

CREATE TABLE IF NOT EXISTS task_favorites (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    task_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_task_favorites_user_task (user_id, task_id),
    KEY idx_task_favorites_user_id (user_id)
);

SELECT 1 FROM (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'tasks'
      AND COLUMN_NAME = 'image_urls'
) AS has_column
WHERE NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'tasks'
      AND COLUMN_NAME = 'image_urls'
);
SET @add_task_image_urls = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE()
       AND TABLE_NAME = 'tasks'
       AND COLUMN_NAME = 'image_urls') = 0,
    'ALTER TABLE tasks ADD COLUMN image_urls JSON NULL AFTER contact_info',
    'SELECT 1'
);
PREPARE stmt FROM @add_task_image_urls;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
