package ru.igrakov.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.igrakov.models.CardModel
import ru.igrakov.models.ColumnModel
import ru.igrakov.models.Difficulty
import ru.igrakov.utils.Strings
import java.util.*

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
    modifier: Modifier = Modifier
) {
    // Состояния для показа диалога создания новой карточки и данных новой карточки
    var showNewCardDialog by remember { mutableStateOf(false) }
    var newCardText by remember { mutableStateOf("") }
    var newCardDescription by remember { mutableStateOf("") }
    var newCardDifficulty by remember { mutableStateOf(Difficulty.EASY) }
    var newCardColor by remember { mutableStateOf(Color.White) }

    // Основная карточка колонки с заголовком и списком карточек
    Card(
        modifier = modifier.width(280.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Верхняя строка с заголовком колонки и кнопкой удаления
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

            // Список карточек с прокруткой
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Отображение каждой карточки в колонке
                columnData.cards.forEach { card ->
                    CardView(
                        card = card,
                        onEdit = { updateCard ->
                            onUpdateCard(updateCard)
                            onUpdate() // общий апдейт после изменения
                        },
                        onDelete = {
                            onDeleteCard(card.id)
                            onUpdate() // общий апдейт после удаления
                        }
                    )
                }
            }

            // Кнопка для открытия диалога добавления новой карточки
            FilledTonalButton(
                onClick = { showNewCardDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = Strings.t("add_card"))
            }
        }
    }

    // Диалог добавления новой карточки
    if (showNewCardDialog) {
        AlertDialog(
            onDismissRequest = { showNewCardDialog = false },
            title = { Text(text = Strings.t("new_card")) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Поле для ввода заголовка карточки
                    OutlinedTextField(
                        value = newCardText,
                        onValueChange = { newCardText = it },
                        label = { Text(Strings.t("title")) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Поле для ввода описания карточки
                    OutlinedTextField(
                        value = newCardDescription,
                        onValueChange = { newCardDescription = it },
                        label = { Text(Strings.t("description")) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Выбор сложности карточки
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

                    // Выбор цвета карточки
                    Text(Strings.t("color_card"))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Текущий выбранный цвет в виде круга
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

                        // Палитра доступных цветов для выбора
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
            // Кнопка подтверждения добавления карточки
            confirmButton = {
                Button(
                    onClick = {
                        // Добавляем карточку только если заголовок не пустой
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
                            // Сбрасываем поля формы
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
            // Кнопка отмены добавления карточки
            dismissButton = {
                TextButton(onClick = { showNewCardDialog = false }) {
                    Text(Strings.t("cancel"))
                }
            }
        )
    }
}
