package ru.igrakov.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.material3.ListItemDefaults.contentColor
import androidx.compose.material3.TabRowDefaults.secondaryContentColor
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush.Companion.horizontalGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.igrakov.models.CardModel
import ru.igrakov.models.Difficulty
import ru.igrakov.models.Person
import ru.igrakov.utils.Strings
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

/**
 * @author Andrey Igrakov
 */
@Composable
fun CardView(
    card: CardModel,
    onEdit: (CardModel) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var showAddPersonDialog by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf(card.text) }
    var description by remember { mutableStateOf(card.description) }
    var difficulty by remember { mutableStateOf(card.difficulty) }
    var color by remember { mutableStateOf(card.color) }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy") }
    val isOverdue = remember(card.deadline) {
        card.deadline?.isBefore(LocalDate.now()) ?: false
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = card.color),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                card.deadline?.let { deadline ->
                    Text(
                        text = "Deadline: ${deadline.format(dateFormatter)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isOverdue) Color.Red else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Start)
                            .background(
                                brush = horizontalGradient(
                                    colors = listOf(
                                        card.difficulty.color.copy(alpha = 0.8f),
                                        card.difficulty.color.copy(alpha = 0.5f)
                                    )
                                ),
                                shape = RoundedCornerShape(50)
                            )
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = card.difficulty.title.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = card.text,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = contentColor,
                            maxLines = 2
                        )
                        if (card.description.isNotEmpty()) {
                            Text(
                                text = card.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = secondaryContentColor,
                                maxLines = 4
                            )
                        }
                    }
                }
                Row {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Редактировать")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Удалить")
                    }
                }
            }

            if (card.people.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    PeopleAvatars(people = card.people)

                    TextButton(
                        onClick = { showAddPersonDialog = true },
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text("Add person")
                    }
                }
            } else {
                TextButton(
                    onClick = { showAddPersonDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("Add person")
                }
            }
        }
    }

    if (showEditDialog) {
        EditCardDialog(
            card = card,
            onDismiss = { showEditDialog = false },
            onSave = { updatedCard ->
                onEdit(updatedCard)
                showEditDialog = false
            },
            onAddPerson = { showAddPersonDialog = true }
        )
    }

    if (showAddPersonDialog) {
        AddPersonDialog(
            onDismiss = { showAddPersonDialog = false },
            onAddPerson = { person ->
                val updatedPeople = card.people.toMutableList().apply {
                    add(person)
                }
                onEdit(card.copy(people = updatedPeople))
            }
        )
    }
}

@Composable
private fun EditCardDialog(
    card: CardModel,
    onDismiss: () -> Unit,
    onSave: (CardModel) -> Unit,
    onAddPerson: () -> Unit
) {
    var text by remember { mutableStateOf(card.text) }
    var description by remember { mutableStateOf(card.description) }
    var difficulty by remember { mutableStateOf(card.difficulty) }
    var color by remember { mutableStateOf(card.color) }
    var isEditingPeople by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var deadline by remember { mutableStateOf(card.deadline) }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy") }
    val deadlineText = remember(deadline) {
        deadline?.format(dateFormatter) ?: "No deadline"
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            onDateSelected = { date ->
                deadline = date
                showDatePicker = false
            },
            initialDate = deadline
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(Strings.t("edit_card")) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text(Strings.t("title")) }
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(Strings.t("description")) }
                )

                Text(Strings.t("difficulty"))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Difficulty.entries.forEach { diff ->
                        DifficultyButton(
                            difficulty = diff,
                            isSelected = difficulty == diff,
                            onClick = { difficulty = diff }
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
                            .background(color, RoundedCornerShape(50))
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
                            isSelected = color == availableColor,
                            onClick = { color = availableColor }
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Deadline: $deadlineText")
                    Button(onClick = { showDatePicker = true }) {
                        Text(if (deadline == null) "Set Deadline" else "Change Deadline")
                    }
                    if (deadline != null) {
                        TextButton(onClick = { deadline = null }) {
                            Text("Remove")
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("People")
                    IconToggleButton(
                        checked = isEditingPeople,
                        onCheckedChange = { isEditingPeople = it }
                    ) {
                        Icon(
                            if (isEditingPeople) Icons.Default.Delete else Icons.Default.Edit,
                            contentDescription = "Edit people"
                        )
                    }
                }

                if (card.people.isNotEmpty()) {
                    EditablePeopleAvatars(
                        people = card.people,
                        isEditing = isEditingPeople,
                        onPersonRemoved = { person ->
                            val updatedPeople = card.people.toMutableList().apply {
                                remove(person)
                            }
                            onSave(card.copy(people = updatedPeople))
                        },
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Button(
                    onClick = onAddPerson,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Person")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(card.copy(
                        text = text,
                        description = description,
                        difficulty = difficulty,
                        color = color
                    ))
                }
            ) {
                Text(Strings.t("save"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Strings.t("cancel"))
            }
        }
    )
}

@Composable
fun AddPersonDialog(
    onDismiss: () -> Unit,
    onAddPerson: (Person) -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Person") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onAddPerson(Person(
                            id = UUID.randomUUID().toString(),
                            name = name.trim()
                        ))
                        onDismiss()
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeopleAvatars(
    people: List<Person>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy((-8).dp)
    ) {
        people.take(3).forEach { person ->
            Box {
                PersonAvatar(person = person)

                val tooltipState = remember { TooltipState() }
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberRichTooltipPositionProvider(),
                    tooltip = {
                        Surface(
                            modifier = Modifier.shadow(4.dp),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = person.name,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    },
                    state = tooltipState
                ) {
                    Box(modifier = Modifier.size(32.dp))
                }
            }
        }

        if (people.size > 3) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+${people.size - 3}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun PersonAvatar(
    person: Person,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(32.dp)
            .background(person.iconColor, CircleShape)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = person.name.take(1).uppercase(),
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun DifficultyButton(
    difficulty: Difficulty,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val containerColor = when (difficulty) {
        Difficulty.EASY -> Color(0xFF81C784)
        Difficulty.MEDIUM -> Color(0xFFFFB74D)
        Difficulty.HARD -> Color(0xFFE57373)
    }

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) containerColor else containerColor.copy(alpha = 0.5f)
        ),
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            null
        }
    ) {
        Text(difficulty.name)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditablePeopleAvatars(
    people: List<Person>,
    isEditing: Boolean,
    onPersonRemoved: (Person) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()
    val shakeAngle by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy((-8).dp)
    ) {
        people.take(3).forEach { person ->
            val tooltipState = remember { TooltipState() }
            var showDeleteConfirm by remember { mutableStateOf(false) }

            Box {
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberRichTooltipPositionProvider(),
                    tooltip = {
                        Surface(
                            modifier = Modifier.shadow(4.dp),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = person.name,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    },
                    state = tooltipState
                ) {
                    Box(
                        modifier = Modifier
                            .rotate(if (isEditing) shakeAngle else 0f)
                            .clickable {
                                if (isEditing) {
                                    showDeleteConfirm = true
                                }
                            }
                    ) {
                        PersonAvatar(person = person)
                    }
                }

                if (showDeleteConfirm) {
                    AlertDialog(
                        onDismissRequest = { showDeleteConfirm = false },
                        title = { Text("Remove person?") },
                        text = { Text("Remove ${person.name} from this card?") },
                        confirmButton = {
                            Button(
                                onClick = {
                                    onPersonRemoved(person)
                                    showDeleteConfirm = false
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Remove")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteConfirm = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            }
        }

        if (people.size > 3) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+${people.size - 3}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ColorPickerButton(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .background(color, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(50)
            )
    )
}

@Composable
fun DataPickerDialog(
    onDismissRequest: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    initialDate: LocalDate? = null
) {
    var selectedDate by remember { mutableStateOf(initialDate ?: LocalDate.now()) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Select Deadline") },
        text = {
            Column {
                DataPicker(
                    selectedDate = selectedDate,
                    onDateSelected = { date -> selectedDate = date },
                    modifier = Modifier.size(300.dp)
                )

                Spacer(Modifier.height(16.dp))

                // Quick selection buttons
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { selectedDate = LocalDate.now().plusDays(1) }) {
                        Text("Tomorrow")
                    }
                    Button(onClick = { selectedDate = LocalDate.now().plusWeeks(1) }) {
                        Text("Next week")
                    }
                    Button(onClick = { selectedDate = LocalDate.now().plusMonths(1) }) {
                        Text("Next month")
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { selectedDate?.let { onDateSelected(it) } }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DataPicker(
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentMonth by remember { mutableStateOf(selectedDate?.let { YearMonth.from(it) } ?: YearMonth.now()) }
    var selectedDateState by remember { mutableStateOf(selectedDate) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Previous month")
            }

            Text(
                text = currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + currentMonth.year,
                style = MaterialTheme.typography.titleMedium
            )

            IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Next month")
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        val daysInMonth = currentMonth.lengthOfMonth()
        val firstDayOfMonth = currentMonth.atDay(1).dayOfWeek.value
        val days = (1..daysInMonth).map { currentMonth.atDay(it) }

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth()
        ) {
            items((1 until firstDayOfMonth).count()) {
                Spacer(Modifier.size(32.dp))
            }

            items(days) { day ->
                val isSelected = selectedDateState?.let { it == day } ?: false
                val isToday = day == LocalDate.now()

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .padding(2.dp)
                        .background(
                            color = when {
                                isSelected -> MaterialTheme.colorScheme.primary
                                isToday -> MaterialTheme.colorScheme.primaryContainer
                                else -> Color.Transparent
                            },
                            shape = CircleShape
                        )
                        .border(
                            width = if (isToday && !isSelected) 1.dp else 0.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                        .clickable {
                            selectedDateState = day
                            onDateSelected(day)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day.dayOfMonth.toString(),
                        color = when {
                            isSelected -> MaterialTheme.colorScheme.onPrimary
                            isToday -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DatePickerDialog(
    onDismissRequest: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    initialDate: LocalDate? = null
) {
    var selectedDate by remember { mutableStateOf(initialDate ?: LocalDate.now()) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Select Deadline") },
        text = {
            Column {
                DataPicker(
                    selectedDate = selectedDate,
                    onDateSelected = { date -> selectedDate = date },
                    modifier = Modifier.size(300.dp)
                )

                Spacer(Modifier.height(16.dp))

                // Quick selection buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(onClick = { selectedDate = LocalDate.now().plusDays(1) }) {
                        Text("Tomorrow")
                    }
                    Button(onClick = { selectedDate = LocalDate.now().plusWeeks(1) }) {
                        Text("Next week")
                    }
                    Button(onClick = { selectedDate = LocalDate.now().plusMonths(1) }) {
                        Text("Next month")
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { selectedDate?.let { onDateSelected(it) } }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}
