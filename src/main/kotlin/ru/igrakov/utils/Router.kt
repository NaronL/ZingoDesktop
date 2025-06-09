package ru.igrakov.utils

import SettingsScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import ru.igrakov.ui.screens.BoardScreen
import ru.igrakov.ui.screens.WorkspacesScreen

/**
 * @author Andrey Igrakov
 */
object Router {

    enum class Screen { Workspaces, Board, Settings }

    val currentScreen = mutableStateOf(Screen.Workspaces)
    var selectedWorkspaceId: String? = null
    var currentFolderPath = ""
    var currentTheme = false
    var currentLocale = "en"

    @Composable
    fun Render() = when (currentScreen.value) {
        Screen.Workspaces -> WorkspacesScreen()
        Screen.Board -> BoardScreen()
        Screen.Settings -> SettingsScreen(
            onSave = ::updateSettings,
            onLogout = { navigate(Screen.Workspaces) },
            currentFolderPath = currentFolderPath,
            currentTheme = currentTheme,
            currentLocale = currentLocale
        )
    }

    private fun updateSettings(folderPath: String, theme: Boolean, locale: String) {
        currentFolderPath = folderPath
        currentTheme = theme
        currentLocale = locale
    }

    fun navigate(screen: Screen, workspaceId: String? = null) {
        currentScreen.value = screen
        selectedWorkspaceId = workspaceId
    }

}
