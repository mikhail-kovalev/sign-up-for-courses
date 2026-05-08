# Obrorg Courses

Курсовой проект: клиент-серверное мобильное приложение для записи на онлайн-курсы образовательной организации.

Проект состоит из двух частей:

- `app` - Android-клиент на Kotlin и Jetpack Compose.
- `server` - back-end на Kotlin/Ktor.
- `docker-compose.yml` - локальная PostgreSQL база данных.

## Соответствие требованиям

- Клиент-серверная архитектура: Android-приложение работает с REST API сервера.
- Back-end реализован на Ktor.
- Сервер использует локальную PostgreSQL БД.
- В приложении есть регистрация, вход и JWT-авторизация.
- Бизнес-данные хранятся в PostgreSQL, Firebase/Firestore не используются.
- Android-интерфейс реализован на Jetpack Compose.
- Клиент разделен по Clean Architecture на слои `data`, `domain`, `presentation`.

## Возможности приложения

- Регистрация нового пользователя.
- Вход тестового или зарегистрированного пользователя.
- Просмотр каталога курсов.
- Поиск и фильтрация курсов.
- Просмотр подробной карточки курса.
- Запись авторизованного пользователя на курс.
- Просмотр своих записей в разделе "Мои курсы".
- Профиль пользователя, смена пароля, настройки темы и уведомлений.
- Отзывы к курсам и отправка обращения в поддержку.

## Запуск базы данных

В корне проекта выполнить:

```powershell
docker compose up -d db
```

PostgreSQL будет доступен на `localhost:5432`.

Параметры локальной БД:

- database: `oborg_courses`
- user: `oborg`
- password: `oborg`

## Запуск back-end

```powershell
.\gradlew.bat -p server run
```

API будет доступен на:

```text
http://localhost:8080
```

Проверка сервера:

```http
GET http://localhost:8080/health
```

Тестовый пользователь:

- email: `student@oborg.ru`
- пароль: `student123`

## Запуск Android-клиента

Открыть папку проекта `D:\ObOrg` в Android Studio и запустить модуль `app`.

Для Android Emulator уже указан адрес сервера:

```kotlin
http://10.0.2.2:8080
```

Адрес задается в [app/build.gradle.kts](app/build.gradle.kts) через `API_BASE_URL`.

Если приложение запускается на физическом телефоне, нужно заменить `API_BASE_URL` на IP-адрес компьютера в локальной сети.

## Основные API

- `GET /health` - проверка работы сервера.
- `POST /auth/register` - регистрация.
- `POST /auth/login` - вход.
- `GET /courses` - список курсов.
- `GET /courses/{id}` - карточка курса.
- `GET /courses/{id}/reviews` - отзывы курса.
- `POST /courses/{id}/enroll` - запись на курс, нужен JWT.
- `POST /courses/{id}/reviews` - публикация отзыва, нужен JWT.
- `GET /enrollments` - записи текущего пользователя, нужен JWT.
- `GET /users/me` - текущий пользователь, нужен JWT.
- `POST /users/me/password` - смена пароля, нужен JWT.
- `POST /support-requests` - обращение в поддержку, нужен JWT.

Примеры запросов находятся в [docs/api.http](docs/api.http).

## Структура Android-клиента

```text
app/src/main/java/ru/oborg/courses
├── core
├── data
│   ├── local
│   ├── remote
│   └── repository
├── domain
│   ├── model
│   ├── repository
│   └── usecase
└── presentation
    ├── auth
    ├── catalog
    ├── detail
    ├── home
    ├── mycourses
    ├── navigation
    ├── profile
    └── theme
```

Слой `data` отвечает за сеть, DTO, локальное хранение токена и реализации репозиториев.

Слой `domain` содержит модели, интерфейсы репозиториев и use case-классы.

Слой `presentation` содержит ViewModel, Jetpack Compose-экраны, навигацию и тему.

## Структура сервера

```text
server/src/main/kotlin/ru/oborg/courses/server
├── application
├── data
├── domain
├── presentation
└── security
```

- `application` - сценарии использования.
- `data` - подключение к БД, миграции, seed-данные, JDBC-репозитории.
- `domain` - модели и интерфейсы репозиториев.
- `presentation` - DTO для REST API.
- `security` - JWT и BCrypt-хеширование паролей.

## База данных

При старте сервер создает таблицы, если их еще нет:

- `users`
- `courses`
- `enrollments`
- `course_reviews`
- `support_requests`

Демонстрационные курсы создаются seed-данными на сервере. Даты стартов поставлены после 5 июня 2026 года, чтобы курсы были актуальны во время защиты.

## Сборка

Сборка Android-приложения:

```powershell
.\gradlew.bat :app:assembleDebug
```

Сборка сервера:

```powershell
.\gradlew.bat -p server build
```
