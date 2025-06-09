package ru.igrakov

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
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
import ru.igrakov.utils.Router
import ru.igrakov.utils.Strings

/**
 * @author Andrey Igrakov
 */
@Composable
@Preview
fun App() {

    val settings = remember { FileService.loadSettings() }

    LaunchedEffect(settings) {
        settings?.let {
            ThemeService.setTheme(it.darkTheme)
            Strings.locale = it.locale
        }
    }

    MaterialTheme(
        colorScheme = if (ThemeService.isDarkTheme) darkColorScheme() else lightColorScheme()
    ) {
        Surface {
            Router.Render()
        }
    }
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Zingo",
        state = androidx.compose.ui.window.WindowState(
            position = WindowPosition.Aligned(Alignment.Center)
        ),
        icon = painterResource("logo.png")
    ) {
        App()
    }
}