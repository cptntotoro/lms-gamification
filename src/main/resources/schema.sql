-- Удаление существующих таблиц (для чистой установки)
DROP TABLE IF EXISTS transactions CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Таблица пользователей
CREATE TABLE users
(
    id           BIGSERIAL PRIMARY KEY,
    user_id      VARCHAR(100) NOT NULL UNIQUE,
    total_points INTEGER      NOT NULL DEFAULT 0,
    level        INTEGER      NOT NULL DEFAULT 1,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP
);

COMMENT ON TABLE users IS 'Пользователи системы (создаются автоматически)';
COMMENT ON COLUMN users.user_id IS 'ID пользователя из LMS';
COMMENT ON COLUMN users.total_points IS 'Всего накопленных очков';
COMMENT ON COLUMN users.level IS 'Текущий уровень (points/100 + 1)';

CREATE INDEX idx_users_external_id ON users (user_id);
CREATE INDEX idx_users_points ON users (total_points DESC);
CREATE INDEX idx_users_level ON users (level DESC);

-- Таблица транзакций (история начислений)
CREATE TABLE transactions
(
    id          BIGSERIAL PRIMARY KEY,
    user_id     VARCHAR(100) NOT NULL,
    event_id    VARCHAR(255) NOT NULL UNIQUE,
    points      INTEGER      NOT NULL CHECK (points > 0),
    description VARCHAR(500),
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE transactions IS 'История начислений очков';
COMMENT ON COLUMN transactions.user_id IS 'ID пользователя из LMS';
COMMENT ON COLUMN transactions.event_id IS 'Уникальный ID события из LMS (защита от дублей)';
COMMENT ON COLUMN transactions.points IS 'Сколько очков начислено';
COMMENT ON COLUMN transactions.description IS 'Описание события';

CREATE INDEX idx_transactions_user_id ON transactions (user_id);
CREATE INDEX idx_transactions_event_id ON transactions (event_id);
CREATE INDEX idx_transactions_created_at ON transactions (created_at DESC);
CREATE INDEX idx_transactions_user_created ON transactions (user_id, created_at DESC);