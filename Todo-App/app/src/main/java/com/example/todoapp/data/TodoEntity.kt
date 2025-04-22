package com.example.todoapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.example.todoapp.model.EisenhowerQuadrant
import com.example.todoapp.model.Todo
import java.util.Date

@Entity(tableName = "todos")
data class TodoEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val isCompleted: Boolean,
    val quadrant: EisenhowerQuadrant,
    val createdAt: Long,
    val dueDate: Long? = null,
    val priority: Int = 2 // 1: Low, 2: Medium, 3: High
) {
    fun toTodo(): Todo {
        return Todo(
            id = id,
            title = title,
            description = description,
            isCompleted = isCompleted,
            quadrant = quadrant,
            createdAt = createdAt,
            dueDate = dueDate,
            priority = priority
        )
    }

    companion object {
        fun fromTodo(todo: Todo): TodoEntity {
            return TodoEntity(
                id = todo.id,
                title = todo.title,
                description = todo.description,
                isCompleted = todo.isCompleted,
                quadrant = todo.quadrant,
                createdAt = todo.createdAt,
                dueDate = todo.dueDate,
                priority = todo.priority
            )
        }
    }
}

class Converters {
    @TypeConverter
    fun fromQuadrant(quadrant: EisenhowerQuadrant): String {
        return quadrant.name
    }

    @TypeConverter
    fun toQuadrant(value: String): EisenhowerQuadrant {
        return EisenhowerQuadrant.valueOf(value)
    }

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}
