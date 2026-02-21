-- Очистка (на случай перезапуска с sql.init.mode=always)
DELETE FROM transactions;
DELETE FROM event_types;
DELETE FROM users;

-- 1. Типы событий (EventType) — разные сценарии начисления
INSERT INTO event_types (uuid, type_code, display_name, points, max_daily_points, active, created_at, updated_at)
VALUES (gen_random_uuid(), 'quiz', 'Квиз / Тест', 80, 400, true, NOW(), NOW()),
       (gen_random_uuid(), 'lab', 'Лабораторная работа', 150, 600, true, NOW(), NOW()),
       (gen_random_uuid(), 'homework', 'Домашняя работа', 60, 300, true, NOW(), NOW()),
       (gen_random_uuid(), 'attendance', 'Посещение занятия', 20, 100, true, NOW(), NOW()),
       (gen_random_uuid(), 'project', 'Проект / Курсовая', 500, 1000, true, NOW(), NOW());

-- 2. Пользователи (users) — несколько тестовых аккаунтов
INSERT INTO users (uuid, user_id, total_points, level, created_at, updated_at)
VALUES (gen_random_uuid(), 'alex123', 1250, 12, '2026-01-10 09:30:00', '2026-02-19 14:45:00'),
       (gen_random_uuid(), 'maria_edu', 3200, 18, '2026-01-05 11:15:00', '2026-02-18 16:20:00'),
       (gen_random_uuid(), 'ivan_student', 480, 6, '2026-02-01 08:00:00', '2026-02-19 10:10:00'),
       (gen_random_uuid(), 'test_user', 0, 1, NOW(), NOW());

-- 3. Транзакции (transactions) — история начислений для alex123 и других
INSERT INTO transactions (uuid, user_id, event_id, event_type_code, points, description, created_at)
VALUES
    -- alex123 — 1250 XP (накоплено разными действиями)
    (gen_random_uuid(), 'alex123', 'evt-quiz-001', 'quiz', 80, 'Пройден квиз по теме "История"', '2026-02-19 09:15:00'),
    (gen_random_uuid(), 'alex123', 'evt-lab-002', 'lab', 150, 'Сдана лабораторная работа №5', '2026-02-18 14:30:00'),
    (gen_random_uuid(), 'alex123', 'evt-home-003', 'homework', 60, 'Домашнее задание №8', '2026-02-17 10:45:00'),
    (gen_random_uuid(), 'alex123', 'evt-quiz-004', 'quiz', 120, 'Квиз повышенной сложности', '2026-02-16 11:20:00'),
    (gen_random_uuid(), 'alex123', 'evt-project-005', 'project', 500, 'Курсовая работа по дисциплине',
     '2026-02-10 15:00:00'),
    (gen_random_uuid(), 'alex123', 'evt-att-006', 'attendance', 20, 'Посещение лекции 15.02', '2026-02-15 08:00:00'),
    (gen_random_uuid(), 'alex123', 'evt-att-007', 'attendance', 20, 'Посещение семинара 18.02', '2026-02-18 08:00:00'),
    (gen_random_uuid(), 'alex123', 'evt-quiz-008', 'quiz', 80, 'Ежедневный квиз', '2026-02-19 08:30:00'),
    (gen_random_uuid(), 'alex123', 'evt-home-009', 'homework', 60, 'Задание №9', '2026-02-19 12:00:00'),
    (gen_random_uuid(), 'alex123', 'evt-quiz-010', 'quiz', 160, 'Супер-квиз (×2 очков)', '2026-02-19 14:00:00'),

    -- maria_edu — высокий уровень для теста админки
    (gen_random_uuid(), 'maria_edu', 'evt-project-101', 'project', 1000, 'Дипломная работа', '2026-02-01 10:00:00'),
    (gen_random_uuid(), 'maria_edu', 'evt-lab-102', 'lab', 600, 'Серия лабораторных', '2026-02-05 13:30:00'),

    -- ivan_student — низкий уровень
    (gen_random_uuid(), 'ivan_student', 'evt-quiz-201', 'quiz', 80, 'Первый квиз', '2026-02-10 09:00:00'),
    (gen_random_uuid(), 'ivan_student', 'evt-att-202', 'attendance', 20, 'Первая лекция', '2026-02-11 08:00:00');