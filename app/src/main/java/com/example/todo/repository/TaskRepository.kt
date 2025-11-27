package com.example.todo.repository

import android.content.Context
import com.example.todo.database.AppDatabase
import com.example.todo.database.TaskDao
import com.example.todo.models.Task
import kotlinx.coroutines.flow.Flow

class TaskRepository(context: Context) {

    private val taskDao: TaskDao

    init {
        val database = AppDatabase.getDatabase(context)
        taskDao = database.taskDao()
    }

    fun getAllTasks(): Flow<List<Task>> {
        return taskDao.getAllTasks()
    }

    suspend fun insertTask(task: Task): Long {
        return taskDao.insert(task)
    }

    suspend fun updateTask(task: Task) {
        taskDao.update(task)
    }

    suspend fun updateTaskCompletion(taskId: Long, isCompleted: Boolean) {
        taskDao.updateTaskCompletionStatus(taskId, isCompleted)
    }

    suspend fun deleteTask(task: Task) {
        taskDao.delete(task)
    }

    suspend fun deleteTaskById(taskId: Long) {
        taskDao.deleteTaskById(taskId)
    }

    suspend fun getTaskById(taskId: Long): Task? {
        return taskDao.getTaskById(taskId)
    }

    suspend fun getMainTask(): Task? {
        return taskDao.getMainTask()
    }

    fun getSubtasks(): Flow<List<Task>> {
        return taskDao.getSubtasks()
    }

    fun getDynamicTasks(): Flow<List<Task>> {
        return taskDao.getDynamicTasks()
    }

    suspend fun getAllCompletedTasks(): List<Task> {
        return taskDao.getAllCompletedTasks()
    }

    suspend fun deleteCompletedTasksBeforeDate(beforeDate: Long) {
        taskDao.deleteCompletedTasksBeforeDate(beforeDate)
    }
}
