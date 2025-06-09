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
@Serializable
data class WorkspaceModel(
    val id: String, // Уникальный идентификатор рабочего пространства
    var title: String, // Название рабочего пространства
    val columns: MutableList<ColumnModel>, // Список колонок, связанных с рабочим пространством
    @Serializable(with = LocalDateSerializer::class)
    val createDate: LocalDate,
)