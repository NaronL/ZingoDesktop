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
 * @author Andrey Igrakov
 */
@Serializable
data class CardModel(
    val id: String,
    var text: String,
    var description: String = "",
    var difficulty: Difficulty = Difficulty.EASY,
    @Serializable(with = ColorSerializer::class)
    var color: Color = Color(0xFFFFFFFF),
    var people: MutableList<Person> = mutableListOf(),
    @Serializable(with = LocalDateSerializer::class)
    var deadline: LocalDate? = null,
) {

    fun deadlineTimestamp(): Long? =
        deadline?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()

}

enum class Difficulty(val title: String, val color: Color) {

    EASY(Strings.t("difficulty_easy"), Color.Green),
    MEDIUM(Strings.t("difficulty_middle"), Color.Yellow),
    HARD(Strings.t("difficulty_hard"), Color.Red),

}

@Serializable
data class Person(
    val id: String,
    val name: String,
    @Serializable(with = ColorSerializer::class)
    val iconColor: Color = randomColor(),
) {
    companion object {
        private val colors = listOf(
            Color(0xFFEF5350), Color(0xFFEC407A), Color(0xFFAB47BC),
            Color(0xFF7E57C2), Color(0xFF5C6BC0), Color(0xFF42A5F5),
            Color(0xFF29B6F6), Color(0xFF26C6DA), Color(0xFF26A69A),
            Color(0xFF66BB6A), Color(0xFF9CCC65), Color(0xFFFFEE58),
            Color(0xFFFFCA28), Color(0xFFFFA726), Color(0xFFFF7043)
        )

        fun randomColor(): Color = colors.random()
    }
}