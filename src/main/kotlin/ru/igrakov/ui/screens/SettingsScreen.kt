import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.twotone.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.igrakov.models.SettingsModel
import ru.igrakov.services.FileService
import ru.igrakov.services.ThemeService
import ru.igrakov.utils.Strings

// Основной Composable для экрана настроек
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onSave: (String, Boolean, String) -> Unit,  // Колбек для сохранения настроек
    onLogout: () -> Unit,                        // Колбек для выхода из аккаунта
    currentFolderPath: String,                   // Текущий путь к папке (не используется явно)
    currentTheme: Boolean,                       // Текущая тема (не используется явно)
    currentLocale: String,                       // Текущий язык (не используется явно)
    modifier: Modifier = Modifier                // Модификатор для настройки внешнего вида
) {
    // Загрузка текущих настроек из файла или использование значений по умолчанию
    val currentSettings = remember { FileService.loadSettings() ?: FileService.defaultSettings() }

    // Локальные состояния для изменения значений настроек на экране
    var folderPath by remember { mutableStateOf(currentSettings.folderPath) }
    var isDarkTheme by remember { mutableStateOf(currentSettings.darkTheme) }
    var selectedLocale by remember { mutableStateOf(currentSettings.locale) }
    var isLocaleMenuExpanded by remember { mutableStateOf(false) }  // Управляет раскрытием меню выбора языка

    // Список доступных локалей и отображаемые имена для пользователя
    val availableLocales = listOf("en", "ru", "es", "fr", "de")
    val localeDisplayNames = mapOf(
        "en" to "English",
        "ru" to "Русский",
        "es" to "Español",
        "fr" to "Français",
        "de" to "Deutsch"
    )

    // Корневой контейнер с градиентным фоном и заполнением всего экрана
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.05f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        // Основной столбец с контентом настроек, с анимацией изменения размера
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .animateContentSize(animationSpec = tween(300)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Заголовок с иконкой и текстом "Настройки", с анимацией появления
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = Strings.t("settings"), // Локализованный текст "Настройки"
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            // Секция выбора папки с заголовком и иконкой
            SettingSection(title = Strings.t("path_folder"), icon = Icons.Default.Home) {
                // Поле ввода с текущим путем к папке
                OutlinedTextField(
                    value = folderPath,
                    onValueChange = { folderPath = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = {
                        Icon(Icons.Default.Home, contentDescription = null)
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp)) // Отступ между секциями

            // Секция выбора темы (светлая/темная)
            SettingSection(title = Strings.t("theme"), icon = Icons.Default.Menu) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isDarkTheme) Strings.t("dark_theme") else Strings.t("light_theme"),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = { isDarkTheme = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Секция выбора языка с выпадающим меню
            SettingSection(title = Strings.t("language"), icon = Icons.TwoTone.LocationOn) {
                ExposedDropdownMenuBox(
                    expanded = isLocaleMenuExpanded,
                    onExpandedChange = { isLocaleMenuExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Поле с выбранным языком, только для чтения
                    TextField(
                        value = localeDisplayNames[selectedLocale] ?: selectedLocale,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(Strings.t("language")) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = isLocaleMenuExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Выпадающее меню с выбором языка
                    ExposedDropdownMenu(
                        expanded = isLocaleMenuExpanded,
                        onDismissRequest = { isLocaleMenuExpanded = false }
                    ) {
                        availableLocales.forEach { locale ->
                            DropdownMenuItem(
                                text = { Text(localeDisplayNames[locale] ?: locale) },
                                onClick = {
                                    selectedLocale = locale
                                    isLocaleMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Кнопки "Сохранить" и "Выйти" в ряд с отступами
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = {
                        // При сохранении - создаём модель настроек и сохраняем её,
                        // затем меняем тему и локаль, вызываем колбек onSave
                        val newSettings = SettingsModel(
                            folderPath = folderPath,
                            darkTheme = isDarkTheme,
                            locale = selectedLocale
                        )
                        FileService.saveSettings(newSettings)
                        ThemeService.setTheme(isDarkTheme)
                        Strings.locale = selectedLocale
                        onSave(folderPath, isDarkTheme, selectedLocale)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Done, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(Strings.t("save"))
                }

                OutlinedButton(
                    onClick = onLogout,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(Strings.t("exit"))
                }
            }
        }
    }
}

// Вспомогательный компонент для секций настроек с заголовком, иконкой и контентом
@Composable
fun SettingSection(
    title: String,            // Заголовок секции
    icon: ImageVector,        // Иконка для заголовка
    content: @Composable ColumnScope.() -> Unit  // Контент секции (например, поле ввода, переключатель)
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            content() // Вставляем содержимое секции
        }
    }
}
