package ru.igrakov.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.igrakov.models.BoardModel
import ru.igrakov.models.CardModel
import ru.igrakov.models.ColumnModel
import ru.igrakov.services.FileService
import ru.igrakov.ui.components.ColumnView
import ru.igrakov.utils.Router
import ru.igrakov.utils.Strings
import java.util.*

/**
 * @author Andrey Igrakov
 */
@Composable
fun BoardScreen() {

    val workspaceId = Router.selectedWorkspaceId
    var showNewColumnDialog by remember { mutableStateOf(false) }
    var boardModel by remember { mutableStateOf<BoardModel?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    var newColumnTitle by remember { mutableStateOf("") }

    LaunchedEffect(workspaceId) {
        workspaceId?.let {
            boardModel = FileService.loadBoard(it) ?: createNewBoard(it).also(FileService::saveBoard)
        }
        isLoading = false
    }

    if (workspaceId == null && !isLoading) {
        LaunchedEffect(Unit) { Router.navigate(Router.Screen.Workspaces) }
        return
    }

    Scaffold(
        topBar = {
            BoardAppBar(
                title = boardModel?.title.orEmpty(),
                onBack = { Router.navigate(Router.Screen.Workspaces) },
                onSettings = { Router.navigate(Router.Screen.Settings) }
            )
        },

        floatingActionButton = {
            FloatingActionButton(onClick = { showNewColumnDialog = true } ) { Text("+") }
        }
    ) { padding ->
        if (showNewColumnDialog) {
            AlertDialog(
                onDismissRequest = { showNewColumnDialog = false },
                title = { Text(Strings.t("new_column")) },
                text = {
                    OutlinedTextField(
                        value = newColumnTitle,
                        onValueChange = { newColumnTitle = it },
                        label = { Text(Strings.t("title_column")) },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newColumnTitle.isNotBlank()) {
                                addColumn(boardModel, newColumnTitle) { updatedBoard ->
                                    boardModel = updatedBoard
                                    newColumnTitle = ""
                                    showNewColumnDialog = false
                                }
                            }
                        }
                    ) {
                        Text(Strings.t("add"))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showNewColumnDialog = false }) {
                        Text(Strings.t("cancel"))
                    }
                }
            )
        }

        if (isLoading || boardModel == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val columns = remember(boardModel) { boardModel?.columns?.toList() ?: emptyList() }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(24.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(padding)
            ) {
                items(columns, key = { it.id }) { column ->
                    ColumnView(
                        columnData = column,
                        onAddCard = { card -> addCard(column.id, card, boardModel) { boardModel = it } },
                        onDeleteColumn = { deleteColumn(column.id, boardModel) { boardModel = it } },
                        onUpdateCard = { updatedCard -> updateCard(column.id, updatedCard, boardModel) { boardModel = it } },
                        onUpdate = { saveBoard(boardModel) }
                    )
                }
            }
        }
    }
}

private fun createNewBoard(id: String) = BoardModel(
    id = id,
    title = "New Board",
    columns = mutableListOf(ColumnModel(UUID.randomUUID().toString(), "To Do", mutableListOf()))
)

private fun addColumn(boardModel: BoardModel?, newTitle: String, updateBoardData: (BoardModel) -> Unit) {
    boardModel?.let {
        val updatedColumns = it.columns.toMutableList().apply {
            add(ColumnModel(UUID.randomUUID().toString(), newTitle, mutableListOf()))
        }
        val updatedBoard = it.copy(columns = updatedColumns)
        saveBoard(updatedBoard)
        updateBoardData(updatedBoard)
    }
}

private fun addCard(columnId: String, card: CardModel, boardModel: BoardModel?, updateBoardData: (BoardModel) -> Unit) {
    boardModel?.let {
        val updatedColumns = it.columns.map { column ->
            if (column.id == columnId) {
                column.copy(cards = column.cards.toMutableList().apply {
                    add(card.copy(id = UUID.randomUUID().toString()))
                })
            } else column
        }
        val updatedBoard = it.copy(columns = updatedColumns.toMutableList())
        saveBoard(updatedBoard)
        updateBoardData(updatedBoard)
    }
}

private fun deleteColumn(columnId: String, boardModel: BoardModel?, updateBoardData: (BoardModel) -> Unit) {
    boardModel?.let {
        val updatedColumns = it.columns.filter { column -> column.id != columnId }
        val updatedBoard = it.copy(columns = updatedColumns.toMutableList())
        saveBoard(updatedBoard)
        updateBoardData(updatedBoard)
    }
}

private fun updateCard(columnId: String, updatedCard: CardModel, boardModel: BoardModel?, updateBoardData: (BoardModel) -> Unit) {
    boardModel?.let {
        val updatedColumns = it.columns.map { column ->
            if (column.id == columnId) {
                column.copy(cards = column.cards.map { card ->
                    if (card.id == updatedCard.id) updatedCard else card
                }.toMutableList())
            } else column
        }
        val updatedBoard = it.copy(columns = updatedColumns.toMutableList())
        saveBoard(updatedBoard)
        updateBoardData(updatedBoard)
    }
}

private fun saveBoard(boardModel: BoardModel?) {
    boardModel?.let {
        FileService.saveBoard(it)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardAppBar(title: String, onBack: () -> Unit, onSettings: () -> Unit) {
    TopAppBar(
        title = { Text(title, fontWeight = FontWeight.Bold) },
        navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = Strings.t("back")) }
        },
        actions = {
            IconButton(onClick = onSettings) { Icon(Icons.Default.Settings, contentDescription = Strings.t("settings")) }
        }
    )
}