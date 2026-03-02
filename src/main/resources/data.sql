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

--- 5. Зачисления — фиксированные значения очков по курсу (для предсказуемости демо)
INSERT INTO user_course_enrollments (uuid, user_uuid, course_uuid, group_uuid, total_points_in_course, enrolled_at)
SELECT gen_random_uuid(),
       u.uuid,
       c.uuid,
       g.uuid,
       CASE
           -- MATH-101, группа M-21-1
           WHEN u.user_id = 'student001' THEN 1240
           WHEN u.user_id = 'student002' THEN 730
           WHEN u.user_id = 'student003' THEN 980
           WHEN u.user_id = 'student004' THEN 510

           -- MATH-101, группа M-21-2
           WHEN u.user_id = 'student005' THEN 1450
           WHEN u.user_id = 'student006' THEN 890
           WHEN u.user_id = 'student007' THEN 1620 -- лидер
           WHEN u.user_id = 'student008' THEN 420

           -- PROG-202
           WHEN u.user_id = 'student009' THEN 670
           WHEN u.user_id = 'student010' THEN 310
           WHEN u.user_id = 'student011' THEN 940
           WHEN u.user_id = 'student012' THEN 1180 -- лидер в PROG-202

           ELSE 0
           END AS total_points_in_course,
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


-- Дополнительные зачисления на HIST-303 (несколько человек)
INSERT INTO user_course_enrollments (uuid, user_uuid, course_uuid, group_uuid, total_points_in_course, enrolled_at)
SELECT gen_random_uuid(),
       u.uuid,
       c.uuid,
       g.uuid,
       CASE u.user_id
           WHEN 'student011' THEN 380
           WHEN 'student012' THEN 640
           ELSE 120
           END,
       NOW() - interval '45 days'
FROM users u
         JOIN courses c ON c.course_id = 'HIST-303'
         JOIN groups g ON g.course_id = c.uuid AND g.group_id = 'H-23-1'
WHERE u.user_id IN ('student011', 'student012', 'student007', 'student004')
ON CONFLICT ON CONSTRAINT unique_user_course DO NOTHING;


-- 6. Транзакции

-- Очищаем, если запускаем повторно
DELETE
FROM transactions;

-- Вставляем по 8–20 транзакций на каждое зачисление с очками > 0
INSERT INTO transactions (uuid,
                          user_uuid,
                          course_uuid,
                          event_id,
                          event_type_uuid,
                          points,
                          description,
                          created_at)
SELECT gen_random_uuid(),
       e.user_uuid,
       e.course_uuid,
       'demo-ev-' || md5(random()::text || clock_timestamp()::text),
       et.uuid,
       -- очки: стараемся держать в разумных пределах типа события
       CASE
           WHEN et.type_code = 'project' THEN floor(random() * 200 + 180)::int -- крупные
           WHEN et.type_code = 'homework' THEN floor(random() * 80 + 60)::int
           WHEN et.type_code = 'quiz' THEN floor(random() * 50 + 30)::int
           WHEN et.type_code = 'bonus' THEN floor(random() * 120 + 40)::int
           ELSE floor(random() * 30 + 10)::int -- attendance и прочее
           END AS points,
       et.display_name || ' — ' ||
       CASE (random() * 4)::int
           WHEN 0 THEN 'отлично!'
           WHEN 1 THEN 'хорошо'
           WHEN 2 THEN 'на твёрдую четвёрку'
           ELSE 'удовлетворительно'
           END AS description,
       -- даты: от 3 месяцев назад до вчера
       NOW() - interval '1 day' * (floor(random() * 90 + 1)::int)
FROM user_course_enrollments e
         CROSS JOIN event_types et
-- генерируем разное количество транзакций на пару (студент × курс)
         CROSS JOIN generate_series(1,
                                    CASE
                                        WHEN e.total_points_in_course > 1400 THEN 16 + (random() * 8)::int
                                        WHEN e.total_points_in_course > 900 THEN 12 + (random() * 6)::int
                                        WHEN e.total_points_in_course > 500 THEN 8 + (random() * 5)::int
                                        ELSE 4 + (random() * 4)::int
                                        END
                    ) gs(n)
WHERE e.total_points_in_course > 0
  AND random() < 0.88 -- небольшое разреживание, чтобы не все типы всегда были
ORDER BY e.user_uuid, e.course_uuid, random()
LIMIT 280; -- верхняя граница на всякий случай