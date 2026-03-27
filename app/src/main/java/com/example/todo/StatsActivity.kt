package com.example.todo

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.todo.models.Task
import com.example.todo.repository.TaskRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StatsActivity : AppCompatActivity() {

    private lateinit var taskRepository: TaskRepository
    
    private lateinit var doneCountText: TextView
    private lateinit var pendingCountText: TextView
    private lateinit var overdueCountText: TextView
    private lateinit var progressCircle: ProgressBar
    private lateinit var percentageText: TextView
    
    private lateinit var doFirstStatsText: TextView
    private lateinit var scheduleStatsText: TextView
    private lateinit var delegateStatsText: TextView
    private lateinit var eliminateStatsText: TextView
    
    private lateinit var completedTasksContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        taskRepository = TaskRepository(this)
        initializeViews()
        setupNavigation()
        observeTasks()
    }

    private fun initializeViews() {
        doneCountText = findViewById(R.id.doneCount)
        pendingCountText = findViewById(R.id.pendingCount)
        overdueCountText = findViewById(R.id.overdueCount)
        progressCircle = findViewById(R.id.progressCircle)
        percentageText = findViewById(R.id.percentageText)
        
        doFirstStatsText = findViewById(R.id.doFirstStats)
        scheduleStatsText = findViewById(R.id.scheduleStats)
        delegateStatsText = findViewById(R.id.delegateStats)
        eliminateStatsText = findViewById(R.id.eliminateStats)
        
        completedTasksContainer = findViewById(R.id.completedTasksContainer)
    }

    private fun setupNavigation() {
        findViewById<LinearLayout>(R.id.homeBtn).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
        findViewById<LinearLayout>(R.id.viewPriorityQuadrantBtn).setOnClickListener {
            startActivity(Intent(this, ActivityQuadrent::class.java))
            finish()
        }
        findViewById<LinearLayout>(R.id.remindersBtn).setOnClickListener {
            startActivity(Intent(this, RemindersActivity::class.java))
            finish()
        }
        findViewById<LinearLayout>(R.id.statsBtn).setOnClickListener { /* Already here */ }
    }

    private fun observeTasks() {
        lifecycleScope.launch {
            taskRepository.getAllTasks().collectLatest { allTasks ->
                updateStatsUI(allTasks)
            }
        }
    }

    private fun updateStatsUI(tasks: List<Task>) {
        val doneTasks = tasks.filter { it.isCompleted }
        val pendingTasks = tasks.filter { !it.isCompleted }
        val now = System.currentTimeMillis()
        val overdueTasks = pendingTasks.filter { it.dueDate != null && it.dueDate < now }

        doneCountText.text = doneTasks.size.toString()
        pendingCountText.text = pendingTasks.size.toString()
        overdueCountText.text = overdueTasks.size.toString()

        val totalCount = tasks.size
        val percentage = if (totalCount > 0) (doneTasks.size * 100) / totalCount else 0
        progressCircle.progress = percentage
        percentageText.text = "$percentage%\ndone"

        // Priority breakdown
        updatePriorityStats(tasks)

        // Recently completed tasks
        updateCompletedTasksList(doneTasks)
    }

    private fun updatePriorityStats(tasks: List<Task>) {
        val priorities = listOf(1, 2, 3, 4)
        val statsTextViews = listOf(doFirstStatsText, scheduleStatsText, delegateStatsText, eliminateStatsText)

        priorities.forEachIndexed { index, p ->
            val totalInPriority = tasks.count { it.priority == p }
            val doneInPriority = tasks.count { it.priority == p && it.isCompleted }
            statsTextViews[index].text = "$doneInPriority/$totalInPriority"
        }
    }

    private fun updateCompletedTasksList(doneTasks: List<Task>) {
        completedTasksContainer.removeAllViews()
        
        // Show last 10 completed tasks
        val recentCompleted = doneTasks.sortedByDescending { it.id }.take(10)
        
        if (recentCompleted.isEmpty()) {
            val emptyText = TextView(this).apply {
                text = "No completed tasks yet"
                setTextColor(ContextCompat.getColor(context, R.color.secondary_text))
                setPadding(0, 40, 0, 0)
                gravity = android.view.Gravity.CENTER
            }
            completedTasksContainer.addView(emptyText)
            return
        }

        recentCompleted.forEach { task ->
            addCompletedTaskView(task)
        }
    }

    private fun addCompletedTaskView(task: Task) {
        val inflater = LayoutInflater.from(this)
        val card = inflater.inflate(R.layout.completed_task_item_view, completedTasksContainer, false)
        
        val title = card.findViewById<TextView>(R.id.completedTaskTitle)
        val date = card.findViewById<TextView>(R.id.completedDate)
        val deleteBtn = card.findViewById<Button>(R.id.deleteTaskBtn)
        val indicator = card.findViewById<View>(R.id.priorityIndicator)

        title.text = task.name
        
        // We don't have a completion date in the model, so we'll just show "Completed"
        date.text = "Status: Completed"
        
        val colorRes = when (task.priority) {
            1 -> R.color.do_first
            2 -> R.color.schedule
            3 -> R.color.delegate
            else -> R.color.eliminate
        }
        indicator.setBackgroundColor(ContextCompat.getColor(this, colorRes))

        deleteBtn.setOnClickListener {
            lifecycleScope.launch {
                taskRepository.deleteTaskById(task.id)
                Toast.makeText(this@StatsActivity, "Task deleted", Toast.LENGTH_SHORT).show()
            }
        }

        completedTasksContainer.addView(card)
    }
}
