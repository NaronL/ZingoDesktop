package ru.igrakov.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.igrakov.utils.Strings

/**
 * Верхняя панель приложения (AppBar) с выравниванием по центру.
 * Содержит название приложения и кнопку перехода к настройкам.
 * Использует Material3.
 * @param onSettingsClick Лямбда, вызываемая при нажатии на кнопку настроек.
 * @author Andrey Igrakov
 **/
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(onSettingsClick: () -> Unit) {
    CenterAlignedTopAppBar(
        title = {
            // Название приложения
            Text(text = "Zingo", style = MaterialTheme.typography.titleLarge)
        },
        actions = {
            // Кнопка настроек
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Default.Settings, // Иконка настроек
                    contentDescription = Strings.t("settings") // Локализованное описание для доступности
                )
            }
        },
        modifier = Modifier.fillMaxWidth() // Занимает всю ширину контейнера
    )
}

/**
 * Превью для Composable редактора.
 * Отображает AppBar без логики обработки нажатия.
 */
@Composable
fun AppBarPreview() {
    MaterialTheme {
        AppBar(onSettingsClick = {}) // Пустая обработка нажатия для предпросмотра
    }
}
