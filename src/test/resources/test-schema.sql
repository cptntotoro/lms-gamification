-- Удаление существующих таблиц (для чистой установки)
DROP TABLE IF EXISTS transactions CASCADE;
DROP TABLE IF EXISTS event_types CASCADE;
DROP TABLE IF EXISTS users CASCADE;

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