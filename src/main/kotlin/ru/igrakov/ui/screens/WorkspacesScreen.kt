package ru.igrakov.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import ru.igrakov.models.BoardModel
import ru.igrakov.models.ColumnModel
import ru.igrakov.models.WorkspaceModel
import ru.igrakov.services.FileService
import ru.igrakov.ui.screens.element.WorkspaceCard
import ru.igrakov.utils.Router
import ru.igrakov.utils.Strings
import java.time.LocalDate
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = MaterialTheme.colorScheme.background
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = Strings.t("workspaces"),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    },

                    modifier = Modifier
                        .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                        .shadow(8.dp, RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
                        .border(
                            0.5.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                        ), // Легкая граница
                    actions = {
                        IconButton(
                            onClick = { Router.navigate(Router.Screen.Settings) },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = Strings.t("settings"),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                AnimatedVisibility(
                    visible = !showCreateDialog,
                    enter = fadeIn() + scaleIn(animationSpec = spring(dampingRatio = 0.6f)),
                    exit = fadeOut() + scaleOut()
                ) {
                    FloatingActionButton(
                        onClick = { showCreateDialog = true },
                        modifier = Modifier.scale(fabScale),
                        containerColor = MaterialTheme.colorScheme.primary,
                        elevation = FloatingActionButtonDefaults.elevation(
                            defaultElevation = 6.dp,
                            pressedElevation = 12.dp,
                            hoveredElevation = 8.dp
                        ),
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Create workspace",
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        ) { padding ->
            AnimatedContent(
                targetState = workspaces.isEmpty(),
                transitionSpec = {
                    (fadeIn() + expandVertically()).togetherWith(fadeOut() + shrinkVertically())
                },
                modifier = Modifier.padding(padding)
            ) { isEmpty ->
                if (isEmpty) {
                    EmptyState(onCreateClick = { showCreateDialog = true })
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
                Router.navigate(Router.Screen.Board, newWorkspace.id)

                newWorkspaceTitle = ""
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun EmptyState(onCreateClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = CircleShape
                )
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    shape = CircleShape
                )
                .clickable(onClick = onCreateClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = Strings.t("no_workspaces"),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
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
        verticalArrangement = Arrangement.spacedBy(12.dp),
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
                        modifier = Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null)
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
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    Strings.t("new_workspace"),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )

                val focusRequester = remember { FocusRequester() }

                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
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
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(12.dp)
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
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(Strings.t("cancel"))
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
                            defaultElevation = 2.dp,
                            pressedElevation = 8.dp
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(Strings.t("create"))
                    }
                }
            }
        }
    }
}
