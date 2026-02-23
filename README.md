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

### 2. Модель и управление типами событий

- Создана сущность `EventType` с полями: `typeCode` (уникальный), `displayName`, `points`, `maxDailyPoints`, `active`
- Реализован поиск активных типов событий (`EventTypeService.getActiveByCode`)
- Добавлен административный CRUD для типов событий:
  - GET `/api/admin/event-types` — список с пагинацией
  - POST/PUT/GET/DELETE `/api/admin/event-types/{id}`
  - `typeCode` неизменяемый после создания
  - DELETE — деактивация (`active = false`), а не физическое удаление

### 3. Защита целостности данных и обработка ошибок

- Добавлен внешний ключ `transactions.event_type_code → event_types.type_code`
- При нарушении FK или уникальности → `DataIntegrityViolationException` перехватывается и преобразуется в
  бизнес-исключение
- Глобальный обработчик ошибок (`@RestControllerAdvice`):
  - `EventTypeNotFoundException` → конкретное сообщение об отключённом типе
  - `DuplicateEventException` → статус `duplicate`
  - `DataIntegrityViolationException` → трактуется как дубликат
  - Все остальные → 500 + "Внутренняя ошибка сервера геймификации"

### 4. Тестирование и качество кода

- Написаны юнит-тесты с покрытием >90% для:
  - `PointsServiceImpl` (лимиты, дубли, обновление уровня)
  - `TransactionServiceImpl` (дубли по eventId и БД)
  - `EventManagementServiceImpl` (оркестрация + обработка ошибок)
  - `LevelCalculatorServiceImpl` (все формулы уровней)
- Добавлены тесты на краевые случаи: отрицательные очки, большие значения, неизвестные формулы
- Настроено отображение покрытия в Coveralls

### 5. Административный функционал и мониторинг

- Реализованы административные эндпоинты под `/api/admin`
- Добавлена возможность деактивации типов событий без удаления истории начислений
- Логирование ключевых операций (начисление, дубли, ошибки) с eventId и userId

### Итоговая оценка прогресса

- Серверная часть готова к интеграции с LMS (webhook `/api/events`)
- Админ-функции реализованы на 80–90% (CRUD типов событий, просмотр пользователей и транзакций)
- Система устойчива к типичным ошибкам (дубли, отключённые типы, нарушения целостности)
- Покрытие тестами высокое, архитектура масштабируема

Следующие шаги (март–апрель 2026):

- Полноценная авторизация (роли ADMIN/USER)
- Интеграция с фронтендом виджета и админ-панели

## Архитектура (на февраль 2026)

```text
[ LMS / внешние системы ]
↓ (webhook / API, LmsEventRequestDto)
[ REST API ]  ← Spring Boot Controllers
↓
[ Service Layer ]
├── UserService               (создание/обновление пользователей)
├── TransactionService        (транзакции + защита от дублей по eventId)
├── EventManagementService    (оркестрация обработки событий от LMS)
├── EventTypeService          (поиск активных типов, проверка лимитов)
├── EventTypeAdminService     (админ CRUD для типов событий)
↓
[ Repository Layer ]  ← Spring Data JPA
↓
[ PostgreSQL ]
├── users
├── transactions              (event_type_code → FK → event_types.type_code)
└── event_types               (настраиваемые типы событий)
```

Основные защитные механизмы:

- Уникальность event_id в transactions → защита от повторного начисления
- Внешний ключ event_type_code → event_types.type_code → целостность данных
- Транзакционность всех операций (@Transactional)
- Валидация входящих данных (@Valid + Bean Validation)
- Проверка дневных лимитов по типу события (EventType.maxDailyPoints)
- Разделение пользовательских и административных сервисов
- Глобальная обработка ошибок через @RestControllerAdvice (конкретные сообщения для бизнес-ошибок, общее — для
  технических)

## Запуск проекта

### Требования

- JDK 21+
- PostgreSQL 17+
- Maven 3.9+

### Локальный запуск

1. Создайте базу данных:

```sql
CREATE DATABASE gamification;
```

2. Настройте application.yml или используйте переменные окружения

```yaml
leveling:
  formula: TRIANGULAR
  base: 500
  increment: 200
```

3. Запуск:

```shell
mvn spring-boot:run
```

Или через IDE: запустить класс GamificationApplication

### Основные эндпоинты API (февраль 2026)

API спроектировано по принципам REST:

- Все пути в нижнем регистре с дефисами
- Ресурсы в множественном числе
- Админ-функции под `/api/admin`
- Виджет-функции под `/api/users` (для фронтенда)
- События от LMS под `/api/events`

| Метод   | Путь                                   | Описание                                      | Доступ     | Примечание                     |
|---------|----------------------------------------|-----------------------------------------------|------------|--------------------------------|
| POST    | `/api/events`                          | Обработка события из LMS (начисление очков)   | LMS        | Основной webhook-эндпоинт     |
| GET     | `/api/users/{userId}/progress`         | Прогресс пользователя для виджета             | Публичный  | Используется фронтендом       |
| GET     | `/api/admin/users`                     | Список всех пользователей                     | ADMIN      | Пагинация и фильтры позже     |
| GET     | `/api/admin/users/{userId}`            | Полная информация о пользователе              | ADMIN      | Для админ-панели              |
| GET     | `/api/admin/users/{userId}/transactions` | История транзакций пользователя             | ADMIN      | Пагинация, сортировка         |
| GET     | `/api/admin/event-types`               | Список всех типов событий                     | ADMIN      | Пагинация                     |
| POST    | `/api/admin/event-types`               | Создание нового типа события                  | ADMIN      | typeCode должен быть уникальным |
| GET     | `/api/admin/event-types/{id}`          | Получение типа события по UUID                | ADMIN      | —                             |
| PUT     | `/api/admin/event-types/{id}`          | Обновление типа события (частичное)           | ADMIN      | typeCode изменить нельзя      |
| DELETE  | `/api/admin/event-types/{id}`          | Деактивация типа события (active = false)     | ADMIN      | Без физического удаления      |

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