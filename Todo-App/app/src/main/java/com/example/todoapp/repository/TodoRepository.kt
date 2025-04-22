package com.example.todoapp.repository

import com.example.todoapp.model.EisenhowerQuadrant
import com.example.todoapp.model.Todo
import com.example.todoapp.model.TodoError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * Repository interface that handles all Todo-related data operations
 */
interface TodoRepository {
    val todos: Flow<List<Todo>>

    fun getTodosByQuadrant(quadrant: EisenhowerQuadrant): Flow<List<Todo>>

    suspend fun addTodo(
        title: String,
        description: String,
        quadrant: EisenhowerQuadrant,
        dueDate: Long? = null,
        priority: Int = 2
    ): Result<Todo>

    suspend fun updateTodo(todo: Todo): Result<Todo>

    suspend fun toggleTodoCompletion(todoId: String): Result<Todo>

    suspend fun moveTodoToQuadrant(todoId: String, newQuadrant: EisenhowerQuadrant): Result<Todo>

    suspend fun deleteTodo(todoId: String): Result<Unit>

    suspend fun clearCompletedTodos(): Result<Unit>

    fun searchTodos(query: String): Flow<List<Todo>>

    fun getUpcomingTodos(): Flow<List<Todo>>
}