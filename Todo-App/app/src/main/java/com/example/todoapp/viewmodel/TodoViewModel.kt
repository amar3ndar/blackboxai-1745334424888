package com.example.todoapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.todoapp.model.EisenhowerQuadrant
import com.example.todoapp.model.QuadrantState
import com.example.todoapp.model.Todo
import com.example.todoapp.model.TodoError
import com.example.todoapp.repository.TodoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Represents the UI state for the Todo application
 */
data class TodoUiState(
    val quadrants: List<QuadrantState> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel that handles the business logic and state management for the Todo application
 */
class TodoViewModel(
    private val repository: TodoRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedDueDate = MutableStateFlow<Long?>(null)
    private val _showCompleted = MutableStateFlow(true)

    val uiState: StateFlow<TodoUiState> = combine(
        _searchQuery,
        _showCompleted,
        EisenhowerQuadrant.values().map { quadrant ->
            repository.getTodosByQuadrant(quadrant).map { todos ->
                QuadrantState(
                    quadrant = quadrant,
                    title = getQuadrantTitle(quadrant),
                    todos = todos
                )
            }
        }
    ) { quadrantStates ->
        TodoUiState(
            quadrants = quadrantStates.toList(),
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TodoUiState(isLoading = true)
    )

    val searchQuery: String
        get() = _searchQuery.value

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedDueDate(timestamp: Long?) {
        _selectedDueDate.value = timestamp
    }

    fun toggleShowCompleted() {
        _showCompleted.value = !_showCompleted.value
    }

    /**
     * Add a new todo
     */
    fun addTodo(
        title: String,
        description: String,
        quadrant: EisenhowerQuadrant,
        dueDate: Long? = null,
        priority: Int = 2
    ) {
        viewModelScope.launch {
            repository.addTodo(title, description, quadrant, dueDate, priority)
                .onFailure { error ->
                    updateErrorState(error)
                }
        }
    }

    /**
     * Toggle todo completion status
     */
    fun toggleTodoCompletion(todoId: String) {
        viewModelScope.launch {
            repository.toggleTodoCompletion(todoId)
                .onFailure { error ->
                    updateErrorState(error)
                }
        }
    }

    /**
     * Move todo to a different quadrant
     */
    fun moveTodoToQuadrant(todoId: String, newQuadrant: EisenhowerQuadrant) {
        viewModelScope.launch {
            repository.moveTodoToQuadrant(todoId, newQuadrant)
                .onFailure { error ->
                    updateErrorState(error)
                }
        }
    }

    /**
     * Delete a todo
     */
    fun deleteTodo(todoId: String) {
        viewModelScope.launch {
            repository.deleteTodo(todoId)
                .onFailure { error ->
                    updateErrorState(error)
                }
        }
    }

    /**
     * Clear all completed todos
     */
    fun clearCompletedTodos() {
        viewModelScope.launch {
            repository.clearCompletedTodos()
                .onFailure { error ->
                    updateErrorState(error)
                }
        }
    }

    /**
     * Clear error state
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun updateErrorState(error: Throwable) {
        val errorMessage = when (error) {
            is TodoError.EmptyTitle -> "Title cannot be empty"
            is TodoError.TodoNotFound -> "Todo not found"
            is TodoError.InvalidQuadrant -> "Invalid quadrant"
            else -> "An unexpected error occurred"
        }
        _uiState.value = _uiState.value.copy(error = errorMessage)
    }

    private fun getQuadrantTitle(quadrant: EisenhowerQuadrant): String {
        return when (quadrant) {
            EisenhowerQuadrant.URGENT_IMPORTANT -> "Do First"
            EisenhowerQuadrant.NOT_URGENT_IMPORTANT -> "Schedule"
            EisenhowerQuadrant.URGENT_NOT_IMPORTANT -> "Delegate"
            EisenhowerQuadrant.NOT_URGENT_NOT_IMPORTANT -> "Eliminate"
        }
    }

    fun getUpcomingTodos(): Flow<List<Todo>> {
        return repository.getUpcomingTodos()
    }

    fun searchTodos(): Flow<List<Todo>> {
        return repository.searchTodos(_searchQuery.value)
    }

    class Factory(
        private val context: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TodoViewModel::class.java)) {
                val database = TodoDatabase.getDatabase(context)
                val repository = TodoRepositoryImpl.getInstance(database.todoDao())
                return TodoViewModel(
                    repository = repository,
                    savedStateHandle = SavedStateHandle()
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}