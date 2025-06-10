package ru.igrakov.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import ru.igrakov.models.CardModel
import ru.igrakov.models.ColumnModel
import ru.igrakov.models.Difficulty
import ru.igrakov.utils.Strings
import java.util.*
import kotlin.math.roundToInt

/**
 * Компонент отображения колонки с карточками в стиле Trello.
 *
 * @param columnData данные колонки, включая заголовок и список карточек
 * @param onAddCard callback для добавления новой карточки
 * @param onDeleteColumn callback для удаления колонки
 * @param onUpdateCard callback для обновления карточки
 * @param onDeleteCard callback для удаления карточки по id
 * @param onUpdate callback общий callback для обновления состояния
 * @param modifier модификатор для внешнего управления композаблом
 */
@Composable
fun ColumnView(
    columnData: ColumnModel,
    onAddCard: (CardModel) -> Unit,
    onDeleteColumn: () -> Unit,
    onUpdateCard: (CardModel) -> Unit,
    onDeleteCard: (cardId: String) -> Unit,
    onUpdate: () -> Unit,
    onMoveCard: (CardModel, String) -> Unit,
    isDropTarget: Boolean,
    onDragStart: (CardModel) -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Состояния для показа диалога создания новой карточки и данных новой карточки
    var showNewCardDialog by remember { mutableStateOf(false) }
    var newCardText by remember { mutableStateOf("") }
    var newCardDescription by remember { mutableStateOf("") }
    var newCardDifficulty by remember { mutableStateOf(Difficulty.EASY) }
    var newCardColor by remember { mutableStateOf(Color.White) }

    var targetColumnId by remember { mutableStateOf<String?>(null) }

    // Основная карточка колонки с заголовком и списком карточек

    val borderColor by animateColorAsState(
        if (isDropTarget) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else Color.Transparent,
        animationSpec = tween(durationMillis = 200)
    )

    val backgroundColor by animateColorAsState(
        if (isDropTarget) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        else MaterialTheme.colorScheme.surface,
        animationSpec = tween(durationMillis = 200)
    )

    Card(
        modifier = modifier
            .width(280.dp)
            .border(
                width = animateDpAsState(if (isDropTarget) 2.dp else 0.dp).value,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = columnData.title,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                )
                IconButton(onClick = { onDeleteColumn() }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = Strings.t("delete_column")
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(columnData.cards, key = { it.id }) { card ->
                    DraggableCard(
                        card = card,
                        onEdit = { updatedCard ->
                            onUpdateCard(updatedCard)
                            onUpdate()
                        },
                        onDelete = {
                            onDeleteCard(card.id)
                            onUpdate()
                        },
                        onDragStart = { onDragStart(card) },
                        onDragEnd = {
                            onDragEnd()
                            onMoveCard(card, columnData.id)
                        }
                    )
                }
            }

            FilledTonalButton(
                onClick = { showNewCardDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = Strings.t("add_card"))
            }
        }
    }

    if (showNewCardDialog) {
        AlertDialog(
            onDismissRequest = { showNewCardDialog = false },
            title = { Text(text = Strings.t("new_card")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newCardText,
                        onValueChange = { newCardText = it },
                        label = { Text(Strings.t("title")) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newCardDescription,
                        onValueChange = { newCardDescription = it },
                        label = { Text(Strings.t("description")) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(Strings.t("difficulty"))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Difficulty.entries.forEach { diff ->
                            DifficultyButton(
                                difficulty = diff,
                                isSelected = newCardDifficulty == diff,
                                onClick = { newCardDifficulty = diff }
                            )
                        }
                    }
                    Text(Strings.t("color_card"))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(newCardColor, RoundedCornerShape(50))
                                .border(
                                    width = 2.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(50)
                                )
                        )
                        listOf(
                            Color.White, Color(0xFFFFF9C4), Color(0xFFBBDEFB),
                            Color(0xFFC8E6C9), Color(0xFFFFCDD2)
                        ).forEach { availableColor ->
                            ColorPickerButton(
                                color = availableColor,
                                isSelected = newCardColor == availableColor,
                                onClick = { newCardColor = availableColor }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCardText.isNotBlank()) {
                            onAddCard(
                                CardModel(
                                    id = UUID.randomUUID().toString(),
                                    text = newCardText,
                                    description = newCardDescription,
                                    difficulty = newCardDifficulty,
                                    color = newCardColor
                                )
                            )
                            newCardText = ""
                            newCardDescription = ""
                            newCardDifficulty = Difficulty.EASY
                            newCardColor = Color.White
                            showNewCardDialog = false
                        }
                    }
                ) {
                    Text(Strings.t("add"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewCardDialog = false }) {
                    Text(Strings.t("cancel"))
                }
            }
        )
    }
}

@Composable
fun DraggableCard(
    card: CardModel,
    onEdit: (CardModel) -> Unit,
    onDelete: () -> Unit,
    onDragStart: () -> Unit,
    onDragEnd: () -> Unit
) {
    var isDragging by remember { mutableStateOf(false) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    val elevation by animateDpAsState(if (isDragging) 16.dp else 4.dp)
    val borderColor by animateColorAsState(
        if (isDragging) MaterialTheme.colorScheme.primary else Color.Transparent
    )
    val scale by animateFloatAsState(if (isDragging) 1.05f else 1f)

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        offsetX = 0f
                        offsetY = 0f
                        isDragging = true
                        onDragStart()
                    },
                    onDragEnd = {
                        offsetX = 0f
                        offsetY = 0f
                        isDragging = false
                        onDragEnd()
                    },
                    onDragCancel = {
                        offsetX = 0f
                        offsetY = 0f
                        isDragging = false
                        onDragEnd()
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                )
            }
    ) {
        CardView(
            card = card,
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation)
                .border(
                    width = if (isDragging) 2.dp else 0.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(8.dp)
                )
                .scale(scale),
            onEdit = onEdit,
            onDelete = onDelete
        )
    }
}