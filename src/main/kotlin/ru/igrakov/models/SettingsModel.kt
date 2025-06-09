package ru.igrakov.models

import kotlinx.serialization.Serializable

/**
 * Модель данных для настроек приложения (Settings).
 * Содержит пользовательские параметры для персонализации приложения.
 * @author Andrey Igrakov
 **/
@Serializable
data class SettingsModel(
    val folderPath: String, // Путь к рабочей папке проекта
    val darkTheme: Boolean, // Флаг включения тёмной темы
    val locale: String // Локализация (язык интерфейса)
)
