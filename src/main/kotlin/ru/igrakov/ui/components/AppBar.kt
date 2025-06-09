package ru.igrakov.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.igrakov.utils.Strings

/**
 * @author Andrey Igrakov
 **/
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(onSettingsClick: () -> Unit) {
    CenterAlignedTopAppBar(
        title = {
            Text(text = "Zingo", style = MaterialTheme.typography.titleLarge)
        },
        actions = {
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Default.Settings, contentDescription = Strings.t("settings"))
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun AppBarPreview() {
    MaterialTheme {
        AppBar(onSettingsClick = {})
    }
}