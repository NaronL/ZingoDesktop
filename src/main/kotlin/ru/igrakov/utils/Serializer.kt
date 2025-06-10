package ru.igrakov.utils

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDate

/**
 * Сериализатор для типа LocalDate.
 * Позволяет преобразовывать дату в строку и обратно для корректной работы с JSON.
 * Используется в моделях, где необходимо сохранять дату в файл.
 * @author Andrey Igrakov
 */
object LocalDateSerializer : KSerializer<LocalDate> {

    // Описание сериализуемого типа: LocalDate будет представляться как строка
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalDate", PrimitiveKind.STRING)

    // Преобразование LocalDate в строку для записи в JSON
    override fun serialize(encoder: Encoder, value: LocalDate) =
        encoder.encodeString(value.toString())

    // Преобразование строки из JSON обратно в LocalDate
    override fun deserialize(decoder: Decoder): LocalDate =
        LocalDate.parse(decoder.decodeString())
}

/**
 * Сериализатор для типа Color.
 * Позволяет сохранять цвет в формате шестнадцатеричной строки и восстанавливать его обратно.
 * Используется для хранения цветов в JSON.
 */
object ColorSerializer : KSerializer<Color> {

    // Описание сериализуемого типа: Color будет представляться как строка
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Color", PrimitiveKind.STRING)

    // Преобразование цвета в шестнадцатеричную строку для записи в JSON
    override fun serialize(encoder: Encoder, value: Color) =
        encoder.encodeString(value.value.toString(16)) // Сохраняем цвет в hex-строке

    // Преобразование шестнадцатеричной строки обратно в объект Color
    override fun deserialize(decoder: Decoder): Color =
        Color(decoder.decodeString().toULong(16))
}
