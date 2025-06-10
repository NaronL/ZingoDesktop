package ru.igrakov.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.material3.ListItemDefaults.contentColor
import androidx.compose.material3.TabRowDefaults.secondaryContentColor
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush.Companion.horizontalGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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

    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy") }
    val isOverdue = remember(card.deadline) {
        card.deadline?.isBefore(LocalDate.now()) ?: false
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = card.color,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Difficulty badge
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
                            text = card.difficulty.title,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                    // Card content
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = card.text,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 2
                        )
                        if (card.description.isNotEmpty()) {
                            Text(
                                text = card.description,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 4
                            )
                        }
                    }

                    card.deadline?.let { deadline ->
                        Text(
                            text = "Срок: ${deadline.format(dateFormatter)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isOverdue) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                // Action buttons
                Row {
                    IconButton(
                        onClick = { showEditDialog = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = Strings.t("edit"),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = Strings.t("delete"),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // People section
            PeopleSection(
                people = card.people,
                onAddPerson = { showAddPersonDialog = true },
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }

    // Dialogs
    if (showEditDialog) {
        EditCardDialog(
            card = card,
            onDismiss = { showEditDialog = false },
            onSave = { updatedCard ->
                onEdit(updatedCard)
                showEditDialog = false
            }
        )
    }

    if (showAddPersonDialog) {
        AddPersonDialog(
            onDismiss = { showAddPersonDialog = false },
            onAddPerson = { person ->
                val updatedPeople = card.people.toMutableList().apply { add(person) }
                onEdit(card.copy(people = updatedPeople))
            }
        )
    }
}

@Composable
private fun PeopleSection(
    people: List<Person>,
    onAddPerson: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (people.isNotEmpty()) {
            PeopleAvatars(people = people)
        }

        TextButton(
            onClick = onAddPerson,
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Text(Strings.t("add_person"))
        }
    }
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
private fun EditCardDialog(
    card: CardModel,
    onDismiss: () -> Unit,
    onSave: (CardModel) -> Unit
) {
    var text by remember { mutableStateOf(card.text) }
    var description by remember { mutableStateOf(card.description) }
    var difficulty by remember { mutableStateOf(card.difficulty) }
    var color by remember { mutableStateOf(card.color) }
    var isEditingPeople by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var deadline by remember { mutableStateOf(card.deadline) }
    var showAddPersonDialog by remember { mutableStateOf(false) }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy") }
    val deadlineText = remember(deadline) {
        deadline?.format(dateFormatter) ?: Strings.t("no_dedline")
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            onDateSelected = { date ->
                deadline = date
                showDatePicker = false
            },
            initialDate = deadline ?: LocalDate.now()
        )
    }

    if (showAddPersonDialog) {
        AddPersonDialog(
            onDismiss = { showAddPersonDialog = false },
            onAddPerson = { person ->
                val updatedPeople = card.people.toMutableList().apply { add(person) }
                onSave(card.copy(people = updatedPeople))
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(Strings.t("edit_card")) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Strings.t("title") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    ),
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Strings.t("description") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    ),
                )

                // Difficulty selector
                Column {
                    Text(Strings.t("difficulty"), style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Difficulty.entries.forEach { diff ->
                            DifficultyButton(
                                difficulty = diff,
                                isSelected = difficulty == diff,
                                onClick = { difficulty = diff },
                            )
                        }
                    }
                }

                // Color selector
                Column {
                    Text(Strings.t("color_card"), style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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
                }

                // Deadline selector
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Срок: $deadlineText")
                    Button(onClick = { showDatePicker = true }) {
                        Text(if (deadline == null) "Установить срок" else Strings.t("change"))
                    }
                    if (deadline != null) {
                        TextButton(onClick = { deadline = null }) {
                            Text(Strings.t("remove"))
                        }
                    }
                }

                // People management
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(Strings.t("people"))
                        IconToggleButton(
                            checked = isEditingPeople,
                            onCheckedChange = { isEditingPeople = it }
                        ) {
                            Icon(
                                if (isEditingPeople) Icons.Default.Delete else Icons.Default.Edit,
                                contentDescription = Strings.t("edit_people")
                            )
                        }
                    }

                    if (card.people.isNotEmpty()) {
                        EditablePeopleAvatars(
                            people = card.people,
                            isEditing = isEditingPeople,
                            onPersonRemoved = { person ->
                                val updatedPeople = card.people.toMutableList().apply { remove(person) }
                                onSave(card.copy(people = updatedPeople))
                            },
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    Button(
                        onClick = { showAddPersonDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(Strings.t("add_person"))
                    }
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
                        color = color,
                        deadline = deadline
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
        title = { Text(Strings.t("add")) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(Strings.t("full_name")) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    ),
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
                Text(Strings.t("add"))
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
fun DifficultyButton(
    difficulty: Difficulty,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val targetContainerColor = difficulty.color
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) targetContainerColor else targetContainerColor.copy(alpha = 0.5f)
    )

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = Color.White
        ),
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        modifier = Modifier.height(36.dp)
    ) {
        Text(difficulty.title)
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
                        title = { Text(Strings.t("remove_person")) },
                        text = { Text("Удалить ${person.name} с этой карточки?") },
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
                                Text(Strings.t("remove"))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteConfirm = false }) {
                                Text(Strings.t("cancel"))
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
fun DataPicker(
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentMonth by remember { mutableStateOf(selectedDate?.let { YearMonth.from(it) } ?: YearMonth.now()) }
    var selectedDateState by remember { mutableStateOf(selectedDate) }

    Column(
        modifier = modifier
            .padding(24.dp)
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.extraLarge
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = { currentMonth = currentMonth.minusMonths(1) },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Previous month",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            AnimatedContent(
                targetState = currentMonth,
                transitionSpec = {
                    val direction = if (targetState.year > initialState.year) 1 else -1
                    (slideInHorizontally { width -> width * direction } + fadeIn())
                        .togetherWith(slideOutHorizontally { width -> -width * direction } + fadeOut())
                }
            ) { month ->
                Text(
                    text = month.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
                        .replaceFirstChar { it.titlecase(Locale.getDefault()) } +
                            " " + month.year,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            IconButton(
                onClick = { currentMonth = currentMonth.plusMonths(1) },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = "Next month",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            val daysOfWeek = listOf("mo", "tu", "we", "th", "fr", "sa", "su").map { Strings.t(it) }
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 8.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        val daysInMonth = currentMonth.lengthOfMonth()
        val firstDayOfMonth = currentMonth.atDay(1).dayOfWeek.value
        val days = (1..daysInMonth).map { currentMonth.atDay(it) }

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items((1 until firstDayOfMonth).count()) {
                Spacer(Modifier.size(36.dp))
            }

            items(days) { day ->
                val isSelected = selectedDateState?.let { it == day } ?: false
                val isToday = day == LocalDate.now()

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            color = when {
                                isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                                isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                else -> Color.Transparent
                            },
                            shape = CircleShape
                        )
                        .border(
                            width = if (isToday && !isSelected) 1.dp else 0.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            shape = CircleShape
                        )
                        .clickable {
                            selectedDateState = day
                            onDateSelected(day)
                        }
                        .animateContentSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day.dayOfMonth.toString(),
                        color = when {
                            isSelected -> MaterialTheme.colorScheme.onPrimary
                            isToday -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
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

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .width(650.dp)
                .padding(16.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = Strings.t("select_date"),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                DataPicker(
                    selectedDate = selectedDate,
                    onDateSelected = { date -> selectedDate = date },
                    modifier = Modifier.width(300.dp)
                )

                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(Strings.t("cancel"))
                    }

                    Button(
                        onClick = { selectedDate?.let { onDateSelected(it) } },
                        enabled = selectedDate != null,
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text(Strings.t("save"))
                    }
                }
            }
        }
    }
}
