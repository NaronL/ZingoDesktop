package ru.igrakov.models

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import ru.igrakov.utils.ColorSerializer
import ru.igrakov.utils.LocalDateSerializer
import ru.igrakov.utils.Strings
import java.time.LocalDate
import java.time.ZoneId

/**
 * Модель данных карточки (Card).
 * Карточка — это элемент внутри колонки, описывающий задачу или единицу работы.
 * Включает в себя текст, описание, сложность, цвет, участников и крайний срок.
 * @author Andrey Igrakov
 */
@Serializable // Класс поддерживает сериализацию (например, для хранения или передачи через сеть)
data class CardModel(
    val id: String, // Уникальный идентификатор карточки
    var text: String, // Основной текст или заголовок карточки
    var description: String, // Подробное описание задачи или элемента
    var difficulty: Difficulty, // Уровень сложности карточки (перечисление: EASY, MEDIUM, HARD)
    @Serializable(with = ColorSerializer::class)
    var color: Color, // Цвет карточки для визуальной идентификации, сериализуется с помощью кастомного сериализатора
    var people: MutableList<Person> = mutableListOf(), // Список участников, назначенных на эту карточку
    @Serializable(with = LocalDateSerializer::class)
    var deadline: LocalDate? = null, // Крайний срок выполнения задачи, может быть не задан (null)
)

/// Перечисление для задания уровня сложности карточки.
enum class Difficulty {
    EASY,   // Лёгкий уровень
    MEDIUM, // Средний уровень
    HARD;   // Высокий уровень

    /// Локализованное название для уровня сложности.
    val title: String
        get() = when (this) {
            EASY -> Strings.t("difficulty_easy") // Возвращает строку из локализации для "лёгкий"
            MEDIUM -> Strings.t("difficulty_middle") // Возвращает строку из локализации для "средний"
            HARD -> Strings.t("difficulty_hard") // Возвращает строку из локализации для "тяжёлый"
        }

    /// Цвет, связанный с уровнем сложности (для визуального обозначения).
    val color: Color
        get() = when (this) {
            EASY -> Color(0xFF81C784)   // Зелёный для лёгкого уровня
            MEDIUM -> Color(0xFFFFF176) // Жёлтый для среднего уровня
            HARD -> Color(0xFFE57373)   // Красный для высокого уровня
        }
}

/// Модель участника карточки (Person).
@Serializable
data class Person(
    val id: String, // Уникальный идентификатор участника
    val name: String, // Имя участника
    @Serializable(with = ColorSerializer::class)
    val iconColor: Color = randomColor(), // Цвет иконки участника, по умолчанию выбирается случайный цвет
) {
    companion object {
        // Список возможных цветов для иконок участников
        private val colors = listOf(
            Color(0xFFEF5350), Color(0xFFEC407A), Color(0xFFAB47BC),
            Color(0xFF7E57C2), Color(0xFF5C6BC0), Color(0xFF42A5F5),
            Color(0xFF29B6F6), Color(0xFF26C6DA), Color(0xFF26A69A),
            Color(0xFF66BB6A), Color(0xFF9CCC65), Color(0xFFFFEE58),
            Color(0xFFFFCA28), Color(0xFFFFA726), Color(0xFFFF7043)
        )

        /// Функция для выбора случайного цвета из доступного списка
        fun randomColor(): Color = colors.random()
    }
}