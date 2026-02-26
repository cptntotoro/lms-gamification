# Геймификация образовательного процесса в LMS

[![Coverage Status](https://coveralls.io/repos/github/cptntotoro/lms-gamification/badge.svg?branch=main)](https://coveralls.io/github/cptntotoro/lms-gamification?branch=main)

[![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.4-brightgreen?style=for-the-badge&logo=spring)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-336791?style=for-the-badge&logo=postgresql)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/license-MIT-blue?style=for-the-badge)](LICENSE)

**НИР:** «Исследование архитектурных подходов и проектирование серверного модуля геймификации для интеграции с
российскими LMS платформами»

**Цель исследования:**  
Исследование архитектурных подходов и технологий real-time обновлений, а также разработка концепции серверного
backend-модуля геймификации для интеграции с российскими LMS-платформами

## Основные результаты НИР (на 19 февраля 2026)

### 1. Серверная логика начисления очков и уровней

- Реализована полная цепочка обработки событий от LMS: получение → валидация → проверка дублей → начисление → обновление
  пользователя
- Введена защита от повторной обработки одного и того же события (`eventId` уникален в таблице `transactions`)
- Добавлена проверка **дневных лимитов** по типу события (`EventType.maxDailyPoints`)
- Реализована атомарность операций через `@Transactional` на всех ключевых методах

Сервис `LevelCalculatorService` поддерживает три академически обоснованные модели роста сложности, а также
fallback-режим для неизвестных формул.

#### 1. TRIANGULAR (треугольная модель, triangular numbers progression)

**Описание:**  
Очки для достижения уровня n пропорциональны треугольному числу T(n) = n(n+1)/2. Это создаёт плавное ускорение роста:
первые уровни даются легко, последующие требуют всё больше усилий.

**Формула уровня (решение обратной задачи):**

L = ⌊ (-1 + √(1 + 8 × totalPoints / base)) / 2 ⌋ + 1

**Очки до следующего уровня:**

ΔL = base × (L + 1)

**Защита:**  
Если totalPoints ≤ 0 → возвращает уровень 1

**Пример (base = 500):**

- 0 очков → уровень 1
- 500 очков → уровень 2
- 1500 очков → уровень 3
- 3000 очков → уровень 4

#### 2. QUADRATIC (квадратичная модель, quadratic progression)

**Описание:**  
Очки для достижения уровня n пропорциональны квадрату номера уровня (n²). Это создаёт более выраженное замедление
прогресса на высоких уровнях — классический подход для долгосрочной мотивации.

**Формула уровня:**

L = ⌊ √(totalPoints / base) ⌋ + 1

**Очки до следующего уровня:**

ΔL = base × (2 × L + 1)

**Защита:**  
Если totalPoints ≤ 0 → возвращает уровень 1

**Пример (base = 200):**

- 0–199 очков → уровень 1
- 200–799 очков → уровень 2
- 800–1799 очков → уровень 3
- 1800–3199 очков → уровень 4

#### 3. LINEAR (линейная модель с постоянным приростом)

**Описание:**  
Самая простая и предсказуемая модель — очки до следующего уровня растут арифметически. Подходит для начального
тестирования или когда нужна линейная прогрессия.

**Алгоритм:**  
Кумулятивная сумма до уровня L:  
S(L) = base + increment × (0 + 1 + 2 + ... + (L-1))

Итеративно находим минимальный L, при котором S(L) > totalPoints.

**Очки до следующего уровня:**

ΔL = base + increment × L

**Защита:**  
Если totalPoints ≤ 0 → возвращает уровень 1

**Пример (base = 500, increment = 200):**

- 0–499 очков → уровень 1
- 500–1199 очков → уровень 2
- 1200–2099 очков → уровень 3
- 2100–3199 очков → уровень 4

#### 4. Fallback-режим (при неизвестной формуле)

**Формула:**

L = 1 + ⌊ totalPoints / 1000 ⌋

**Очки до следующего уровня:** фиксированные 1000

**Пример:**

- 0–999 очков → уровень 1
- 1000–1999 очков → уровень 2
- 9000–9999 очков → уровень 10

#### Поведение при краевых случаях

- totalPoints ≤ 0 → всегда возвращает уровень 1 (защита от отрицательных значений)
- Очень большие значения (Integer.MAX_VALUE) → корректно обрабатываются без переполнения (используется `long` в
  промежуточных расчётах)
- Нулевой или отрицательный currentLevel в `pointsToNextLevel` → возвращает корректное значение для следующего уровня

#### Научная основа моделей

- TRIANGULAR — опирается на треугольные числа (A000217 OEIS), популярна в RPG (Final Fantasy, Pokémon)
- QUADRATIC — классическая экспоненциальная кривая прогресса (World of Warcraft, RuneScape)
- LINEAR — используется в ранних системах (Habitica, Khan Academy badges)

### 2. Модель и управление типами событий

- Создана сущность `EventType` с полями: `typeCode` (уникальный), `displayName`, `points`, `maxDailyPoints`, `active`
- Реализован поиск активных типов событий (`EventTypeService.getActiveByCode`)
- Добавлен административный CRUD для типов событий:
  - GET `/api/admin/event-types` — список с пагинацией
  - POST/PUT/GET/DELETE `/api/admin/event-types/{id}`
  - `typeCode` неизменяемый после создания
  - DELETE — деактивация (`active = false`), а не физическое удаление

### 3. Защита целостности данных и обработка ошибок

- Добавлен внешний ключ `transactions.event_type_code → event_types.type_code` с ограничением ON DELETE RESTRICT / ON
  UPDATE CASCADE
- При попытке сохранить транзакцию с несуществующим или отключённым `typeCode` → возникает
  `DataIntegrityViolationException`
- Все ключевые операции (`save`, `update`) выполняются в `@Transactional` — откат при любой ошибке
- Глобальный обработчик исключений (`@RestControllerAdvice`) перехватывает и преобразует ошибки в единый формат ответа
  `LmsEventResponsetDto` (всегда HTTP 200 OK для бизнес-ошибок):

| Исключение                            | HTTP-статус               | Ответ в JSON                                                                               | Логирование         | Описание / когда возникает                                                                  |
|---------------------------------------|---------------------------|--------------------------------------------------------------------------------------------|---------------------|---------------------------------------------------------------------------------------------|
| `DuplicateEventException`             | 200 OK                    | `{ "status": "duplicate", "message": "..." }`                                              | INFO                | Дубликат события по `eventId` (проверка в `TransactionService` или уникальность БД)         |
| `DuplicateEventTypeException`         | 200 OK                    | `{ "status": "duplicate", "message": "..." }`                                              | INFO                | Попытка создать тип события с уже существующим `typeCode`                                   |
| `UserNotFoundException`               | 200 OK                    | `{ "status": "error", "message": "..." }`                                                  | WARN                | Пользователь не найден в БД при запросе прогресса или начислении                            |
| `EventTypeNotFoundException`          | 200 OK                    | `{ "status": "error", "message": "..." }`                                                  | WARN                | Тип события не найден или отключён (`active = false`) при обработке события                 |
| `DataIntegrityViolationException`     | 200 OK                    | `{ "status": "duplicate", "message": "Событие уже обработано (обнаружено на уровне БД)" }` | WARN                | Нарушение уникальности или внешнего ключа в БД (дубликат `eventId` или неверный `typeCode`) |
| `MethodArgumentNotValidException`     | 400 Bad Request           | `{ "status": "error", "message": "поле: сообщение" }`                                      | WARN                | Ошибка валидации входящего DTO (`@Valid` в контроллере)                                     |
| Любое другое исключение (`Exception`) | 500 Internal Server Error | `{ "status": "error", "message": "Внутренняя ошибка сервера геймификации" }`               | ERROR + stack trace | Необработанные технические ошибки (NPE, SQLException, timeout и т.д.)                       |

**Ключевые принципы обработки:**

- Бизнес-ошибки (дубликат, лимит, не найден тип, не найден пользователь) → всегда 200 OK + понятный `message` для
  клиента LMS
- Технические ошибки → 500 Internal Server Error + общий текст ошибки (чтобы не раскрывать детали в продакшене)
- Полное логирование: INFO/WARN для бизнес-ошибок, ERROR + stack trace для технических
- Нет «голых» 4xx/5xx без тела ответа — клиент всегда получает структурированный JSON

**Пример ответа при ошибке (200 OK):**

```json
{
  "status": "error",
  "message": "Неизвестный или отключённый тип события: webinar"
}
```

### 4. Тестирование и качество кода

- Написаны юнит-тесты с покрытием >90%
- Добавлены тесты на краевые случаи: отрицательные очки, большие значения, неизвестные формулы
- Настроено отображение покрытия в Coveralls

### 5. Административный функционал и мониторинг

- Реализованы административные эндпоинты под `/api/admin`
- Добавлена возможность деактивации типов событий без удаления истории начислений
- Логирование ключевых операций (начисление, дубли, ошибки) с eventId и userId

### 6. Поддержка курсов и групп студентов (новое, февраль 2026)

- Реализована гибридная модель: курсы и группы **опциональны** (управляется настройкой
  `gamification.features.courses.enabled`)
- Добавлены сущности: `Course`, `Group`, `UserCourseEnrollment`
- Пользователь может быть зачислен на несколько курсов одновременно
- Очки начисляются **дважды**:
  - глобально (`users.total_points`)
  - по конкретному курсу (`user_course_enrollments.total_points_in_course`)
- Реализована защита от дублирования зачислений (`unique_user_course`)
- Кастомные исключения:
  - `CourseNotFoundException`
  - `GroupNotFoundException`
  - `UserNotEnrolledInCourseException`
- Глобальный обработчик исключений возвращает понятные сообщения в `LmsEventResponseDto` (HTTP 200 для бизнес-ошибок)

Это позволяет:

- формировать лидерборды по курсу и группе
- анализировать эффективность геймификации по дисциплинам отдельно
- проводить сравнение групп/потоков внутри одного курса

### Итоговая оценка прогресса

- Серверная часть готова к интеграции с LMS (webhook `/api/events`)
- Админ-функции реализованы на 80–90% (CRUD типов событий, просмотр пользователей и транзакций)
- Система устойчива к типичным ошибкам (дубли, отключённые типы, нарушения целостности)
- Покрытие тестами высокое, архитектура масштабируема

Следующие шаги (март–апрель 2026): - Полноценная авторизация (роли ADMIN/USER)

- Интеграция с фронтендом виджета и админ-панели

## Архитектура (на февраль 2026)

```text
[LMS / внешние системы]
    ↓ Webhook / API (POST /api/events, LmsEventRequestDto)
[REST API]  ← Spring Boot Controllers
    ↓
[Service Layer]  ← основные бизнес-сервисы
    ├── UserService                 (поиск/создание/обновление пользователей по userId)
    ├── UserCourseService           (зачисление на курсы, начисление очков по курсу)
    ├── UserAdminService            (админ-доступ: поиск, список, блокировка пользователей)
    ├── PointsService               (начисление очков)
    ├── PointsService               (бизнес-логика начисления очков за событие из LMS)
    ├── LevelCalculatorService      (расчёт уровня и очков до следующего уровня — TRIANGULAR/QUADRATIC/LINEAR)
    ├── TransactionService          (сохранение транзакций, проверка дублей по eventId, сумма по дате/типу)
    ├── EventManagementService      (оркестрация: вызов PointsService → получение типа события → формирование ответа LMS)
    ├── EventTypeService            (поиск активных типов событий, проверка доступности)
    └── EventTypeAdminService       (админ-CRUD типов событий: создание/редактирование/деактивация)
    ↓
[Repository Layer]  ← Spring Data JPA
    ↓
[PostgreSQL]
    ├── users                       (uuid, user_id, total_points, level, created_at, updated_at)
    ├── courses                     (uuid, course_id, display_name, short_name, description, active)
    ├── groups                      (uuid, group_id, display_name, course_id, active)
    ├── user_course_enrollments     (uuid, user_id, course_id, group_id, total_points_in_course, enrolled_at)
    ├── transactions                (uuid, user_id, event_id, event_type_code, points_earned, created_at)
    └── event_types                 (uuid, type_code, display_name, points, max_daily_points, active)
```

Основные защитные механизмы:

- Гибкие формулы расчёта уровня (TRIANGULAR, QUADRATIC, LINEAR + fallback)
- Уникальность event_id в transactions → защита от повторного начисления
- Внешний ключ event_type_code → event_types.type_code → целостность данных
- Уникальность зачисления: unique_user_course в user_course_enrollments
- Транзакционность всех операций (@Transactional)
- Валидация входящих данных (@Valid + Bean Validation)
- Проверка дневных лимитов по типу события (EventType.maxDailyPoints)
- Разделение пользовательских и административных сервисов
- Глобальная обработка ошибок через @RestControllerAdvice (конкретные сообщения для бизнес-ошибок, общее — для
  технических)

## Запуск проекта

### Требования к окружению

- **JDK**: 21+
- **Maven**: 3.9+
- **PostgreSQL**: 17+ (или Docker)
- **Docker** + **Docker Compose** (рекомендуется для быстрого старта)

### Вариант 1: Запуск с Docker (рекомендуется)

1. Убедитесь, что Docker и Docker Compose установлены и запущены.
2. В корне проекта находится `docker-compose.yml` и `Dockerfile`.
3. Выполните команду:

```bash
docker-compose up --build
```

- Приложение запустится на порту 8080
- PostgreSQL будет доступен на порту 5433 (чтобы не конфликтовать с локальным Postgres)
- База данных gamification создастся автоматически

#### Доступные адреса:

- Приложение: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs
- Health-check: http://localhost:8080/actuator/health

#### Остановка:

```
docker-compose down
```

### Вариант 2: Локальный запуск без Docker

1. Установите PostgreSQL 17+ и создайте базу данных:

```sql
CREATE DATABASE gamification;
```

2. Настройте application.yml или используйте переменные окружения

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/gamification-db
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver

gamification:
  features:
    courses:
      enabled: true                     # включить поддержку курсов и групп
      requireCourseOnFirstEvent: false  # требовать courseId при первом событии
    leveling:
      formula:
        type: TRIANGULAR
        base: 500
        increment: 200
```

3. Запуск без выполнения тестов:

```shell
mvn spring-boot:run -DSkipTests
```

### Основные эндпоинты API (февраль 2026)

API спроектировано по принципам REST:

- Все пути в нижнем регистре с дефисами
- Ресурсы в множественном числе
- Админ-функции под `/api/admin`
- Виджет-функции под `/api/users` (для фронтенда)
- События от LMS под `/api/events`

| Метод  | Путь                                     | Описание                                    | Доступ    | Примечание                      |
|--------|------------------------------------------|---------------------------------------------|-----------|---------------------------------|
| POST   | `/api/events`                            | Обработка события из LMS (начисление очков) | LMS       | Основной webhook-эндпоинт       |
| GET    | `/api/users/{userId}/progress`           | Прогресс пользователя для виджета           | Публичный | Используется фронтендом         |
| GET    | `/api/admin/users`                       | Список всех пользователей                   | ADMIN     | Пагинация и фильтры позже       |
| GET    | `/api/admin/users/{userId}`              | Полная информация о пользователе            | ADMIN     | Для админ-панели                |
| GET    | `/api/admin/users/{userId}/transactions` | История транзакций пользователя             | ADMIN     | Пагинация, сортировка           |
| GET    | `/api/admin/event-types`                 | Список всех типов событий                   | ADMIN     | Пагинация                       |
| POST   | `/api/admin/event-types`                 | Создание нового типа события                | ADMIN     | typeCode должен быть уникальным |
| GET    | `/api/admin/event-types/{id}`            | Получение типа события по UUID              | ADMIN     | —                               |
| PUT    | `/api/admin/event-types/{id}`            | Обновление типа события (частичное)         | ADMIN     | typeCode изменить нельзя        |
| DELETE | `/api/admin/event-types/{id}`            | Деактивация типа события (active = false)   | ADMIN     | Без физического удаления        |

### Рекомендации по использованию

- **LMS** → только POST `/api/events`
- **Виджет / фронтенд** → только GET `/api/users/{userId}/progress`
- **Админ-панель** → всё под `/api/admin`
- Все админ-эндпоинты должны быть защищены (в будущем `@PreAuthorize("hasRole('ADMIN')")` + JWT или OAuth)

### Документация API

Swagger UI доступен по адресу:

[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

OpenAPI спецификация:

[http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

### Cхема БД

![](db_diagram.png)

### Тестирование

Запуск всех тестов

```
mvn test
```