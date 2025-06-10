package ru.igrakov.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
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
 * Экран настроек приложения.
 * Позволяет пользователю изменять путь к папке, переключать тему (светлая/тёмная) и выбирать язык интерфейса.
 * Настройки сохраняются локально и применяются немедленно.
 * Также предоставляет кнопку выхода из аккаунта.
 *
 * @param onSave Коллбек, вызываемый при сохранении настроек (путь к папке, тема, язык).
 * @param onLogout Коллбек для выхода из аккаунта.
 * @param currentFolderPath Текущий путь к папке (начальное значение).
 * @param currentTheme Текущая тема (true — тёмная, false — светлая).
 * @param currentLocale Текущий выбранный язык интерфейса.
 * @param modifier Модификатор для стилизации компонента.
 * @author Andrey Igrakov
 *
*/
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BoardScreen() {

    // Идентификатор выбранного рабочего пространства
    val workspaceId = Router.selectedWorkspaceId

    // Флаги состояния
    var showNewColumnDialog by remember { mutableStateOf(false) }
    var boardModel by remember { mutableStateOf<BoardModel?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    // Название новой колонки
    var newColumnTitle by remember { mutableStateOf("") }

    var draggedCard by remember { mutableStateOf<CardModel?>(null) }
    var sourceColumnId by remember { mutableStateOf<String?>(null) }

    fun updateBoard(newBoard: BoardModel?) {
        boardModel = newBoard
        saveBoard(newBoard)
    }

    // Загрузка данных доски при изменении рабочего пространства
    LaunchedEffect(workspaceId) {
        workspaceId?.let {
            // Загружаем доску или создаём новую, если её нет
            boardModel = FileService.loadBoard(it) ?: createNewBoard(it).also(FileService::saveBoard)
        }
        isLoading = false
    }

    // Если рабочее пространство не выбрано, возвращаемся на экран выбора
    if (workspaceId == null && !isLoading) {
        LaunchedEffect(Unit) { Router.navigate(Router.Screen.Workspaces) }
        return
    }

    // Основной контейнер экрана с верхней панелью и кнопкой добавления колонки
    Scaffold(
        topBar = {
            BoardAppBar(
                title = boardModel?.title.orEmpty(),
                onBack = { Router.navigate(Router.Screen.Workspaces) },
                onSettings = { Router.navigate(Router.Screen.Settings) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showNewColumnDialog = true }) { Text("+") }
        }
    ) { padding ->

        // Диалог добавления новой колонки
        if (showNewColumnDialog) {
            AlertDialog(
                onDismissRequest = { showNewColumnDialog = false },
                title = { Text(Strings.t("new_column")) },
                text = {
                    OutlinedTextField(
                        value = newColumnTitle,
                        onValueChange = { newColumnTitle = it },
                        label = { Text(Strings.t("title_column")) },
                        modifier = Modifier.fillMaxWidth(),
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
                    val isDropTarget = remember(draggedCard, sourceColumnId) {
                        draggedCard != null && sourceColumnId != column.id
                    }


                    ColumnView(
                        columnData = column,
                        onAddCard = { card ->
                            addCard(column.id, card, boardModel) { updatedBoard ->
                                updateBoard(updatedBoard)
                            }
                        },
                        onDeleteColumn = {
                            deleteColumn(column.id, boardModel) { updatedBoard ->
                                updateBoard(updatedBoard)
                            }
                        },
                        onDeleteCard = { cardId ->
                            val updatedColumns = boardModel?.columns?.map {
                                if (it.id == column.id) it.copy(cards = it.cards.filter { c -> c.id != cardId })
                                else it
                            } ?: emptyList()
                            updateBoard(boardModel?.copy(columns = updatedColumns.toMutableList()))
                        },
                        onUpdateCard = { updatedCard ->
                            updateCard(column.id, updatedCard, boardModel) { updatedBoard ->
                                updateBoard(updatedBoard)
                            }
                        },
                        onUpdate = { saveBoard(boardModel) },
                        onMoveCard = { card, targetColumnId ->
                            if (sourceColumnId != null && sourceColumnId != targetColumnId) {
                                moveCard(
                                    boardModel,
                                    card.id,
                                    sourceColumnId!!,
                                    targetColumnId
                                ) { updatedBoard ->
                                    updateBoard(updatedBoard)
                                }
                            }
                        },
                        isDropTarget = isDropTarget,
                        onDragStart = { card ->
                            draggedCard = card
                            sourceColumnId = column.id
                        },
                        onDragEnd = {
                            draggedCard = null
                            sourceColumnId = null
                        },
                        modifier = Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null)
                    )
                    }
            }
        }
    }
}

/**
 * Создаёт новую доску с одной колонкой "To Do" по умолчанию.
 */
private fun createNewBoard(id: String) = BoardModel(
    id = id,
    title = "New Board",
    columns = mutableListOf(ColumnModel(UUID.randomUUID().toString(), "To Do", mutableListOf()))
)

/**
 * Добавляет новую колонку в доску.
 */
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

/**
 * Добавляет карточку в указанную колонку.
 */
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

/**
 * Удаляет колонку по её ID.
 */
private fun deleteColumn(columnId: String, boardModel: BoardModel?, updateBoardData: (BoardModel) -> Unit) {
    boardModel?.let {
        val updatedColumns = it.columns.filter { column -> column.id != columnId }
        val updatedBoard = it.copy(columns = updatedColumns.toMutableList())
        saveBoard(updatedBoard)
        updateBoardData(updatedBoard)
    }
}

/**
 * Обновляет карточку в указанной колонке.
 */
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

/**
 * Сохраняет доску в файл.
 */
private fun saveBoard(boardModel: BoardModel?) {
    boardModel?.let {
        FileService.saveBoard(it)
    }
}

private fun moveCard(
    boardModel: BoardModel?,
    cardId: String,
    sourceColumnId: String,
    targetColumnId: String,
    updateBoardData: (BoardModel) -> Unit
) {
    boardModel?.let { board ->
        val sourceColumn = board.columns.firstOrNull { it.id == sourceColumnId }
        val card = sourceColumn?.cards?.firstOrNull { it.id == cardId } ?: return@let

        val updatedColumns = board.columns.map { column ->
            when (column.id) {
                sourceColumnId -> column.copy(
                    cards = column.cards.filter { it.id != cardId }.toMutableList()
                )
                targetColumnId -> column.copy(
                    cards = (column.cards + card).toMutableList()
                )
                else -> column
            }
        }
        updateBoardData(board.copy(columns = updatedColumns.toMutableList()))
    }
}


/**
 * Верхняя панель доски с кнопкой "назад" и кнопкой настроек.
 */
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
