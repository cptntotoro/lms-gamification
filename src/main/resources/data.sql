-- 1. Типы событий
INSERT INTO event_types (uuid, type_code, display_name, points, max_daily_points, active, created_at, updated_at)
VALUES (gen_random_uuid(), 'quiz', 'Квиз / Тест', 50, 150, true, NOW(), NOW()),
       (gen_random_uuid(), 'homework', 'Домашняя работа', 100, 300, true, NOW(), NOW()),
       (gen_random_uuid(), 'attendance', 'Посещение занятия', 20, 100, true, NOW(), NOW()),
       (gen_random_uuid(), 'project', 'Проект / Курсовая', 300, NULL, true, NOW(), NOW()),
       (gen_random_uuid(), 'bonus', 'Бонус / Дополнительно', 80, 200, true, NOW(), NOW());

-- 2. Курсы
INSERT INTO courses (uuid, course_id, display_name, short_name, description, active, created_at, updated_at)
VALUES (gen_random_uuid(), 'MATH-101', 'Математический анализ', 'Матан', 'Классический курс по математическому анализу',
        true, NOW(), NOW()),
       (gen_random_uuid(), 'PROG-202', 'Программирование на Java', 'Java', 'Объектно-ориентированное программирование',
        true, NOW(), NOW()),
       (gen_random_uuid(), 'HIST-303', 'История России XX века', 'История', 'Ключевые события и личности', true, NOW(),
        NOW());

-- 3. Группы
INSERT INTO groups (uuid, group_id, display_name, course_id, active, created_at, updated_at)
VALUES (gen_random_uuid(), 'M-21-1', 'М-21-1 (утро)', (SELECT uuid FROM courses WHERE course_id = 'MATH-101'), true,
        NOW(), NOW()),
       (gen_random_uuid(), 'M-21-2', 'М-21-2 (вечер)', (SELECT uuid FROM courses WHERE course_id = 'MATH-101'), true,
        NOW(), NOW()),
       (gen_random_uuid(), 'P-22-1', 'П-22-1 (основная)', (SELECT uuid FROM courses WHERE course_id = 'PROG-202'), true,
        NOW(), NOW()),
       (gen_random_uuid(), 'P-22-2', 'П-22-2 (интенсив)', (SELECT uuid FROM courses WHERE course_id = 'PROG-202'), true,
        NOW(), NOW()),
       (gen_random_uuid(), 'H-23-1', 'И-23-1', (SELECT uuid FROM courses WHERE course_id = 'HIST-303'), true, NOW(),
        NOW());

-- 4. Пользователи — 12 студентов
INSERT INTO users (uuid, user_id, total_points, level, created_at, updated_at)
VALUES (gen_random_uuid(), 'student001', 850, 5, NOW(), NOW()),
       (gen_random_uuid(), 'student002', 720, 4, NOW(), NOW()),
       (gen_random_uuid(), 'student003', 610, 4, NOW(), NOW()),
       (gen_random_uuid(), 'student004', 980, 6, NOW(), NOW()),
       (gen_random_uuid(), 'student005', 450, 3, NOW(), NOW()),
       (gen_random_uuid(), 'student006', 320, 3, NOW(), NOW()),
       (gen_random_uuid(), 'student007', 1150, 7, NOW(), NOW()),
       (gen_random_uuid(), 'student008', 890, 5, NOW(), NOW()),
       (gen_random_uuid(), 'student009', 540, 4, NOW(), NOW()),
       (gen_random_uuid(), 'student010', 670, 4, NOW(), NOW()),
       (gen_random_uuid(), 'student011', 280, 2, NOW(), NOW()),
       (gen_random_uuid(), 'student012', 410, 3, NOW(), NOW());

-- 5. Зачисления
INSERT INTO user_course_enrollments (uuid, user_uuid, course_uuid, group_uuid, total_points_in_course, enrolled_at)
SELECT gen_random_uuid(),
       u.uuid,
       c.uuid,
       g.uuid,
       floor(random() * 800 + 100)::int,
       NOW() - interval '1 month' * floor(random() * 6 + 1)
FROM users u
         CROSS JOIN courses c
         JOIN groups g ON g.course_id = c.uuid
WHERE (u.user_id ~ '^student00[1-4]' AND c.course_id = 'MATH-101' AND g.group_id = 'M-21-1')
   OR (u.user_id ~ '^student00[5-8]' AND c.course_id = 'MATH-101' AND g.group_id = 'M-21-2')
   OR (u.user_id ~ '^student009' AND c.course_id = 'PROG-202' AND g.group_id = 'P-22-1')
   OR (u.user_id ~ '^student01[0-2]' AND c.course_id = 'PROG-202' AND g.group_id = 'P-22-2')
   OR (u.user_id ~ '^student(011|012)' AND c.course_id = 'HIST-303' AND g.group_id = 'H-23-1');

-- 6. Транзакции — points всегда > 0
INSERT INTO transactions (uuid, user_uuid, course_uuid, event_id, event_type_uuid, points, description, created_at)
SELECT gen_random_uuid(),
       u.uuid,
       c.uuid,
       'demo-event-' || md5(random()::text || clock_timestamp()::text),
       et.uuid,
       GREATEST(10, et.points + floor(random() * 100 - 30)::int)::int,
       et.display_name || ' — ' ||
       CASE
           WHEN random() < 0.3 THEN 'отлично'
           WHEN random() < 0.6 THEN 'хорошо'
           ELSE 'удовлетворительно'
           END,
       NOW() - interval '1 day' * floor(random() * 90 + 1)
FROM users u
         JOIN user_course_enrollments uce ON uce.user_uuid = u.uuid
         JOIN courses c ON c.uuid = uce.course_uuid
         JOIN event_types et ON true
WHERE random() < 0.65
LIMIT 100;