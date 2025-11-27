package com.example.todo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.todo.repository.TaskRepository
import com.example.todo.utils.QuadrantUtils
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
    private lateinit var addTasksButton: LinearLayout
    private lateinit var quadrent: LinearLayout

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
        addTasksButton = findViewById(R.id.addNewTaskBtn)
        quadrent = findViewById(R.id.viewPriorityQuadrantBtn)

        setupNavigation()
        loadTasksIntoQuadrants()
    }

    private fun setupNavigation() {
        homeButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        addTasksButton.setOnClickListener {
            Log.d("Navigation", "Calendar clicked")
        }

        quadrent.setOnClickListener {
            val intent = Intent(this, BambooTargetActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun loadTasksIntoQuadrants() {
        lifecycleScope.launch {
            taskRepository.getAllTasks().collect { tasks ->
                // Clear all containers
                doFirstContainer.removeAllViews()
                scheduleContainer.removeAllViews()
                delegateContainer.removeAllViews()
                eliminateContainer.removeAllViews()

                // Group tasks by quadrant
                val groupedTasks = QuadrantUtils.getTasksGroupedByQuadrant(tasks)

                // Populate each quadrant
                for ((quadrant, quadrantTasks) in groupedTasks) {
                    val container = when (quadrant) {
                        QuadrantUtils.Quadrant.DO_FIRST -> doFirstContainer
                        QuadrantUtils.Quadrant.SCHEDULE -> scheduleContainer
                        QuadrantUtils.Quadrant.DELEGATE -> delegateContainer
                        QuadrantUtils.Quadrant.ELIMINATE -> eliminateContainer
                    }

                    for (task in quadrantTasks) {
                        addTaskToQuadrant(container, task)
                    }
                }
            }
        }
    }

    private fun addTaskToQuadrant(container: LinearLayout, task: com.example.todo.models.Task) {
        val inflater = LayoutInflater.from(this)
        val taskView = inflater.inflate(R.layout.quadrant_task_item, container, false)

        val taskName = taskView.findViewById<TextView>(R.id.task_name)
        val taskCheckbox = taskView.findViewById<CheckBox>(R.id.task_checkbox)

        taskName.text = task.name
        taskCheckbox.isChecked = task.isCompleted

        taskCheckbox.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                taskRepository.updateTaskCompletion(task.id, isChecked)
            }
        }

        container.addView(taskView)
    }
}
