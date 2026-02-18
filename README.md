# Геймификация образовательного процесса в LMS

[![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.4-brightgreen?style=for-the-badge&logo=spring)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-336791?style=for-the-badge&logo=postgresql)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/license-MIT-blue?style=for-the-badge)](LICENSE)

**НИР:** «Исследование архитектурных подходов и проектирование серверного модуля геймификации для интеграции с российскими LMS платформами»

**Цель исследования:**  
Исследование архитектурных подходов и технологий real-time обновлений, а также разработка концепции серверного 
backend-модуля геймификации для интеграции с российскими LMS-платформами

## Основные результаты НИР

- Реализована серверная часть системы начисления очков и уровней с защитой от дублирования событий
- Обеспечена транзакционная целостность и атомарность операций
- Разработан административный интерфейс для мониторинга данных

## Архитектура (на февраль 2026)

```text
[ LMS / внешние системы ]
↓ (webhook / API)
[ REST API ]  ← Spring Boot Controllers
↓
[ Service Layer ]
├── UserService         (бизнес-логика пользователей)
├── UserAdminService    (административные операции)
├── TransactionService  (транзакции и защита от дублей)
├── EventManagementService  (обработка событий от LMS)
↓
[ Repository Layer ]  ← Spring Data JPA
↓
[ PostgreSQL ]
├── users
└── transactions
```

Основные защитные механизмы:

- Уникальность `event_id` → защита от повторного начисления
- Транзакционность операций (`@Transactional`)
- Валидация входящих данных (`@Valid + Bean Validation`)
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

### Основные эндпоинты (на текущий момент)

| Метод | Путь                                   | Описание                        | Доступ |
|-------|----------------------------------------|---------------------------------|--------|
| POST  | /api/events                            | Обработка события из LMS        | LMS    |
| GET   | /api/admin/users/{userId}              | Информация о пользователе       | ADMIN  |
| GET   | /api/admin/users/{userId}/transactions | История транзакций пользователя | ADMIN  |
| GET   | /api/admin/users                       | Список всех пользователей       | ADMIN  |

### Документация API

Swagger UI доступен по адресу:

```text
http://localhost:8080/swagger-ui.html
```

OpenAPI спецификация:

```text
http://localhost:8080/v3/api-docs
```
### Тестирование

Запуск всех тестов

```
mvn test
```