package com.example.todoapp.repository

import com.example.todoapp.data.TodoDao
import com.example.todoapp.data.TodoEntity
import com.example.todoapp.model.EisenhowerQuadrant
import com.example.todoapp.model.Todo
import com.example.todoapp.model.TodoError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TodoRepositoryImpl(
    private val todoDao: TodoDao
) : TodoRepository {
    override val todos: Flow<List<Todo>> = todoDao.getAllTodos()
        .map { entities -> entities.map { it.toTodo() } }

    override fun getTodosByQuadrant(quadrant: EisenhowerQuadrant): Flow<List<Todo>> {
        return todoDao.getTodosByQuadrant(quadrant)
            .map { entities -> entities.map { it.toTodo() } }
    }

    override suspend fun addTodo(
        title: String,
        description: String,
        quadrant: EisenhowerQuadrant,
        dueDate: Long?,
        priority: Int
    ): Result<Todo> {
        return try {
            val todo = Todo.createTodo(
                title = title,
                description = description,
                quadrant = quadrant,
                dueDate = dueDate,
                priority = priority
            )
            todoDao.insertTodo(TodoEntity.fromTodo(todo))
            Result.success(todo)
        } catch (e: IllegalArgumentException) {
            Result.failure(TodoError.EmptyTitle())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateTodo(todo: Todo): Result<Todo> {
        return try {
            todoDao.updateTodo(TodoEntity.fromTodo(todo))
            Result.success(todo)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleTodoCompletion(todoId: String): Result<Todo> {
        return try {
            val todoEntity = todoDao.getTodoById(todoId)
                ?: return Result.failure(TodoError.TodoNotFound())
            
            val updatedTodo = todoEntity.toTodo().copy(isCompleted = !todoEntity.isCompleted)
            todoDao.updateTodo(TodoEntity.fromTodo(updatedTodo))
            Result.success(updatedTodo)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun moveTodoToQuadrant(todoId: String, newQuadrant: EisenhowerQuadrant): Result<Todo> {
        return try {
            val todoEntity = todoDao.getTodoById(todoId)
                ?: return Result.failure(TodoError.TodoNotFound())
            
            val updatedTodo = todoEntity.toTodo().copy(quadrant = newQuadrant)
            todoDao.updateTodo(TodoEntity.fromTodo(updatedTodo))
            Result.success(updatedTodo)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteTodo(todoId: String): Result<Unit> {
        return try {
            val todoEntity = todoDao.getTodoById(todoId)
                ?: return Result.failure(TodoError.TodoNotFound())
            
            todoDao.deleteTodo(todoEntity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearCompletedTodos(): Result<Unit> {
        return try {
            todoDao.deleteCompletedTodos()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun searchTodos(query: String): Flow<List<Todo>> {
        return todoDao.searchTodos(query)
            .map { entities -> entities.map { it.toTodo() } }
    }

    override fun getUpcomingTodos(): Flow<List<Todo>> {
        return todoDao.getUpcomingTodos(System.currentTimeMillis())
            .map { entities -> entities.map { it.toTodo() } }
    }

    companion object {
        @Volatile
        private var INSTANCE: TodoRepository? = null

        fun getInstance(todoDao: TodoDao): TodoRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TodoRepositoryImpl(todoDao).also { INSTANCE = it }
            }
        }
    }
}
