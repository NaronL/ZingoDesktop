package ru.igrakov.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import ru.igrakov.models.*
import ru.igrakov.services.FileService
import ru.igrakov.ui.screens.element.WorkspaceCard
import ru.igrakov.utils.Router
import ru.igrakov.utils.Strings
import java.time.LocalDate
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun WorkspacesScreen() {
    var workspaces by remember { mutableStateOf(emptyList<WorkspaceModel>()) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var newWorkspaceTitle by remember { mutableStateOf("") }



    val fabScale by animateFloatAsState(
        targetValue = if (showCreateDialog) 0.8f else 1f,
        animationSpec = spring(dampingRatio = 0.6f)
    )

    LaunchedEffect(Unit) {
        workspaces = runCatching { FileService.loadWorkspaces() }
            .getOrElse { emptyList() }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = Strings.t("workspaces"),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.alpha(0.9f)
                    )
                }, // закрывающая скобка была пропущена
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                ),
                actions = {
                    IconButton(
                        onClick = { Router.navigate(Router.Screen.Settings) },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = Strings.t("settings"),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                modifier = Modifier.shadow(4.dp)
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = !showCreateDialog,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    modifier = Modifier.scale(fabScale),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 12.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add, // исправил, так как ты подключаешь Material Icons
                        contentDescription = "Create workspace",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    ) { padding ->
        AnimatedContent(
            targetState = workspaces.isEmpty(),
            transitionSpec = {
                fadeIn() with fadeOut() using SizeTransform(clip = false)
            },
            modifier = Modifier.padding(padding)
        ) { isEmpty ->
            if (isEmpty) {
                EmptyState()
            } else {
                WorkspaceList(
                    workspaces = workspaces,
                    onOpen = { workspaceId ->
                        Router.selectedWorkspaceId = workspaceId
                        Router.navigate(Router.Screen.Board, workspaceId)
                    },
                    onDelete = { id ->
                        workspaces = workspaces.filterNot { it.id == id }
                        FileService.deleteWorkspace(id)
                    },
                    onEdit = { id, newTitle ->
                        workspaces = workspaces.map {
                            if (it.id == id) it.copy(title = newTitle).also { updated ->
                                FileService.saveWorkspace(updated)
                            } else it
                        }
                    }
                )
            }
        }
    }

    if (showCreateDialog) {
        CreateWorkspaceDialog(
            title = newWorkspaceTitle,
            onTitleChange = { newWorkspaceTitle = it },
            onDismiss = { showCreateDialog = false },
            onCreate = {
                val newWorkspace = WorkspaceModel(
                    id = UUID.randomUUID().toString(),
                    title = newWorkspaceTitle,
                    columns = mutableListOf(),
                    createDate = LocalDate.now(),
                )
                workspaces = workspaces + newWorkspace
                FileService.saveWorkspace(newWorkspace)

                val initialBoard = BoardModel(
                    id = newWorkspace.id,
                    title = newWorkspace.title,
                    columns = listOf("Сделать", "В прогрессе", "Готово").map {
                        ColumnModel(id = UUID.randomUUID().toString(), title = it, cards = mutableListOf())
                    }.toMutableList()
                )
                FileService.saveBoard(initialBoard)

                Router.selectedWorkspaceId = newWorkspace.id
                Router.navigate(Router.Screen.Board, newWorkspace.id) // Добавил workspaceId

                newWorkspaceTitle = ""
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = Strings.t("no_workspaces"),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant, // завершил строку
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun WorkspaceList(
    workspaces: List<WorkspaceModel>,
    onOpen: (String) -> Unit,
    onDelete: (String) -> Unit,
    onEdit: (String, String) -> Unit,
) {
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        items(workspaces, key = { it.id }) { workspace ->
            key(workspace.id) {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    WorkspaceCard(
                        workspace = workspace,
                        onOpen = { onOpen(workspace.id) },
                        onDelete = { onDelete(workspace.id) },
                        onEdit = { newTitle -> onEdit(workspace.id, newTitle) },
                    )
                }
            }
        }
    }
}

@Composable
private fun CreateWorkspaceDialog(
    title: String,
    onTitleChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onCreate: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        var textFieldFocus by remember { mutableStateOf(true) }

        LaunchedEffect(Unit) {
            textFieldFocus = true
        }

        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 24.dp,
            modifier = Modifier
                .widthIn(min = 280.dp, max = 560.dp)
                .padding(vertical = 24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    Strings.t("new_workspace"),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                )

                val focusRequester = remember { FocusRequester() }

                LaunchedEffect(textFieldFocus) {
                    if (textFieldFocus) {
                        focusRequester.requestFocus()
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    label = { Text(Strings.t("title_workspace")) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { if (title.isNotBlank()) onCreate() }
                    ),
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text(Strings.t("cancel").uppercase())
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onCreate,
                        enabled = title.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 0.dp
                        )
                    ) {
                        Text(Strings.t("create").uppercase())
                    }
                }
            }
        }
    }
}
