package com.example.todo.utils

import com.example.todo.models.Task

object QuadrantUtils {

    // Enum to represent the four quadrants
    enum class Quadrant {
        DO_FIRST,   // Urgent and Important
        SCHEDULE,   // Not Urgent but Important
        DELEGATE,   // Urgent but Not Important
        ELIMINATE   // Not Urgent and Not Important
    }

    /**
     * Groups a list of tasks into their respective quadrants based on priority.
     * This is a simplified logic. A real implementation might use more properties
     * like 'isUrgent' or 'isImportant'.
     *
     * Priority Mapping:
     * 1 -> DO_FIRST
     * 2 -> SCHEDULE
     * 3 -> DELEGATE
     * 4 -> ELIMINATE
     */
    fun getTasksGroupedByQuadrant(tasks: List<Task>): Map<Quadrant, List<Task>> {
        // Use the groupBy function to sort tasks based on their priority.
        return tasks.groupBy { task ->
            when (task.priority) {
                1 -> Quadrant.DO_FIRST
                2 -> Quadrant.SCHEDULE
                3 -> Quadrant.DELEGATE
                else -> Quadrant.ELIMINATE // Default for priority 4 or any other value
            }
        }
    }
}
