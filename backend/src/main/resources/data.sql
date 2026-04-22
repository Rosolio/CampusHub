INSERT INTO users (id, student_id, name, email, password, avatar_url, major, score, points, created_at, updated_at)
VALUES
    (1, '20239999', '测试用户一', 'user1@example.com', '$2y$10$pDyLu1we/8IENxGEMGmBvOr4sKtzrDSfwG2TeailJfy2ZP05iFjZu', NULL, 'Computer Science', 10.00, 100, NOW(), NOW()),
    (2, '20239998', '测试用户二', 'user2@example.com', '$2y$10$pDyLu1we/8IENxGEMGmBvOr4sKtzrDSfwG2TeailJfy2ZP05iFjZu', NULL, 'Mathematics', 8.50, 80, NOW(), NOW()),
    (3, '20230001', '测试用户', 'test@example.com', '$2y$10$pDyLu1we/8IENxGEMGmBvOr4sKtzrDSfwG2TeailJfy2ZP05iFjZu', NULL, 'Software Engineering', 0.00, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE
    student_id = VALUES(student_id),
    name = VALUES(name),
    email = VALUES(email),
    password = VALUES(password),
    avatar_url = VALUES(avatar_url),
    major = VALUES(major),
    score = VALUES(score),
    points = VALUES(points),
    updated_at = NOW();

INSERT INTO user_settings (id, user_id, notification_enabled, theme, language, updated_at)
VALUES
    (1, 1, 1, 'light', 'zh-CN', NOW()),
    (2, 2, 1, 'light', 'zh-CN', NOW()),
    (3, 3, 1, 'light', 'zh-CN', NOW())
ON DUPLICATE KEY UPDATE
    user_id = VALUES(user_id),
    notification_enabled = VALUES(notification_enabled),
    theme = VALUES(theme),
    language = VALUES(language),
    updated_at = NOW();

ALTER TABLE users AUTO_INCREMENT = 4;
ALTER TABLE user_settings AUTO_INCREMENT = 4;
