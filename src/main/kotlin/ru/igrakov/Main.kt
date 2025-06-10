package ru.igrakov

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import ru.igrakov.services.FileService
import ru.igrakov.services.ThemeService
import ru.igrakov.utils.DarkColors
import ru.igrakov.utils.LightColors
import ru.igrakov.utils.Router
import ru.igrakov.utils.Strings

/**
 * Точка входа в приложение.
 * Инициализирует настройки, локализацию и тему.
 * Отрисовывает основное содержимое приложения с помощью Compose.
 * @author Andrey Igrakov
 */
@Composable
@Preview
fun App() {

    // Загрузка настроек при старте приложения
    val settings = remember { FileService.loadSettings() }

    // Синхронизация темы и локали после загрузки настроек
    LaunchedEffect(settings) {
        settings?.let {
            ThemeService.setTheme(it.darkTheme) // Устанавливаем тему
            Strings.locale = it.locale // Устанавливаем локаль
        }
    }

    // Применяем тему (светлую или тёмную)
    MaterialTheme(
        colorScheme = if (ThemeService.isDarkTheme) DarkColors else LightColors
    ) {
        Surface {
            Router.Render() // Отрисовываем основную навигацию приложения
        }
    }
}

/**
 * Главная функция запуска Desktop приложения.
 * Создаёт окно приложения, задаёт его иконку, заголовок и позицию.
 */
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication, // Закрытие приложения
        title = "Zingo", // Заголовок окна
        state = androidx.compose.ui.window.WindowState(
            position = WindowPosition.Aligned(Alignment.Center) // Центрируем окно при запуске
        ),
        icon = painterResource("logo.png") // Иконка приложения
    ) {
        App() // Запускаем основной UI
    }
}
