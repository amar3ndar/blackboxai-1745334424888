package com.example.todoapp.data

import androidx.room.*
import com.example.todoapp.model.EisenhowerQuadrant
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {
    @Query("SELECT * FROM todos ORDER BY createdAt DESC")
    fun getAllTodos(): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todos WHERE quadrant = :quadrant ORDER BY createdAt DESC")
    fun getTodosByQuadrant(quadrant: EisenhowerQuadrant): Flow<List<TodoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodo(todo: TodoEntity)

    @Update
    suspend fun updateTodo(todo: TodoEntity)

    @Delete
    suspend fun deleteTodo(todo: TodoEntity)

    @Query("DELETE FROM todos WHERE isCompleted = 1")
    suspend fun deleteCompletedTodos()

    @Query("SELECT * FROM todos WHERE id = :todoId")
    suspend fun getTodoById(todoId: String): TodoEntity?

    @Query("SELECT * FROM todos WHERE title LIKE '%' || :searchQuery || '%' OR description LIKE '%' || :searchQuery || '%'")
    fun searchTodos(searchQuery: String): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todos WHERE dueDate IS NOT NULL AND dueDate > :now ORDER BY dueDate ASC")
    fun getUpcomingTodos(now: Long): Flow<List<TodoEntity>>
}
