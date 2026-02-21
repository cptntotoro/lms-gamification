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

- Реализована серверная часть системы начисления очков и уровней с защитой от дублирования событий
- Введена модель настраиваемых типов событий (`EventType`) с возможностью задания количества очков, дневных лимитов и
  состояния активности
- Обеспечена транзакционная целостность и атомарность операций
- Добавлена защита от нарушения внешних ключей между транзакциями и типами событий
- Реализованы административные сервисы для управления типами событий (CRUD)
- Обновлены репозитории и тесты с учётом новых полей и ограничений БД
- Разработан административный интерфейс для мониторинга данных (частично)

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