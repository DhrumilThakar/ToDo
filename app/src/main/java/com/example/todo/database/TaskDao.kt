package com.example.todo.database

import androidx.room.*
import com.example.todo.models.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks ORDER BY priority ASC")
    fun getAllTasks(): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task): Long

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: Long)

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Long): Task?


    @Query("UPDATE tasks SET isCompleted = :isCompleted WHERE id = :taskId")
    suspend fun updateTaskCompletionStatus(taskId: Long, isCompleted: Boolean)

    @Query("SELECT * FROM tasks WHERE isMainTask = 1 LIMIT 1")
    suspend fun getMainTask(): Task?

    @Query("SELECT * FROM tasks WHERE isSubtask = 1 ORDER BY id ASC")
    fun getSubtasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE isMainTask = 0 AND isSubtask = 0 ORDER BY priority ASC")
    fun getDynamicTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE isCompleted = 1")
    suspend fun getAllCompletedTasks(): List<Task>

    @Query("DELETE FROM tasks WHERE isCompleted = 1 AND dueDate IS NOT NULL AND dueDate < :beforeDate")
    suspend fun deleteCompletedTasksBeforeDate(beforeDate: Long)
}
