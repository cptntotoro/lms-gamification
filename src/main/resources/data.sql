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

-- 4. Пользователи — 12 студентов (total_points и level будут пересчитаны позже)
INSERT INTO users (uuid, user_id, total_points, level, created_at, updated_at)
VALUES (gen_random_uuid(), 'student001', 0, 1, NOW(), NOW()),
       (gen_random_uuid(), 'student002', 0, 1, NOW(), NOW()),
       (gen_random_uuid(), 'student003', 0, 1, NOW(), NOW()),
       (gen_random_uuid(), 'student004', 0, 1, NOW(), NOW()),
       (gen_random_uuid(), 'student005', 0, 1, NOW(), NOW()),
       (gen_random_uuid(), 'student006', 0, 1, NOW(), NOW()),
       (gen_random_uuid(), 'student007', 0, 1, NOW(), NOW()),
       (gen_random_uuid(), 'student008', 0, 1, NOW(), NOW()),
       (gen_random_uuid(), 'student009', 0, 1, NOW(), NOW()),
       (gen_random_uuid(), 'student010', 0, 1, NOW(), NOW()),
       (gen_random_uuid(), 'student011', 0, 1, NOW(), NOW()),
       (gen_random_uuid(), 'student012', 0, 1, NOW(), NOW());

-- 5. Зачисления (создаём для всех студентов)
INSERT INTO user_course_enrollments (uuid, user_uuid, course_uuid, group_uuid, total_points_in_course, enrolled_at)
SELECT gen_random_uuid(),
       u.uuid,
       c.uuid,
       g.uuid,
       0,
       NOW() - interval '1 month' *
    CASE
    WHEN u.user_id ~ '00[1-4]' THEN 2
    WHEN u.user_id ~ '00[5-8]' THEN 3
    ELSE 1
END
FROM users u
         JOIN courses c ON c.course_id IN ('MATH-101', 'PROG-202')
         LEFT JOIN groups g ON g.course_id = c.uuid
    AND g.group_id = CASE
                         WHEN u.user_id ~ '00[1-4]' THEN 'M-21-1'
                         WHEN u.user_id ~ '00[5-8]' THEN 'M-21-2'
                         WHEN u.user_id = 'student009' THEN 'P-22-1'
                         WHEN u.user_id ~ '01[0-2]' THEN 'P-22-2'
END
WHERE (u.user_id ~ '^student00[1-8]' AND c.course_id = 'MATH-101')
   OR (u.user_id ~ '^student009|^student01[0-2]' AND c.course_id = 'PROG-202')
ON CONFLICT ON CONSTRAINT unique_user_course DO NOTHING;

-- Дополнительные зачисления на HIST-303
INSERT INTO user_course_enrollments (uuid, user_uuid, course_uuid, group_uuid, total_points_in_course, enrolled_at)
SELECT gen_random_uuid(),
       u.uuid,
       c.uuid,
       g.uuid,
       0,
       NOW() - interval '45 days'
FROM users u
    JOIN courses c ON c.course_id = 'HIST-303'
    JOIN groups g ON g.course_id = c.uuid AND g.group_id = 'H-23-1'
WHERE u.user_id IN ('student011', 'student012', 'student007', 'student004')
ON CONFLICT ON CONSTRAINT unique_user_course DO NOTHING;

-- 6. Генерация транзакций (без использования DO, только обычные SQL-запросы)
DELETE FROM transactions;

-- Для каждого пользователя (кроме трёх нулевых) создаём от 8 до 15 транзакций
-- с очками, соответствующими типам событий из таблицы event_types
INSERT INTO transactions (uuid, user_uuid, course_uuid, group_uuid, event_id, event_type_uuid, points, description, created_at)
SELECT
    gen_random_uuid(),
    t.user_uuid,
    t.course_uuid,
    t.group_uuid,
    'demo-ev-' || md5(random()::text || clock_timestamp()::text),
    t.event_type_uuid,
    t.points,
    t.event_display_name || ' — ' ||
    CASE (random() * 4)::int
        WHEN 0 THEN 'отлично!'
        WHEN 1 THEN 'хорошо'
        WHEN 2 THEN 'на твёрдую четвёрку'
        ELSE 'удовлетворительно'
END,
    NOW() - (random() * 90)::int * interval '1 day'
FROM (
    SELECT
        u.uuid AS user_uuid,
        e.course_uuid,
        e.group_uuid,
        et.uuid AS event_type_uuid,
        et.points,
        et.display_name AS event_display_name,
        -- нумеруем строки для каждого пользователя, чтобы ограничить количество транзакций
        ROW_NUMBER() OVER (PARTITION BY u.uuid ORDER BY random()) AS rn
    FROM users u
    -- берём одно случайное зачисление для пользователя (чтобы не плодить на все курсы)
    CROSS JOIN LATERAL (
        SELECT course_uuid, group_uuid
        FROM user_course_enrollments
        WHERE user_uuid = u.uuid
        ORDER BY random()
        LIMIT 1
    ) e
    CROSS JOIN event_types et
    -- генерируем до 20 кандидатов на пользователя (из них отфильтруем нужное количество)
    CROSS JOIN generate_series(1, 20) AS gs
    WHERE u.user_id NOT IN ('student010', 'student011', 'student012')
) t
WHERE t.rn BETWEEN 1 AND (8 + floor(random() * 8)::int)  -- от 8 до 15 транзакций на пользователя
ORDER BY t.user_uuid, random()
-- лимит на случай если что-то пошло не так (но обычно не срабатывает)
LIMIT 200;

-- 7. Корректировка суммы очков в зачислениях: делаем её равной сумме очков транзакций для каждого зачисления
UPDATE user_course_enrollments e
SET total_points_in_course = (
    SELECT COALESCE(SUM(t.points), 0)
    FROM transactions t
    WHERE t.user_uuid = e.user_uuid AND t.course_uuid = e.course_uuid
);

-- 8. Корректировка глобальных очков и уровня пользователей
UPDATE users u
SET total_points = (
    SELECT COALESCE(SUM(e.total_points_in_course), 0)
    FROM user_course_enrollments e
    WHERE e.user_uuid = u.uuid
),
    level = GREATEST(1, FLOOR((SELECT COALESCE(SUM(e.total_points_in_course), 0)
                               FROM user_course_enrollments e
                               WHERE e.user_uuid = u.uuid) / 100) + 1);