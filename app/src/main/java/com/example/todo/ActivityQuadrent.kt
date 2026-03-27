package com.example.todo

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.todo.models.Task
import com.example.todo.repository.TaskRepository
import kotlinx.coroutines.launch

class ActivityQuadrent : AppCompatActivity() {

    private lateinit var taskRepository: TaskRepository
    
    // Quadrant containers
    private lateinit var doFirstContainer: LinearLayout
    private lateinit var scheduleContainer: LinearLayout
    private lateinit var delegateContainer: LinearLayout
    private lateinit var eliminateContainer: LinearLayout
    
    // Navigation buttons
    private lateinit var homeButton: LinearLayout
    private lateinit var quadrantButton: LinearLayout
    private lateinit var remindersButton: LinearLayout
    private lateinit var statsButton: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quadrent)

        taskRepository = TaskRepository(this)

        // Initialize quadrant containers
        doFirstContainer = findViewById(R.id.do_first_container)
        scheduleContainer = findViewById(R.id.schedule_container)
        delegateContainer = findViewById(R.id.delegate_container)
        eliminateContainer = findViewById(R.id.eliminate_container)

        // Initialize navigation buttons
        homeButton = findViewById(R.id.homeBtn)
        quadrantButton = findViewById(R.id.viewPriorityQuadrantBtn)
        remindersButton = findViewById(R.id.remindersBtn)
        statsButton = findViewById(R.id.statsBtn)

        setupNavigation()
        loadTasksIntoQuadrants()
    }

    private fun setupNavigation() {
        homeButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()
        }

        quadrantButton.setOnClickListener { /* Already here */ }
        
        remindersButton.setOnClickListener { startNoAnimActivity(RemindersActivity::class.java) }
        
        statsButton.setOnClickListener { startNoAnimActivity(StatsActivity::class.java) }
    }

    private fun startNoAnimActivity(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        startActivity(intent)
        overridePendingTransition(0, 0)
        finish()
    }

    private fun loadTasksIntoQuadrants() {
        lifecycleScope.launch {
            taskRepository.getAllTasks().collect { tasks ->
                // Clear all containers
                doFirstContainer.removeAllViews()
                scheduleContainer.removeAllViews()
                delegateContainer.removeAllViews()
                eliminateContainer.removeAllViews()

                tasks.forEach { task ->
                    val container = when (task.priority) {
                        1 -> doFirstContainer
                        2 -> scheduleContainer
                        3 -> delegateContainer
                        else -> eliminateContainer
                    }
                    
                    val colorRes = when (task.priority) {
                        1 -> R.color.do_first
                        2 -> R.color.schedule
                        3 -> R.color.delegate
                        else -> R.color.eliminate
                    }
                    
                    addTaskToQuadrant(container, task, colorRes)
                }
            }
        }
    }

    private fun addTaskToQuadrant(container: LinearLayout, task: Task, colorRes: Int) {
        val inflater = LayoutInflater.from(this)
        val taskView = inflater.inflate(R.layout.quadrant_task_item, container, false)

        val taskName = taskView.findViewById<TextView>(R.id.task_name)
        val dot = taskView.findViewById<View>(R.id.dot)

        taskName.text = task.name
        dot.setBackgroundResource(R.drawable.circle_light_cream)
        dot.backgroundTintList = getColorStateList(colorRes)

        if (task.isCompleted) {
            taskName.paintFlags = taskName.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            taskName.alpha = 0.5f
            dot.alpha = 0.5f
        } else {
            taskName.paintFlags = taskName.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            taskName.alpha = 1.0f
            dot.alpha = 1.0f
        }

        container.addView(taskView)
    }
}
