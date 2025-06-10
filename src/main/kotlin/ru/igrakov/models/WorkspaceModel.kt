package ru.igrakov.models

import kotlinx.serialization.Serializable
import ru.igrakov.utils.LocalDateSerializer
import java.time.LocalDate

/**
 * Модель данных для рабочего пространства (Workspace).
 * Позволяет объединить несколько досок и их содержимое в логическую группу.
 * Подразумевает возможность работы с несколькими пространствами в рамках одного приложения.
 * В будущем можно добавить привязку пользователей или разрешений доступа.
 * @author Andrey Igrakov
 **/
@Serializable // Аннотация, указывающая, что данный класс может быть сериализован (например, в JSON)
data class WorkspaceModel(
    val id: String, // Уникальный идентификатор рабочего пространства, используется для различения разных объектов
    var title: String, // Название рабочего пространства, может быть изменено пользователем
    val columns: MutableList<ColumnModel>, // Список колонок, входящих в данное рабочее пространство; позволяет динамически добавлять или удалять колонки
    @Serializable(with = LocalDateSerializer::class) // Указывает, что поле будет сериализоваться с помощью кастомного сериализатора для LocalDate
    val createDate: LocalDate, // Дата создания рабочего пространства; хранится как LocalDate (без времени)
)