-- Удаление существующих таблиц (для чистой установки)
DROP TABLE IF EXISTS user_course_enrollments CASCADE;
DROP TABLE IF EXISTS groups CASCADE;
DROP TABLE IF EXISTS courses CASCADE;
DROP TABLE IF EXISTS transactions CASCADE;
DROP TABLE IF EXISTS event_types CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Таблица пользователей
CREATE TABLE users
(
    uuid         UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    user_id      VARCHAR(100) NOT NULL UNIQUE,
    total_points INTEGER      NOT NULL DEFAULT 0,
    level        INTEGER      NOT NULL DEFAULT 1,
    created_at   TIMESTAMP(6) NOT NULL,
    updated_at   TIMESTAMP(6)
);

COMMENT ON TABLE users IS 'Пользователи системы (создаются автоматически)';
COMMENT ON COLUMN users.user_id IS 'Идентификатор пользователя из LMS';
COMMENT ON COLUMN users.total_points IS 'Общее количество очков';
COMMENT ON COLUMN users.level IS 'Текущий уровень';

CREATE INDEX idx_users_external_id ON users (user_id);
CREATE INDEX idx_users_points ON users (total_points DESC);
CREATE INDEX idx_users_level ON users (level DESC);

-- Таблица типов событий
CREATE TABLE event_types
(
    uuid             UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    type_code        VARCHAR(50)  NOT NULL UNIQUE,
    display_name     VARCHAR(100) NOT NULL,
    points           INTEGER      NOT NULL CHECK (points > 0),
    max_daily_points INTEGER CHECK (max_daily_points >= 0),
    active           BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMP(6) NOT NULL,
    updated_at       TIMESTAMP(6)
);

COMMENT ON TABLE event_types IS 'Типы событий из LMS (настраиваемые шаблоны начисления очков)';
COMMENT ON COLUMN event_types.type_code IS 'Уникальный код типа (используется LMS в поле eventType)';
COMMENT ON COLUMN event_types.display_name IS 'Человеко-читаемое название для отображения';
COMMENT ON COLUMN event_types.points IS 'Базовое количество очков за событие этого типа';
COMMENT ON COLUMN event_types.max_daily_points IS 'Максимум очков в день по типу (NULL = без лимита)';
COMMENT ON COLUMN event_types.active IS 'Активен ли тип (можно отключать без удаления)';

CREATE INDEX idx_event_types_type_code ON event_types (type_code);
CREATE INDEX idx_event_types_active ON event_types (active);

-- Таблица транзакций
CREATE TABLE transactions
(
    uuid            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         VARCHAR(100) NOT NULL,
    event_id        VARCHAR(255) NOT NULL UNIQUE,
    event_type_code VARCHAR(50)  NOT NULL REFERENCES event_types (type_code) ON DELETE RESTRICT ON UPDATE CASCADE,
    points          INTEGER      NOT NULL CHECK (points > 0),
    description     VARCHAR(500),
    created_at      TIMESTAMP(6) NOT NULL
);

COMMENT ON TABLE transactions IS 'Транзакции';
COMMENT ON COLUMN transactions.user_id IS 'Идентификатор пользователя из LMS';
COMMENT ON COLUMN transactions.event_id IS 'Идентификатор события из LMS';
COMMENT ON COLUMN transactions.event_type_code IS 'Код типа события (ссылка на event_types.type_code)';
COMMENT ON COLUMN transactions.points IS 'Количество начисленных очков';
COMMENT ON COLUMN transactions.description IS 'Описание события';

CREATE INDEX idx_transactions_user_id ON transactions (user_id);
CREATE INDEX idx_transactions_event_id ON transactions (event_id);
CREATE INDEX idx_transactions_event_type_code ON transactions (event_type_code);
CREATE INDEX idx_transactions_created_at ON transactions (created_at DESC);
CREATE INDEX idx_transactions_user_created ON transactions (user_id, created_at DESC);

-- Таблица курсов
CREATE TABLE courses
(
    uuid         UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    course_id    VARCHAR(100) NOT NULL UNIQUE,
    display_name VARCHAR(255) NOT NULL,
    short_name   VARCHAR(50),
    description  TEXT,
    active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMP(6) NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP(6)
);

COMMENT ON TABLE courses IS 'Курсы / дисциплины (например: "Математический анализ", "История России")';
COMMENT ON COLUMN courses.course_id IS 'Внешний идентификатор курса из LMS (строка любого формата)';
COMMENT ON COLUMN courses.display_name IS 'Полное название курса';
COMMENT ON COLUMN courses.short_name IS 'Короткое обозначение (опционально)';

CREATE INDEX idx_courses_course_id ON courses (course_id);
CREATE INDEX idx_courses_active ON courses (active);

-- Таблица групп / потоков внутри курса
CREATE TABLE groups
(
    uuid         UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    group_id     VARCHAR(100) NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    course_id    UUID         NOT NULL REFERENCES courses (uuid) ON DELETE CASCADE,
    active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMP(6) NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP(6),
    CONSTRAINT unique_group_in_course UNIQUE (group_id, course_id)
);

COMMENT ON TABLE groups IS 'Группы / потоки / классы внутри курса';
COMMENT ON COLUMN groups.group_id IS 'Внешний идентификатор группы из LMS';
COMMENT ON COLUMN groups.course_id IS 'Ссылка на курс';

CREATE INDEX idx_groups_group_id_course ON groups (group_id, course_id);
CREATE INDEX idx_groups_active ON groups (active);

-- Связующая таблица: пользователь - курс (с очками по курсу)
CREATE TABLE user_course_enrollments
(
    uuid                   UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    user_id                UUID         NOT NULL REFERENCES users (uuid) ON DELETE CASCADE,
    course_id              UUID         NOT NULL REFERENCES courses (uuid) ON DELETE CASCADE,
    group_id               UUID         REFERENCES groups (uuid) ON DELETE SET NULL,
    total_points_in_course INTEGER      NOT NULL DEFAULT 0,
    enrolled_at            TIMESTAMP(6) NOT NULL DEFAULT NOW(),
    completed_at           TIMESTAMP(6),
    CONSTRAINT unique_user_course UNIQUE (user_id, course_id)
);

COMMENT ON TABLE user_course_enrollments IS 'Зачисление студентов на курсы + статистика по курсу';
COMMENT ON COLUMN user_course_enrollments.user_id IS 'Ссылка на студента';
COMMENT ON COLUMN user_course_enrollments.course_id IS 'Ссылка на курс';
COMMENT ON COLUMN user_course_enrollments.group_id IS 'Группа/поток (может быть NULL)';
COMMENT ON COLUMN user_course_enrollments.total_points_in_course IS 'Сумма очков, заработанных именно на этом курсе';

CREATE INDEX idx_enrollments_user_course ON user_course_enrollments (user_id, course_id);
CREATE INDEX idx_enrollments_course_id ON user_course_enrollments (course_id);