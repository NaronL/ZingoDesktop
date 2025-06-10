package ru.igrakov.ui.screens.element

// Импорты для анимаций, UI-компонентов и управления состояниями
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import ru.igrakov.models.WorkspaceModel
import ru.igrakov.utils.Strings

/**
 * Компонент карточки рабочего пространства с возможностью открыть, редактировать и удалить.
 */
@Composable
fun WorkspaceCard(
    workspace: WorkspaceModel, // Модель рабочего пространства
    onOpen: () -> Unit,         // Колбэк на открытие
    onDelete: () -> Unit,       // Колбэк на удаление
    onEdit: (String) -> Unit,   // Колбэк на редактирование названия
    modifier: Modifier = Modifier
) {
    // Состояние для отображения диалога редактирования
    var showEditDialog by remember { mutableStateOf(false) }
    var editedTitle by remember { mutableStateOf(workspace.title) }

    // Источник взаимодействия для отслеживания нажатия
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    // Анимация изменения тени
    val elevation by animateDpAsState(
        targetValue = if (pressed) 8.dp else 4.dp,
        animationSpec = tween(durationMillis = 50)
    )

    // Анимация изменения толщины границы
    val borderWidth by animateDpAsState(
        targetValue = if (pressed) 2.dp else 1.dp,
        animationSpec = tween(durationMillis = 100)
    )

    // Анимация изменения цвета границы
    val borderColor by animateColorAsState(
        targetValue = if (pressed)
            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
        else
            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
        animationSpec = tween(durationMillis = 100)
    )

    // Анимация масштабирования карточки при нажатии
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = 0.6f)
    )

    // Карточка рабочего пространства
    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(onClick = onOpen), // Клик по карточке вызывает onOpen
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        border = BorderStroke(borderWidth, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Иконка с первой буквой названия
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = CircleShape
                    )
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = workspace.title.take(1).uppercase(), // Первая буква названия
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Колонка с названием и датой создания
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = workspace.title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Created ${workspace.createDate}", // Дата создания
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            // Кнопка редактирования
            IconButton(
                onClick = { showEditDialog = true },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = Strings.t("edit"),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Кнопка удаления
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = Strings.t("delete"),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    // Всплывающее окно для редактирования
    if (showEditDialog) {
        EditWorkspaceDialog(
            title = editedTitle,
            onTitleChange = { editedTitle = it },
            onDismiss = { showEditDialog = false },
            onSave = {
                onEdit(editedTitle)
                showEditDialog = false
            }
        )
    }
}

/**
 * Диалог для редактирования названия рабочего пространства.
 */
@Composable
private fun EditWorkspaceDialog(
    title: String,                        // Текущее название
    onTitleChange: (String) -> Unit,      // Обработчик изменения текста
    onDismiss: () -> Unit,                // Закрытие диалога
    onSave: () -> Unit                    // Сохранение изменений
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            elevation = CardDefaults.cardElevation(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Заголовок диалога
                Text(
                    Strings.t("edit"),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = MaterialTheme.colorScheme.primary
                    )
                )

                // Поле ввода названия
                OutlinedTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    label = { Text(Strings.t("title")) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                // Кнопки внизу диалога
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Кнопка отмены
                    TextButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(Strings.t("cancel"))
                    }

                    Spacer(Modifier.width(12.dp))

                    // Кнопка сохранения
                    Button(
                        onClick = onSave,
                        enabled = title.isNotBlank(), // Кнопка активна, если текст не пустой
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 2.dp,
                            pressedElevation = 8.dp
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(Strings.t("save"))
                    }
                }
            }
        }
    }
}
