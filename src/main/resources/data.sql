-- Seed some sample tasks
INSERT INTO tasks(title, priority, due_date, target_minutes, status)
VALUES
('Write project proposal', 2, NULL, 60, 'PENDING'),
('Implement login screen', 3, NULL, 90, 'PENDING')
ON DUPLICATE KEY UPDATE title=VALUES(title);