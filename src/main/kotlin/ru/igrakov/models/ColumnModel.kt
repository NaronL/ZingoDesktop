package ru.igrakov.models

import kotlinx.serialization.Serializable

/**
 * Модель данных для колонки (Column).
 * Содержит карточки, сгруппированные по определённому признаку в пределах доски.
 * Может использоваться в разных контекстах (например, в задачах, планах и др.).
 * @author Andrey Igrakov
 **/
@Serializable
data class ColumnModel(
    val id: String, // Уникальный идентификатор колонки
    val title: String, // Название колонки
    val cards: MutableList<CardModel> = mutableListOf() // Список карточек внутри колонки (по умолчанию пустой)
)