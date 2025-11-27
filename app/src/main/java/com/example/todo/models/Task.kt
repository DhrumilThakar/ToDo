package com.example.todo.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val isCompleted: Boolean = false,
    val isMainTask: Boolean = false,
    val isSubtask: Boolean = false,
    val isUrgent: Boolean = false,
    val isImportant: Boolean = false,
    val dueDate: Long? = null, // Store due date as timestamp
    val priority: Int = 4 // e.g., 1 for high, 2 for medium, 3 for low, 4 for eliminate (default)
)