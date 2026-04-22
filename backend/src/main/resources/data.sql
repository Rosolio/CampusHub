INSERT INTO users (id, student_id, name, email, password, avatar_url, major, role, status, disabled_reason, score, points, last_login_at, created_at, updated_at)
VALUES
    (1, '20239999', '测试用户一', 'user1@example.com', '$2y$10$pDyLu1we/8IENxGEMGmBvOr4sKtzrDSfwG2TeailJfy2ZP05iFjZu', NULL, 'Computer Science', 'USER', 'ACTIVE', NULL, 10.00, 100, NOW(), NOW(), NOW()),
    (2, '20239998', '测试用户二', 'user2@example.com', '$2y$10$pDyLu1we/8IENxGEMGmBvOr4sKtzrDSfwG2TeailJfy2ZP05iFjZu', NULL, 'Mathematics', 'USER', 'ACTIVE', NULL, 8.50, 80, NOW(), NOW(), NOW()),
    (3, '20230001', '测试用户', 'test@example.com', '$2y$10$pDyLu1we/8IENxGEMGmBvOr4sKtzrDSfwG2TeailJfy2ZP05iFjZu', NULL, 'Software Engineering', 'USER', 'ACTIVE', NULL, 0.00, 0, NOW(), NOW(), NOW()),
    (4, 'admin', '系统管理员', 'admin@campusaid.local', '$2y$10$pDyLu1we/8IENxGEMGmBvOr4sKtzrDSfwG2TeailJfy2ZP05iFjZu', NULL, 'CampusAid Ops', 'ADMIN', 'ACTIVE', NULL, 10.00, 999, NOW(), NOW(), NOW())
ON DUPLICATE KEY UPDATE
    student_id = VALUES(student_id),
    name = VALUES(name),
    email = VALUES(email),
    password = VALUES(password),
    avatar_url = VALUES(avatar_url),
    major = VALUES(major),
    role = VALUES(role),
    status = VALUES(status),
    disabled_reason = VALUES(disabled_reason),
    score = VALUES(score),
    points = VALUES(points),
    last_login_at = VALUES(last_login_at),
    updated_at = NOW();

INSERT INTO user_settings (id, user_id, notification_enabled, theme, language, updated_at)
VALUES
    (1, 1, 1, 'light', 'zh-CN', NOW()),
    (2, 2, 1, 'light', 'zh-CN', NOW()),
    (3, 3, 1, 'light', 'zh-CN', NOW()),
    (4, 4, 1, 'light', 'zh-CN', NOW())
ON DUPLICATE KEY UPDATE
    user_id = VALUES(user_id),
    notification_enabled = VALUES(notification_enabled),
    theme = VALUES(theme),
    language = VALUES(language),
    updated_at = NOW();

ALTER TABLE users AUTO_INCREMENT = 5;
ALTER TABLE user_settings AUTO_INCREMENT = 5;

INSERT INTO announcements (id, author_id, title, content, pinned, created_at, updated_at)
VALUES
    (1, 4, 'CampusAid 社区公告', '欢迎使用校园互助平台。若你发现 bug、交互问题或有产品建议，可通过“社区反馈”功能直接提交给管理员。', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE
    author_id = VALUES(author_id),
    title = VALUES(title),
    content = VALUES(content),
    pinned = VALUES(pinned),
    updated_at = NOW();

ALTER TABLE announcements AUTO_INCREMENT = 2;
