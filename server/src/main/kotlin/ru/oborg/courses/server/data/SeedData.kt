package ru.oborg.courses.server.data

import ru.oborg.courses.server.domain.repository.AuthRepository
import ru.oborg.courses.server.security.PasswordHasher
import java.time.LocalDate

class ServerSeeder(
    private val authRepository: AuthRepository,
    private val courseRepository: JdbcCourseRepository
) {
    fun seed() {
        seedUsers()
        seedCourses()
        seedReviews()
    }

    private fun seedUsers() {
        authRepository.createUser(
            fullName = "Демо Студент",
            email = "student@oborg.ru",
            passwordHash = PasswordHasher.hash("student123"),
            role = "student"
        )
        authRepository.createUser(
            fullName = "Администратор ОбОрг",
            email = "admin@oborg.ru",
            passwordHash = PasswordHasher.hash("admin123"),
            role = "admin"
        )
    }

    private fun seedCourses() {
        val firstStartDate = LocalDate.of(2026, 6, 10)

        listOf(
            SeedCourse(
                title = "Основы Kotlin для Android",
                description = "Практический курс по синтаксису Kotlin, коллекциям, корутинам и подготовке к разработке Android-приложений.",
                teacher = "Мария Климова",
                durationHours = 36,
                priceRub = 5900,
                level = "Начальный",
                startsAt = firstStartDate,
                seatsTotal = 24
            ),
            SeedCourse(
                title = "Jetpack Compose: интерфейсы",
                description = "Создание современных экранов на Compose: состояние, навигация, Material 3 и работа с ViewModel.",
                teacher = "Илья Сафронов",
                durationHours = 42,
                priceRub = 7900,
                level = "Средний",
                startsAt = firstStartDate.plusDays(2),
                seatsTotal = 18
            ),
            SeedCourse(
                title = "Back-end на Ktor",
                description = "Проектирование REST API, JWT-авторизация, подключение PostgreSQL и обработка ошибок в серверном приложении.",
                teacher = "Алексей Морозов",
                durationHours = 48,
                priceRub = 8900,
                level = "Средний",
                startsAt = firstStartDate.plusDays(5),
                seatsTotal = 16
            ),
            SeedCourse(
                title = "Clean Architecture в мобильной разработке",
                description = "Разделение Android-проекта на data, domain и presentation, use case-слой и тестируемые репозитории.",
                teacher = "Екатерина Волкова",
                durationHours = 30,
                priceRub = 6900,
                level = "Продвинутый",
                startsAt = firstStartDate.plusDays(7),
                seatsTotal = 20
            ),
            SeedCourse(
                title = "Android Pro: сеть и хранение данных",
                description = "Практика работы с Ktor Client, DTO, репозиториями, состояниями загрузки и локальными настройками приложения.",
                teacher = "Никита Орлов",
                durationHours = 40,
                priceRub = 7600,
                level = "Средний",
                startsAt = firstStartDate.plusDays(10),
                seatsTotal = 22
            ),
            SeedCourse(
                title = "UI/UX дизайн мобильных приложений",
                description = "Проектирование экранов, дизайн-система, работа с цветом, типографикой и интерактивными прототипами.",
                teacher = "София Лебедева",
                durationHours = 34,
                priceRub = 7200,
                level = "Начальный",
                startsAt = firstStartDate.plusDays(12),
                seatsTotal = 26
            ),
            SeedCourse(
                title = "SQL и аналитика для образовательных проектов",
                description = "Запросы PostgreSQL, агрегации, отчеты по студентам, расписанию, оплатам и эффективности курсов.",
                teacher = "Дмитрий Крылов",
                durationHours = 32,
                priceRub = 6800,
                level = "Начальный",
                startsAt = firstStartDate.plusDays(14),
                seatsTotal = 28
            ),
            SeedCourse(
                title = "Интернет-маркетинг образовательных программ",
                description = "Анализ аудитории, контент-план, воронка записи на курс и метрики продвижения онлайн-обучения.",
                teacher = "Ольга Смирнова",
                durationHours = 28,
                priceRub = 6400,
                level = "Начальный",
                startsAt = firstStartDate.plusDays(17),
                seatsTotal = 30
            ),
            SeedCourse(
                title = "Business English для IT-специалистов",
                description = "Разговорная практика, переписка с командой, презентация проекта и защита технических решений на английском.",
                teacher = "Алина Воронцова",
                durationHours = 44,
                priceRub = 8100,
                level = "Средний",
                startsAt = firstStartDate.plusDays(20),
                seatsTotal = 20
            ),
            SeedCourse(
                title = "Управление командой и учебными проектами",
                description = "Планирование спринтов, роли в команде, контроль сроков, обратная связь и подготовка итоговой защиты.",
                teacher = "Павел Егоров",
                durationHours = 30,
                priceRub = 7000,
                level = "Продвинутый",
                startsAt = firstStartDate.plusDays(23),
                seatsTotal = 18
            )
        ).forEach { courseRepository.insertSeedCourse(it) }
    }

    private fun seedReviews() {
        val reviewers = listOf(
            SeedReviewer(
                fullName = "Анна Петрова",
                email = "anna.petrova@oborg.ru",
                courseTitle = "Основы Kotlin для Android",
                rating = 5,
                text = "Курс помог быстро войти в Kotlin. Больше всего понравились практические задания после каждого модуля."
            ),
            SeedReviewer(
                fullName = "Михаил Козлов",
                email = "mikhail.kozlov@oborg.ru",
                courseTitle = "Jetpack Compose: интерфейсы",
                rating = 5,
                text = "После курса стало понятнее, как собирать экраны на Compose и не путаться в состоянии."
            ),
            SeedReviewer(
                fullName = "Ирина Соколова",
                email = "irina.sokolova@oborg.ru",
                courseTitle = "Back-end на Ktor",
                rating = 5,
                text = "Хороший темп: маршруты, авторизацию и PostgreSQL собрали в один понятный серверный проект."
            ),
            SeedReviewer(
                fullName = "Денис Романов",
                email = "denis.romanov@oborg.ru",
                courseTitle = "UI/UX дизайн мобильных приложений",
                rating = 4,
                text = "Понравился разбор карточек, цветов и навигации. Стало проще смотреть на интерфейс как на систему."
            )
        )

        reviewers.forEach { reviewer ->
            val user = authRepository.createUser(
                fullName = reviewer.fullName,
                email = reviewer.email,
                passwordHash = PasswordHasher.hash("student123"),
                role = "student"
            ) ?: authRepository.findByEmail(reviewer.email)

            if (user != null) {
                courseRepository.insertSeedReview(
                    userId = user.id,
                    courseTitle = reviewer.courseTitle,
                    rating = reviewer.rating,
                    text = reviewer.text
                )
            }
        }
    }
}

private data class SeedReviewer(
    val fullName: String,
    val email: String,
    val courseTitle: String,
    val rating: Int,
    val text: String
)
