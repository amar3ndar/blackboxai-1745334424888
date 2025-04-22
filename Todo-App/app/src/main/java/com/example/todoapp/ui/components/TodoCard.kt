package com.example.todoapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.todoapp.model.EisenhowerQuadrant
import com.example.todoapp.model.Todo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoCard(
    todo: Todo,
    onToggleCompletion: (String) -> Unit,
    onDelete: (String) -> Unit,
    onMoveToQuadrant: (String, EisenhowerQuadrant) -> Unit,
    onUpdateDueDate: (String, Long?) -> Unit,
    onUpdatePriority: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(if (todo.isCompleted) 0.6f else 1f)

    var showDatePicker by remember { mutableStateOf(false) }
    var showPriorityDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(alpha)
            .swipeToDelete { onDelete(todo.id) },
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = when (todo.priority) {
                3 -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                1 -> MaterialTheme.colorScheme.surfaceVariant
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Checkbox(
                        checked = todo.isCompleted,
                        onCheckedChange = { onToggleCompletion(todo.id) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = todo.title,
                        style = MaterialTheme.typography.titleMedium,
                        textDecoration = if (todo.isCompleted) TextDecoration.LineThrough else null,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More options")
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Set Due Date") },
                                onClick = {
                                    showDatePicker = true
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.DateRange,
                                        contentDescription = "Set due date"
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Set Priority") },
                                onClick = {
                                    showPriorityDialog = true
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.PriorityHigh,
                                        contentDescription = "Set priority"
                                    )
                                }
                            )
                        EisenhowerQuadrant.values().forEach { quadrant ->
                            if (quadrant != todo.quadrant) {
                                DropdownMenuItem(
                                    text = { Text(getQuadrantTitle(quadrant)) },
                                    onClick = {
                                        onMoveToQuadrant(todo.id, quadrant)
                                        showMenu = false
                                    }
                                )
                            }
                        }
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                onDelete(todo.id)
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete todo"
                                )
                            }
                        )
                    }
                }
            }

                // Description
                AnimatedVisibility(
                    visible = todo.description.isNotBlank(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Text(
                        text = todo.description,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 44.dp, top = 4.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Due Date and Priority
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 44.dp, top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Due Date
                    todo.dueDate?.let { dueDate ->
                        AssistChip(
                            onClick = { showDatePicker = true },
                            label = { Text(formatDate(dueDate)) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.DateRange,
                                    contentDescription = "Due date"
                                )
                            }
                        )
                    }

                    // Priority
                    AssistChip(
                        onClick = { showPriorityDialog = true },
                        label = { Text(getPriorityText(todo.priority)) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.PriorityHigh,
                                contentDescription = "Priority"
                            )
                        }
                    )
                }
            }

            // Date Picker Dialog
            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    onDateSelected = { timestamp ->
                        onUpdateDueDate(todo.id, timestamp)
                        showDatePicker = false
                    },
                    initialDate = todo.dueDate
                )
            }

            // Priority Dialog
            if (showPriorityDialog) {
                PriorityDialog(
                    currentPriority = todo.priority,
                    onDismiss = { showPriorityDialog = false },
                    onPrioritySelected = { priority ->
                        onUpdatePriority(todo.id, priority)
                        showPriorityDialog = false
                    }
                )
            }
        }
    }
}

@Composable
private fun DatePickerDialog(
    onDismissRequest: () -> Unit,
    onDateSelected: (Long?) -> Unit,
    initialDate: Long? = null
) {
    val calendar = Calendar.getInstance()
    initialDate?.let { calendar.timeInMillis = it }

    DatePickerDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = {
                onDateSelected(calendar.timeInMillis)
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onDateSelected(null)
                onDismissRequest()
            }) {
                Text("Clear")
            }
        }
    ) {
        DatePicker(
            state = rememberDatePickerState(
                initialSelectedDateMillis = initialDate
            )
        )
    }
}

@Composable
private fun PriorityDialog(
    currentPriority: Int,
    onDismiss: () -> Unit,
    onPrioritySelected: (Int) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Priority") },
        text = {
            Column {
                RadioButton(
                    selected = currentPriority == 3,
                    onClick = { onPrioritySelected(3) },
                    label = "High"
                )
                RadioButton(
                    selected = currentPriority == 2,
                    onClick = { onPrioritySelected(2) },
                    label = "Medium"
                )
                RadioButton(
                    selected = currentPriority == 1,
                    onClick = { onPrioritySelected(1) },
                    label = "Low"
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun RadioButton(
    selected: Boolean,
    onClick: () -> Unit,
    label: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(label)
    }
}

private fun formatDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

private fun getPriorityText(priority: Int): String {
    return when (priority) {
        3 -> "High"
        1 -> "Low"
        else -> "Medium"
    }
}

private fun Modifier.swipeToDelete(
    onDelete: () -> Unit
): Modifier = composed {
    val dismissState = rememberDismissState(
        confirmValueChange = { value ->
            if (value == DismissValue.DismissedToEnd) {
                onDelete()
                true
            } else false
        }
    )

    SwipeToDismiss(
        state = dismissState,
        background = {
            val color = MaterialTheme.colorScheme.error
            val scale = LocalDensity.current
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onError
                )
            }
        },
        dismissContent = { this }
    )
}
