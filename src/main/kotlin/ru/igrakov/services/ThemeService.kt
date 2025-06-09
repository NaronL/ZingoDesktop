package ru.igrakov.services

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * @author Andrey Igrakov
 */
object ThemeService {

    var isDarkTheme by mutableStateOf(true)
        private set

    fun toggleTheme() {
        isDarkTheme = !isDarkTheme
        FileService.loadSettings().let { settings ->
            FileService.saveSettings(settings!!.copy(darkTheme = isDarkTheme))
        }
    }

    fun setTheme(darkTheme: Boolean) {
        isDarkTheme = darkTheme
    }

}