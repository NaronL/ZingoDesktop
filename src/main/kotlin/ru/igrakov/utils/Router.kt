package ru.igrakov.utils

import SettingsScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import ru.igrakov.ui.screens.BoardScreen
import ru.igrakov.ui.screens.WorkspacesScreen

/**
 * Объект Router отвечает за навигацию между экранами приложения
 * и хранит состояние текущего экрана, выбранного рабочего пространства,
 * а также настройки пользователя (текущая папка, тема и локаль).
 * @author Andrey Igrakov
 */
object Router {

    // Перечисление экранов, между которыми можно навигировать
    enum class Screen { Workspaces, Board, Settings }

    // Текущий выбранный экран, observable состояние для Compose
    val currentScreen = mutableStateOf(Screen.Workspaces)

    // ID выбранного рабочего пространства (если есть)
    var selectedWorkspaceId: String? = null

    // Путь к текущей папке, выбранной в настройках
    var currentFolderPath = ""

    // Текущая тема: false - светлая, true - тёмная (по логике приложения)
    var currentTheme = false

    // Текущая локаль приложения (например, "ru" для русского)
    var currentLocale = "ru"

    /**
     * Функция-компонент Compose, которая рендерит экран,
     * соответствующий текущему значению currentScreen.
     */
    @Composable
    fun Render() = when (currentScreen.value) {
        Screen.Workspaces -> WorkspacesScreen()  // Экран рабочих пространств
        Screen.Board -> BoardScreen()            // Основной экран доски (карточек, задач)
        Screen.Settings -> SettingsScreen(       // Экран настроек приложения
            onSave = ::updateSettings,           // Колбэк для сохранения настроек
            onLogout = { navigate(Screen.Workspaces) }, // При выходе - переход к рабочим пространствам
            currentFolderPath = currentFolderPath, // Передаём текущий путь папки
            currentTheme = currentTheme,           // Передаём текущую тему
            currentLocale = currentLocale          // Передаём текущую локаль
        )
    }

    /**
     * Обновляет настройки приложения с новыми значениями:
     * путь к папке, тема и локаль.
     */
    private fun updateSettings(folderPath: String, theme: Boolean, locale: String) {
        currentFolderPath = folderPath
        currentTheme = theme
        currentLocale = locale
    }

    /**
     * Функция для навигации между экранами.
     * Можно указать опциональный workspaceId, если надо выбрать конкретное рабочее пространство.
     */
    fun navigate(screen: Screen, workspaceId: String? = null) {
        currentScreen.value = screen
        selectedWorkspaceId = workspaceId
    }

}
