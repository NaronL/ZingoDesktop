package ru.igrakov.models

import kotlinx.serialization.Serializable

/**
 * Модель данных для доски (Board).
 * Представляет собой набор колонок, объединённых под одним заголовком.
 * @author Andrey Igrakov
 **/
@Serializable
data class BoardModel(
    val id: String, // Уникальный идентификатор доски
    val title: String, // Название доски
    val columns: MutableList<ColumnModel> // Список колонок на доске
)