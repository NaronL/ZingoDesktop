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
 * @author Andrey Igrakov
 **/
@Composable
fun ColumnView(
    columnData: ColumnModel,
    onAddCard: (CardModel) -> Unit,
    onDeleteColumn: () -> Unit,
    onUpdateCard: (CardModel) -> Unit,
    onUpdate: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showNewCardDialog by remember { mutableStateOf(false) }
    var newCardText by remember { mutableStateOf("") }
    var newCardDescription by remember { mutableStateOf("") }
    var newCardDifficulty by remember { mutableStateOf(Difficulty.EASY) }
    var newCardColor by remember { mutableStateOf(Color.White) }

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

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                columnData.cards.forEach { card ->
                    CardView(
                        card = card,
                        onEdit = { updateCard ->
                            onUpdateCard(updateCard)
                            onUpdate()
                        },
                        onDelete = {
                            columnData.cards.remove(card)
                            onUpdate()
                        }
                    )
                }
            }

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

    if (showNewCardDialog) {
        AlertDialog(
            onDismissRequest = { showNewCardDialog = false },
            title = { Text(text = Strings.t("new_card")) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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