package com.example.todoapp.ui

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.todoapp.model.EisenhowerQuadrant
import com.example.todoapp.model.QuadrantState
import com.example.todoapp.ui.components.TodoCard
import com.example.todoapp.viewmodel.TodoViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EisenhowerMatrixScreen(
    viewModel: TodoViewModel = viewModel(factory = TodoViewModel.Factory(LocalContext.current))
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddTodoDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var showSearchBar by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Todo Eisenhower") },
                actions = {
                    // Search Icon
                    IconButton(onClick = { showSearchBar = !showSearchBar }) {
                        Icon(
                            if (showSearchBar) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = if (showSearchBar) "Close search" else "Search"
                        )
                    }
                    // Filter Icon
                    IconButton(onClick = { viewModel.toggleShowCompleted() }) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = "Filter completed"
                        )
                    }
                    // Clear completed
                    IconButton(onClick = { viewModel.clearCompletedTodos() }) {
                        Icon(Icons.Default.Clear, "Clear completed")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddTodoDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Todo")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                // Search Bar
                AnimatedVisibility(
                    visible = showSearchBar,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    SearchBar(
                        query = viewModel.searchQuery,
                        onQueryChange = viewModel::setSearchQuery,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                }

                // Upcoming Tasks Section
                AnimatedVisibility(
                    visible = !showSearchBar,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    val upcomingTodos by viewModel.getUpcomingTodos()
                        .collectAsState(initial = emptyList())

                    if (upcomingTodos.isNotEmpty()) {
                        UpcomingTasksSection(
                            todos = upcomingTodos,
                            onToggleCompletion = viewModel::toggleTodoCompletion,
                            onDelete = viewModel::deleteTodo,
                            onMoveToQuadrant = viewModel::moveTodoToQuadrant,
                            onUpdateDueDate = { id, date -> 
                                viewModel.updateTodo(
                                    upcomingTodos.find { it.id == id }?.copy(dueDate = date) 
                                        ?: return@UpcomingTasksSection
                                )
                            },
                            onUpdatePriority = { id, priority ->
                                viewModel.updateTodo(
                                    upcomingTodos.find { it.id == id }?.copy(priority = priority)
                                        ?: return@UpcomingTasksSection
                                )
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
                // Matrix Grid
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    // First Column (Urgent)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp)
                    ) {
                        QuadrantSection(
                            quadrantState = uiState.quadrants.find { it.quadrant == EisenhowerQuadrant.URGENT_IMPORTANT },
                            onToggleCompletion = viewModel::toggleTodoCompletion,
                            onDelete = viewModel::deleteTodo,
                            onMoveToQuadrant = viewModel::moveTodoToQuadrant,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        QuadrantSection(
                            quadrantState = uiState.quadrants.find { it.quadrant == EisenhowerQuadrant.URGENT_NOT_IMPORTANT },
                            onToggleCompletion = viewModel::toggleTodoCompletion,
                            onDelete = viewModel::deleteTodo,
                            onMoveToQuadrant = viewModel::moveTodoToQuadrant,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Second Column (Not Urgent)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp)
                    ) {
                        QuadrantSection(
                            quadrantState = uiState.quadrants.find { it.quadrant == EisenhowerQuadrant.NOT_URGENT_IMPORTANT },
                            onToggleCompletion = viewModel::toggleTodoCompletion,
                            onDelete = viewModel::deleteTodo,
                            onMoveToQuadrant = viewModel::moveTodoToQuadrant,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        QuadrantSection(
                            quadrantState = uiState.quadrants.find { it.quadrant == EisenhowerQuadrant.NOT_URGENT_NOT_IMPORTANT },
                            onToggleCompletion = viewModel::toggleTodoCompletion,
                            onDelete = viewModel::deleteTodo,
                            onMoveToQuadrant = viewModel::moveTodoToQuadrant,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Add Todo Dialog
            if (showAddTodoDialog) {
                AddTodoDialog(
                    onDismiss = { showAddTodoDialog = false },
                    onAddTodo = { title, description, quadrant, dueDate, priority ->
                        viewModel.addTodo(title, description, quadrant, dueDate, priority)
                        showAddTodoDialog = false
                    }
                )
            }

            // Error Handling with custom Snackbar
            uiState.error?.let { error ->
                LaunchedEffect(error) {
                    scope.launch {
                        val result = snackbarHostState.showSnackbar(
                            message = error,
                            actionLabel = "Dismiss",
                            duration = SnackbarDuration.Long,
                            withDismissAction = true
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            viewModel.clearError()
                        }
                    }
                }
            }

            // Error Handling
            LaunchedEffect(uiState.error) {
                uiState.error?.let { error ->
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = error,
                            duration = SnackbarDuration.Short
                        )
                        viewModel.clearError()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuadrantSection(
    quadrantState: QuadrantState?,
    onToggleCompletion: (String) -> Unit,
    onDelete: (String) -> Unit,
    onMoveToQuadrant: (String, EisenhowerQuadrant) -> Unit,
    modifier: Modifier = Modifier
) {
    if (quadrantState == null) return

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Text(
                text = quadrantState.title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                textAlign = TextAlign.Center
            )

            if (quadrantState.todos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No tasks here",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = quadrantState.todos,
                        key = { it.id }
                    ) { todo ->
                        TodoCard(
                            todo = todo,
                            onToggleCompletion = onToggleCompletion,
                            onDelete = onDelete,
                            onMoveToQuadrant = onMoveToQuadrant
                        )
                    }
                }
            }
        }
    }
}

@Composable
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text("Search todos...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
        trailingIcon = if (query.isNotEmpty()) {
            {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear search")
                }
            }
        } else null,
        singleLine = true,
        shape = MaterialTheme.shapes.medium
    )
}

@Composable
private fun UpcomingTasksSection(
    todos: List<Todo>,
    onToggleCompletion: (String) -> Unit,
    onDelete: (String) -> Unit,
    onMoveToQuadrant: (String, EisenhowerQuadrant) -> Unit,
    onUpdateDueDate: (String, Long?) -> Unit,
    onUpdatePriority: (String, Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Upcoming Tasks",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            todos.forEach { todo ->
                TodoCard(
                    todo = todo,
                    onToggleCompletion = onToggleCompletion,
                    onDelete = onDelete,
                    onMoveToQuadrant = onMoveToQuadrant,
                    onUpdateDueDate = onUpdateDueDate,
                    onUpdatePriority = onUpdatePriority,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private fun AddTodoDialog(
    onDismiss: () -> Unit,
    onAddTodo: (String, String, EisenhowerQuadrant, Long?, Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedQuadrant by remember { mutableStateOf(EisenhowerQuadrant.URGENT_IMPORTANT) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Todo") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                // Priority Selection
                Text("Priority:", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FilterChip(
                        selected = selectedPriority == 1,
                        onClick = { selectedPriority = 1 },
                        label = { Text("Low") }
                    )
                    FilterChip(
                        selected = selectedPriority == 2,
                        onClick = { selectedPriority = 2 },
                        label = { Text("Medium") }
                    )
                    FilterChip(
                        selected = selectedPriority == 3,
                        onClick = { selectedPriority = 3 },
                        label = { Text("High") }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Due Date Selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Due Date:", style = MaterialTheme.typography.labelLarge)
                    TextButton(onClick = { showDatePicker = true }) {
                        Text(
                            selectedDate?.let { formatDate(it) } ?: "Set Due Date"
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Quadrant Selection
                Text("Quadrant:", style = MaterialTheme.typography.labelLarge)
                EisenhowerQuadrant.values().forEach { quadrant ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedQuadrant == quadrant,
                            onClick = { selectedQuadrant = quadrant }
                        )
                        Text(
                            text = when (quadrant) {
                                EisenhowerQuadrant.URGENT_IMPORTANT -> "Do First"
                                EisenhowerQuadrant.NOT_URGENT_IMPORTANT -> "Schedule"
                                EisenhowerQuadrant.URGENT_NOT_IMPORTANT -> "Delegate"
                                EisenhowerQuadrant.NOT_URGENT_NOT_IMPORTANT -> "Eliminate"
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank()) {
                        onAddTodo(title, description, selectedQuadrant, selectedDate, selectedPriority)
                    }
                }
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