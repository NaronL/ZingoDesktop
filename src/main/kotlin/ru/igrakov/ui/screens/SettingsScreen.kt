import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.sharp.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.igrakov.models.SettingsModel
import ru.igrakov.services.FileService
import ru.igrakov.services.ThemeService
import ru.igrakov.utils.Strings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onSave: (String, Boolean, String) -> Unit,
    onLogout: () -> Unit,
    currentFolderPath: String,
    currentTheme: Boolean,
    currentLocale: String,
    modifier: Modifier = Modifier
) {
    val currentSettings = remember { FileService.loadSettings() ?: FileService.defaultSettings() }
    var folderPath by remember { mutableStateOf(currentSettings.folderPath) }
    var isDarkTheme by remember { mutableStateOf(currentSettings.darkTheme) }
    var selectedLocale by remember { mutableStateOf(currentSettings.locale) }
    var isLocaleMenuExpanded by remember { mutableStateOf(false) }

    val availableLocales = listOf("en", "ru", "es", "fr", "de")

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .animateContentSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Заголовок с иконкой
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = Strings.t("settings"),
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            // Карточка с путём к папке
            Surface(
                shape = RoundedCornerShape(20.dp),
                tonalElevation = 4.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = Strings.t("path_folder"),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = folderPath,
                        onValueChange = { folderPath = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Карточка с переключателем темы и выбором локали
            Surface(
                shape = RoundedCornerShape(20.dp),
                tonalElevation = 4.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Тема
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            Strings.t("theme"),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = { isDarkTheme = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    // Локаль
                    ExposedDropdownMenuBox(
                        expanded = isLocaleMenuExpanded,
                        onExpandedChange = { isLocaleMenuExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextField(
                            value = selectedLocale,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(Strings.t("language")) },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isLocaleMenuExpanded)
                            },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Sharp.LocationOn,
                                    contentDescription = null
                                )
                            }
                        )
                        ExposedDropdownMenu(
                            expanded = isLocaleMenuExpanded,
                            onDismissRequest = { isLocaleMenuExpanded = false }
                        ) {
                            availableLocales.forEach { locale ->
                                DropdownMenuItem(
                                    text = { Text(locale) },
                                    onClick = {
                                        selectedLocale = locale
                                        isLocaleMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // Кнопки "Сохранить" и "Выйти"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = {
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
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Done, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(Strings.t("save"))
                }

                Button(
                    onClick = onLogout,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(Strings.t("exit"))
                }
            }
        }
    }
}
