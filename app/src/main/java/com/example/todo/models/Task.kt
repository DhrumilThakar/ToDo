package com.example.todo.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val isUrgent: Boolean,
    val isImportant: Boolean,
    val dueDate: Long?,
    val isCompleted: Boolean = false,
    val isMainTask: Boolean = false,
    val isSubtask: Boolean = false,
    val parentId: Long? = null,
    val priority: Int
)
