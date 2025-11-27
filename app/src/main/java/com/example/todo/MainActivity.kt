package com.example.todo

import android.app.Activity
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.todo.models.Task
import com.example.todo.repository.TaskRepository
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    // Main Task Views
    private lateinit var addMainTaskBtn: ImageButton
    private lateinit var mainTaskNameDisplay: TextView
    private lateinit var completeMainTaskCheckBox: CheckBox
    private var currentMainTaskId: Long? = null

    // Minor Task Views (Subtasks)
    private lateinit var addMinorTaskBtn1: ImageButton
    private lateinit var minorTaskNameDisplay1: TextView
    private lateinit var completeMinorTaskCheckBox1: CheckBox
    private var currentSubtaskIds: MutableList<Long?> = mutableListOf(null, null, null)

    private lateinit var addMinorTaskBtn2: ImageButton
    private lateinit var minorTaskNameDisplay2: TextView
    private lateinit var completeMinorTaskCheckBox2: CheckBox

    private lateinit var addMinorTaskBtn3: ImageButton
    private lateinit var minorTaskNameDisplay3: TextView
    private lateinit var completeMinorTaskCheckBox3: CheckBox

    // Views for dynamic task list
    private lateinit var tasksContainer: LinearLayout
    private lateinit var addTaskPlaceholder: LinearLayout
    private lateinit var addTaskDivider: View

    // Navigation buttons
    private lateinit var homeButton: LinearLayout
    private lateinit var addNewTaskButton: LinearLayout
    private lateinit var quadrantButton: LinearLayout

    // Database and repository
    private lateinit var taskRepository: TaskRepository

    // To identify which "+" button was clicked
    private var clickedFixedTaskSourceTag: String? = null

    // Launcher for fixed tasks
    private val addFixedTaskLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { data ->
                val taskName = data.getStringExtra("TASK_NAME")
                val isUrgent = data.getBooleanExtra("IS_URGENT", false)
                val isImportant = data.getBooleanExtra("IS_IMPORTANT", false)
                val dueDate = data.getLongExtra("DUE_DATE", 0L)

                if (!taskName.isNullOrBlank()) {
                    lifecycleScope.launch {
                        val priority = calculatePriority(isUrgent, isImportant)
                        when (clickedFixedTaskSourceTag) {
                            "main_task" -> {
                                val task = Task(
                                    name = taskName,
                                    isUrgent = isUrgent,
                                    isImportant = isImportant,
                                    dueDate = if (dueDate > 0) dueDate else null,
                                    isMainTask = true,
                                    priority = priority
                                )
                                val id = taskRepository.insertTask(task)
                                currentMainTaskId = id
                                updateFixedTaskDisplay(mainTaskNameDisplay, taskName, addMainTaskBtn, completeMainTaskCheckBox)
                            }
                            "minor_task_1" -> {
                                val task = Task(
                                    name = taskName,
                                    isUrgent = isUrgent,
                                    isImportant = isImportant,
                                    dueDate = if (dueDate > 0) dueDate else null,
                                    isSubtask = true,
                                    priority = priority
                                )
                                val id = taskRepository.insertTask(task)
                                currentSubtaskIds[0] = id
                                updateFixedTaskDisplay(minorTaskNameDisplay1, taskName, addMinorTaskBtn1, completeMinorTaskCheckBox1)
                            }
                            "minor_task_2" -> {
                                val task = Task(
                                    name = taskName,
                                    isUrgent = isUrgent,
                                    isImportant = isImportant,
                                    dueDate = if (dueDate > 0) dueDate else null,
                                    isSubtask = true,
                                    priority = priority
                                )
                                val id = taskRepository.insertTask(task)
                                currentSubtaskIds[1] = id
                                updateFixedTaskDisplay(minorTaskNameDisplay2, taskName, addMinorTaskBtn2, completeMinorTaskCheckBox2)
                            }
                            "minor_task_3" -> {
                                val task = Task(
                                    name = taskName,
                                    isUrgent = isUrgent,
                                    isImportant = isImportant,
                                    dueDate = if (dueDate > 0) dueDate else null,
                                    isSubtask = true,
                                    priority = priority
                                )
                                val id = taskRepository.insertTask(task)
                                currentSubtaskIds[2] = id
                                updateFixedTaskDisplay(minorTaskNameDisplay3, taskName, addMinorTaskBtn3, completeMinorTaskCheckBox3)
                            }
                        }
                    }
                }
            }
        }
        clickedFixedTaskSourceTag = null
    }

    // Launcher for dynamic tasks
    private val addDynamicTaskLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.let { data ->
                    val taskName = data.getStringExtra("TASK_NAME")
                    val isUrgent = data.getBooleanExtra("IS_URGENT", false)
                    val isImportant = data.getBooleanExtra("IS_IMPORTANT", false)
                    val dueDate = data.getLongExtra("DUE_DATE", 0L)

                    if (!taskName.isNullOrBlank()) {
                        lifecycleScope.launch {
                            val priority = calculatePriority(isUrgent, isImportant)
                            val task = Task(
                                name = taskName,
                                isUrgent = isUrgent,
                                isImportant = isImportant,
                                dueDate = if (dueDate > 0) dueDate else null,
                                isMainTask = false,
                                isSubtask = false,
                                priority = priority
                            )
                            val insertedId = taskRepository.insertTask(task)
                            // Reload tasks to get the actual task with ID from database
                            loadTasksFromDatabase()
                        }
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize database
        taskRepository = TaskRepository(this)

        // Initialize Main Task Views
        addMainTaskBtn = findViewById(R.id.add_main_task_btn)
        mainTaskNameDisplay = findViewById(R.id.main_task_name_display)
        completeMainTaskCheckBox = findViewById(R.id.complete)

        // Initialize Minor Task Views
        addMinorTaskBtn1 = findViewById(R.id.add_minor_task_btn_1)
        minorTaskNameDisplay1 = findViewById(R.id.minor_task_name_display_1)
        completeMinorTaskCheckBox1 = findViewById(R.id.complete1)
        
        addMinorTaskBtn2 = findViewById(R.id.add_minor_task_btn_2)
        minorTaskNameDisplay2 = findViewById(R.id.minor_task_name_display_2)
        completeMinorTaskCheckBox2 = findViewById(R.id.complete2)
        
        addMinorTaskBtn3 = findViewById(R.id.add_minor_task_btn_3)
        minorTaskNameDisplay3 = findViewById(R.id.minor_task_name_display_3)
        completeMinorTaskCheckBox3 = findViewById(R.id.complete3)

        // Initialize dynamic task list views
        tasksContainer = findViewById(R.id.tasksContainer)
        addTaskPlaceholder = findViewById(R.id.addTaskPlaceholder)
        addTaskDivider = findViewById(R.id.addTaskDivider)

        addTaskPlaceholder.setOnClickListener {
            val intent = Intent(this, AddTaskActivity::class.java)
            addDynamicTaskLauncher.launch(intent)
        }

        // Initialize Navigation Buttons
        homeButton = findViewById(R.id.homeBtn)
        addNewTaskButton = findViewById(R.id.addNewTaskBtn)
        quadrantButton = findViewById(R.id.viewPriorityQuadrantBtn)

        // Setup click listeners for fixed "+" buttons
        addMainTaskBtn.setOnClickListener { startAddTaskActivityForFixedTask("main_task") }
        addMinorTaskBtn1.setOnClickListener { startAddTaskActivityForFixedTask("minor_task_1") }
        addMinorTaskBtn2.setOnClickListener { startAddTaskActivityForFixedTask("minor_task_2") }
        addMinorTaskBtn3.setOnClickListener { startAddTaskActivityForFixedTask("minor_task_3") }

        // Setup CheckBox Listeners for strikethrough effect
        setupCheckBoxListener(completeMainTaskCheckBox, mainTaskNameDisplay, 0)
        setupCheckBoxListener(completeMinorTaskCheckBox1, minorTaskNameDisplay1, 1)
        setupCheckBoxListener(completeMinorTaskCheckBox2, minorTaskNameDisplay2, 2)
        setupCheckBoxListener(completeMinorTaskCheckBox3, minorTaskNameDisplay3, 3)

        // Navigation Listeners
        homeButton.setOnClickListener {
            Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show()
        }

        addNewTaskButton.setOnClickListener {
            val intent = Intent(this, AddTaskActivity::class.java)
            addDynamicTaskLauncher.launch(intent)
        }

        quadrantButton.setOnClickListener {
            val intent = Intent(this, ActivityQuadrent::class.java)
            startActivity(intent)
        }

        // Load data from database
        loadTasksFromDatabase()
    }

    private fun loadTasksFromDatabase() {
        lifecycleScope.launch {
            // Load main task
            val mainTask = taskRepository.getMainTask()
            if (mainTask != null) {
                currentMainTaskId = mainTask.id
                updateFixedTaskDisplay(mainTaskNameDisplay, mainTask.name, addMainTaskBtn, completeMainTaskCheckBox)
                completeMainTaskCheckBox.isChecked = mainTask.isCompleted
            }

            // Load subtasks
            val subtasksFlow = taskRepository.getSubtasks()
            subtasksFlow.collect { subtasks ->
                for ((index, subtask) in subtasks.withIndex()) {
                    if (index < 3) {
                        currentSubtaskIds[index] = subtask.id
                        val display = when (index) {
                            0 -> minorTaskNameDisplay1
                            1 -> minorTaskNameDisplay2
                            else -> minorTaskNameDisplay3
                        }
                        val btn = when (index) {
                            0 -> addMinorTaskBtn1
                            1 -> addMinorTaskBtn2
                            else -> addMinorTaskBtn3
                        }
                        val checkbox = when (index) {
                            0 -> completeMinorTaskCheckBox1
                            1 -> completeMinorTaskCheckBox2
                            else -> completeMinorTaskCheckBox3
                        }
                        updateFixedTaskDisplay(display, subtask.name, btn, checkbox)
                        checkbox.isChecked = subtask.isCompleted
                    }
                }
            }

            // Load dynamic tasks
            val dynamicTasksFlow = taskRepository.getDynamicTasks()
            dynamicTasksFlow.collect { tasks ->
                // Clear existing dynamic tasks from view
                tasksContainer.removeAllViews()
                tasksContainer.addView(addTaskPlaceholder)
                tasksContainer.addView(addTaskDivider)
                
                for (task in tasks) {
                    addNewTaskViewToDynamicList(task.name, task.id, task)
                }
            }
            
            // Check and cleanup completed tasks
            cleanupCompletedTasks()
        }
    }

    private fun startAddTaskActivityForFixedTask(sourceTag: String) {
        clickedFixedTaskSourceTag = sourceTag
        val intent = Intent(this, AddTaskActivity::class.java)
        intent.putExtra("SOURCE_TAG", sourceTag)
        addFixedTaskLauncher.launch(intent)
    }

    private fun updateFixedTaskDisplay(textView: TextView, taskName: String, addButton: ImageButton, checkBox: CheckBox) {
        textView.text = taskName
        textView.visibility = View.VISIBLE
        checkBox.visibility = View.VISIBLE
        addButton.visibility = View.GONE
    }

    private fun addNewTaskViewToDynamicList(taskName: String, taskId: Long, task: Task) {
        val inflater = LayoutInflater.from(this)
        val taskView = inflater.inflate(R.layout.list_item_task, tasksContainer, false)

        val taskNameDisplay = taskView.findViewById<TextView>(R.id.task_name_display)
        val taskCheckbox = taskView.findViewById<CheckBox>(R.id.task_checkbox)
        val editButton = taskView.findViewById<ImageButton>(R.id.edit_task_btn)
        val deleteButton = taskView.findViewById<ImageButton>(R.id.delete_task_btn)

        taskNameDisplay.text = taskName
        taskCheckbox.isChecked = task.isCompleted
        if (task.isCompleted) {
            taskNameDisplay.paintFlags = taskNameDisplay.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        }
        
        // Store task ID in view tag
        taskView.tag = taskId

        setupDynamicCheckBoxListener(taskCheckbox, taskNameDisplay, taskId)
        
        // Setup edit button
        editButton.setOnClickListener {
            val intent = Intent(this, AddTaskActivity::class.java)
            intent.putExtra("EDIT_TASK_ID", taskId.toLong())
            intent.putExtra("EDIT_TASK_NAME", task.name)
            intent.putExtra("EDIT_TASK_URGENT", task.isUrgent)
            intent.putExtra("EDIT_TASK_IMPORTANT", task.isImportant)
            intent.putExtra("EDIT_TASK_DUE_DATE", task.dueDate ?: 0L)
            editTaskLauncher.launch(intent)
        }
        
        // Setup delete button
        deleteButton.setOnClickListener {
            lifecycleScope.launch {
                val taskToDelete = taskRepository.getTaskById(taskId)
                if (taskToDelete != null) {
                    taskRepository.deleteTask(taskToDelete)
                    tasksContainer.removeView(taskView)
                    // Also remove divider if exists
                    val viewIndex = tasksContainer.indexOfChild(taskView)
                    if (viewIndex < tasksContainer.childCount - 1) {
                        val nextView = tasksContainer.getChildAt(viewIndex + 1)
                        if (nextView is View && nextView.height == (1 * resources.displayMetrics.density).toInt()) {
                            tasksContainer.removeView(nextView)
                        }
                    }
                }
            }
        }

        val indexOfPlaceholder = tasksContainer.indexOfChild(addTaskPlaceholder)
        if (indexOfPlaceholder != -1) {
            tasksContainer.removeViewAt(indexOfPlaceholder)
            if (tasksContainer.indexOfChild(addTaskDivider) != -1) {
                tasksContainer.removeView(addTaskDivider)
            }
        }

        tasksContainer.addView(taskView)

        val newDivider = View(this)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            (1 * resources.displayMetrics.density).toInt()
        )
        newDivider.setBackgroundColor(getColor(R.color.divider))
        newDivider.layoutParams = layoutParams
        tasksContainer.addView(newDivider)

        tasksContainer.addView(addTaskPlaceholder)
        tasksContainer.addView(addTaskDivider)
    }

    private fun setupCheckBoxListener(checkBox: CheckBox, textView: TextView, subtaskIndex: Int) {
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                textView.paintFlags = textView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                textView.paintFlags = textView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }

            lifecycleScope.launch {
                val taskId = when (subtaskIndex) {
                    0 -> currentMainTaskId ?: return@launch
                    else -> currentSubtaskIds.getOrNull(subtaskIndex - 1) ?: return@launch
                }
                taskRepository.updateTaskCompletion(taskId, isChecked)
            }
        }
    }

    private fun setupDynamicCheckBoxListener(checkBox: CheckBox, textView: TextView, taskId: Long) {
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                textView.paintFlags = textView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                textView.paintFlags = textView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
            
            lifecycleScope.launch {
                taskRepository.updateTaskCompletion(taskId, isChecked)
                if (isChecked) {
                    // Check if all tasks are completed and cleanup
                    cleanupCompletedTasks()
                }
            }
        }
    }
    
    private fun calculatePriority(isUrgent: Boolean, isImportant: Boolean): Int {
        return when {
            isUrgent && isImportant -> 1  // DO_FIRST
            !isUrgent && isImportant -> 2  // SCHEDULE (Not Urgent but Important)
            isUrgent && !isImportant -> 3  // DELEGATE (Urgent but Not Important)
            else -> 4  // ELIMINATE (Neither)
        }
    }
    
    private fun cleanupCompletedTasks() {
        lifecycleScope.launch {
            val completedTasks = taskRepository.getAllCompletedTasks()
            if (completedTasks.isNotEmpty()) {
                val now = System.currentTimeMillis()
                // Remove completed tasks that have passed their due date
                val tasksToDelete = completedTasks.filter { 
                    it.dueDate != null && it.dueDate < now 
                }
                for (task in tasksToDelete) {
                    taskRepository.deleteTask(task)
                }
            }
        }
    }
    
    // Launcher for editing tasks
    private val editTaskLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { data ->
                val taskId = data.getLongExtra("EDIT_TASK_ID", -1L)
                val taskName = data.getStringExtra("TASK_NAME")
                val isUrgent = data.getBooleanExtra("IS_URGENT", false)
                val isImportant = data.getBooleanExtra("IS_IMPORTANT", false)
                val dueDate = data.getLongExtra("DUE_DATE", 0L)

                if (taskId != -1L && !taskName.isNullOrBlank()) {
                    lifecycleScope.launch {
                        val existingTask = taskRepository.getTaskById(taskId)
                        if (existingTask != null) {
                            val priority = calculatePriority(isUrgent, isImportant)
                            val updatedTask = existingTask.copy(
                                name = taskName,
                                isUrgent = isUrgent,
                                isImportant = isImportant,
                                dueDate = if (dueDate > 0) dueDate else null,
                                priority = priority
                            )
                            taskRepository.updateTask(updatedTask)
                            // Reload tasks to refresh the view
                            loadTasksFromDatabase()
                        }
                    }
                }
            }
        }
    }
}
