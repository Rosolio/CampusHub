-- Performance migration: add composite indexes for task feed queries
-- Run this migration against your existing database

USE campushub;

-- Composite index for the primary feed query pattern:
--   WHERE review_status = 'approved' AND task_mode = ? ORDER BY created_at DESC
-- Covers both task and topic feed queries
ALTER TABLE tasks ADD INDEX idx_tasks_feed_query (review_status, task_mode, status, created_at);

-- Index for category-filtered feed queries
ALTER TABLE tasks ADD INDEX idx_tasks_feed_category (review_status, task_mode, category, created_at);

-- Index for expiration checks in feed queries
ALTER TABLE tasks ADD INDEX idx_tasks_expires (expires_at);

-- Covering index for task_comments correlated subquery
--   SELECT COUNT(*) FROM task_comments WHERE task_id = ?
-- This makes the per-row comment count lookup extremely fast (index-only scan)
ALTER TABLE task_comments ADD INDEX idx_task_comments_taskid_covering (task_id);

-- Index for admin tasks feed query (sort by review_status priority + created_at)
ALTER TABLE tasks ADD INDEX idx_tasks_admin_feed (review_status, created_at);

-- Index for task_likes lookup by user + task IDs (used in attachLikeStatus)
-- If task_likes table exists
-- ALTER TABLE task_likes ADD INDEX idx_task_likes_user_task (user_id, task_id);
