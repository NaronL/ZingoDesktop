package ru.igrakov.services

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Сервис для управления темой приложения.
 * Позволяет переключать между светлой и тёмной темой с поддержкой автоматической реакции UI через Compose.
 * Использует состояние Compose для отслеживания изменений темы.
 * @author Andrey Igrakov
 */
object ThemeService {

    /**
     * Флаг текущей темы.
     * true — тёмная тема, false — светлая тема.
     * Значение обёрнуто в Compose состояние для автоматической реакции интерфейса.
     */
    var isDarkTheme by mutableStateOf(true)
        private set // Запрещаем изменять значение напрямую снаружи, доступ только через setTheme()

    /**
     * Устанавливает тему приложения.
     * @param darkTheme true — установить тёмную тему, false — установить светлую.
     */
    fun setTheme(darkTheme: Boolean) {
        isDarkTheme = darkTheme
    }

}
