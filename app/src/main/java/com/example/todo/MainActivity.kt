package com.example.todo

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ScrollView
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

    // --- VIEWS ---
    private lateinit var mainTaskMenuBtn: TextView
    private lateinit var minorTaskMenuBtn1: TextView
    private lateinit var minorTaskMenuBtn2: TextView
    private lateinit var minorTaskMenuBtn3: TextView
    private lateinit var addMainTaskBtn: ImageButton
    private lateinit var mainTaskNameDisplay: TextView
    private lateinit var completeMainTaskCheckBox: CheckBox
    private lateinit var addMinorTaskBtn1: ImageButton
    private lateinit var minorTaskNameDisplay1: TextView
    private lateinit var completeMinorTaskCheckBox1: CheckBox
    private lateinit var addMinorTaskBtn2: ImageButton
    private lateinit var minorTaskNameDisplay2: TextView
    private lateinit var completeMinorTaskCheckBox2: CheckBox
    private lateinit var addMinorTaskBtn3: ImageButton
    private lateinit var minorTaskNameDisplay3: TextView
    private lateinit var completeMinorTaskCheckBox3: CheckBox
    private lateinit var tasksContainer: LinearLayout
    private lateinit var addTaskPlaceholder: LinearLayout
    private lateinit var scrollViewTasks: ScrollView
    private lateinit var homeButton: LinearLayout
    private lateinit var addNewTaskButton: LinearLayout
    private lateinit var quadrantButton: LinearLayout

    // --- STATE & DATA ---
    private lateinit var taskRepository: TaskRepository
    private var currentMainTaskId: Long? = null
    private var currentSubtaskIds: MutableList<Long?> = mutableListOf(null, null, null)
    private var clickedFixedTaskSourceTag: String? = null

    // --- ACTIVITY RESULT LAUNCHERS ---

    private val addFixedTaskLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // A task was added, just reload everything to be safe
            loadTasksFromDatabase()
        }
    }

    private val addDynamicTaskLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // A dynamic task was added, reload the dynamic list and scroll
                loadDynamicTasks(scrollToBottom = true)
            }
        }

    private val editTaskLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // A task was edited, reload everything to reflect changes
            loadTasksFromDatabase()
        }
    }

    // --- LIFECYCLE METHODS ---

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        // Load or refresh data every time the activity comes into the foreground
        loadTasksFromDatabase()
    }

    // --- INITIALIZATION ---

    private fun initializeViews() {
        taskRepository = TaskRepository(this)

        // Main Task Views
        addMainTaskBtn = findViewById(R.id.add_main_task_btn)
        mainTaskNameDisplay = findViewById(R.id.main_task_name_display)
        completeMainTaskCheckBox = findViewById(R.id.complete)
        mainTaskMenuBtn = findViewById(R.id.main_task_menu_btn)

        // Subtask 1 Views
        addMinorTaskBtn1 = findViewById(R.id.add_minor_task_btn_1)
        minorTaskNameDisplay1 = findViewById(R.id.minor_task_name_display_1)
        completeMinorTaskCheckBox1 = findViewById(R.id.complete1)
        minorTaskMenuBtn1 = findViewById(R.id.minor_task_menu_btn_1)

        // Subtask 2 Views
        addMinorTaskBtn2 = findViewById(R.id.add_minor_task_btn_2)
        minorTaskNameDisplay2 = findViewById(R.id.minor_task_name_display_2)
        completeMinorTaskCheckBox2 = findViewById(R.id.complete2)
        minorTaskMenuBtn2 = findViewById(R.id.minor_task_menu_btn_2)

        // Subtask 3 Views
        addMinorTaskBtn3 = findViewById(R.id.add_minor_task_btn_3)
        minorTaskNameDisplay3 = findViewById(R.id.minor_task_name_display_3)
        completeMinorTaskCheckBox3 = findViewById(R.id.complete3)
        minorTaskMenuBtn3 = findViewById(R.id.minor_task_menu_btn_3)

        // Other Views
        tasksContainer = findViewById(R.id.tasksContainer)
        addTaskPlaceholder = findViewById(R.id.addTaskPlaceholder)
        scrollViewTasks = findViewById(R.id.scrollViewTasks)
        homeButton = findViewById(R.id.homeBtn)
        addNewTaskButton = findViewById(R.id.addNewTaskBtn)
        quadrantButton = findViewById(R.id.viewPriorityQuadrantBtn)
    }

    private fun setupListeners() {
        // Fixed "+" buttons
        addMainTaskBtn.setOnClickListener { startAddTaskActivityForFixedTask("main_task") }
        addMinorTaskBtn1.setOnClickListener { handleSubtaskAddClick("minor_task_1") }
        addMinorTaskBtn2.setOnClickListener { handleSubtaskAddClick("minor_task_2") }
        addMinorTaskBtn3.setOnClickListener { handleSubtaskAddClick("minor_task_3") }

        // CheckBox Listeners for fixed tasks
        setupCheckBoxListener(completeMainTaskCheckBox, mainTaskNameDisplay, 0)
        setupCheckBoxListener(completeMinorTaskCheckBox1, minorTaskNameDisplay1, 1)
        setupCheckBoxListener(completeMinorTaskCheckBox2, minorTaskNameDisplay2, 2)
        setupCheckBoxListener(completeMinorTaskCheckBox3, minorTaskNameDisplay3, 3)

        // Navigation and Dynamic Task Add
        homeButton.setOnClickListener { Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show() }
        addNewTaskButton.setOnClickListener { addDynamicTaskLauncher.launch(Intent(this, AddTaskActivity::class.java)) }
        quadrantButton.setOnClickListener { startActivity(Intent(this, ActivityQuadrent::class.java)) }
        addTaskPlaceholder.setOnClickListener { addDynamicTaskLauncher.launch(Intent(this, AddTaskActivity::class.java)) }
    }

    // --- DATA LOADING & UI UPDATING ---

    private fun loadTasksFromDatabase(scrollToBottom: Boolean = false) {
        resetFixedTaskDisplay()
        lifecycleScope.launch {
            // Load main task
            taskRepository.getMainTask()?.let { mainTask ->
                currentMainTaskId = mainTask.id
                updateFixedTaskDisplay(mainTaskNameDisplay, mainTask.name, addMainTaskBtn, completeMainTaskCheckBox, mainTaskMenuBtn)
                completeMainTaskCheckBox.isChecked = mainTask.isCompleted
                applyStrikethrough(mainTaskNameDisplay, mainTask.isCompleted)
                mainTaskMenuBtn.setOnClickListener { showTaskMenu(mainTask, it) }

                // Load subtasks for the main task
                val subtasks = taskRepository.getSubtasksAsList(mainTask.id)
                subtasks.forEachIndexed { index, subtask ->
                    if (index < 3) {
                        currentSubtaskIds[index] = subtask.id
                        val (display, button, checkbox) = getSubtaskViews(index)

                        // **FIX**: Get the correct menu button for the current subtask
                        val menuBtn = when (index) {
                            0 -> minorTaskMenuBtn1
                            1 -> minorTaskMenuBtn2
                            else -> minorTaskMenuBtn3
                        }

                        updateFixedTaskDisplay(display, subtask.name, button, checkbox, menuBtn)
                        checkbox.isChecked = subtask.isCompleted
                        applyStrikethrough(display, subtask.isCompleted)
                        // **FIX**: Set the listener on the correct menu button
                        menuBtn.setOnClickListener { showTaskMenu(subtask, it) }
                    }
                }
            }
            // Load dynamic tasks
            loadDynamicTasks(scrollToBottom)
        }
    }

    private fun loadDynamicTasks(scrollToBottom: Boolean = false) {
        lifecycleScope.launch {
            val tasks = taskRepository.getDynamicTasksAsList()
            // Efficiently remove old dynamic task views
            val viewsToRemove = mutableListOf<View>()
            for (i in 0 until tasksContainer.childCount) {
                val child = tasksContainer.getChildAt(i)
                if (child.tag != null && child.tag is Long) {
                    viewsToRemove.add(child)
                }
            }
            viewsToRemove.forEach { tasksContainer.removeView(it) }


            // Add new views
            tasks.forEach { addNewTaskViewToDynamicList(it) }

            if (scrollToBottom) {
                scrollViewTasks.post { scrollViewTasks.fullScroll(View.FOCUS_DOWN) }
            }
        }
    }

    private fun resetFixedTaskDisplay() {
        // Reset Main Task
        mainTaskNameDisplay.visibility = View.GONE
        completeMainTaskCheckBox.visibility = View.GONE
        mainTaskMenuBtn.visibility = View.GONE // Hide main menu
        addMainTaskBtn.visibility = View.VISIBLE
        mainTaskNameDisplay.text = ""
        completeMainTaskCheckBox.isChecked = false
        applyStrikethrough(mainTaskNameDisplay, false)
        currentMainTaskId = null

        // Reset Subtasks
        for (i in 0..2) {
            val (display, button, checkbox) = getSubtaskViews(i)

            // **FIX**: Get the correct menu button to hide it
            val menuBtn = when (i) {
                0 -> minorTaskMenuBtn1
                1 -> minorTaskMenuBtn2
                else -> minorTaskMenuBtn3
            }
            menuBtn.visibility = View.GONE // Hide menu

            display.visibility = View.GONE
            checkbox.visibility = View.GONE
            button.visibility = View.VISIBLE
            display.text = ""
            checkbox.isChecked = false
            applyStrikethrough(display, false)
        }
        currentSubtaskIds = mutableListOf(null, null, null)
    }

    private fun addNewTaskViewToDynamicList(task: Task) {
        val taskView = LayoutInflater.from(this).inflate(R.layout.list_item_task, tasksContainer, false)
        val taskNameDisplay = taskView.findViewById<TextView>(R.id.task_name_display)
        val taskCheckbox = taskView.findViewById<CheckBox>(R.id.task_checkbox)
        val menuButton = taskView.findViewById<TextView>(R.id.task_menu_btn)

        taskNameDisplay.text = task.name
        taskCheckbox.isChecked = task.isCompleted
        applyStrikethrough(taskNameDisplay, task.isCompleted)
        taskView.tag = task.id // Set tag for identification

        setupDynamicCheckBoxListener(taskCheckbox, taskNameDisplay, task.id)
        menuButton.setOnClickListener { showTaskMenu(task, it) }

        val placeholderIndex = tasksContainer.indexOfChild(addTaskPlaceholder)
        tasksContainer.addView(taskView, placeholderIndex)
    }

    // --- HELPER & EVENT HANDLER FUNCTIONS ---

    private fun handleSubtaskAddClick(sourceTag: String) {
        if (currentMainTaskId == null) {
            Toast.makeText(this, "Please create a main task first", Toast.LENGTH_SHORT).show()
        } else {
            startAddTaskActivityForFixedTask(sourceTag)
        }
    }

    private fun startAddTaskActivityForFixedTask(sourceTag: String) {
        clickedFixedTaskSourceTag = sourceTag
        val intent = Intent(this, AddTaskActivity::class.java).putExtra("SOURCE_TAG", sourceTag)
        addFixedTaskLauncher.launch(intent)
    }

    // This function signature is correct
    private fun updateFixedTaskDisplay(
        textView: TextView,
        taskName: String,
        addButton: ImageButton,
        checkBox: CheckBox,
        menuButton: TextView
    ) {
        textView.text = taskName
        textView.visibility = View.VISIBLE
        checkBox.visibility = View.VISIBLE
        menuButton.visibility = View.VISIBLE
        addButton.visibility = View.GONE
    }

    private fun setupCheckBoxListener(checkBox: CheckBox, textView: TextView, taskIndex: Int) {
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            applyStrikethrough(textView, isChecked)
            lifecycleScope.launch {
                val taskId = if (taskIndex == 0) currentMainTaskId else currentSubtaskIds.getOrNull(taskIndex - 1)
                taskId?.let { taskRepository.updateTaskCompletion(it, isChecked) }
            }
        }
    }

    private fun setupDynamicCheckBoxListener(checkBox: CheckBox, textView: TextView, taskId: Long) {
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            applyStrikethrough(textView, isChecked)
            lifecycleScope.launch {
                taskRepository.updateTaskCompletion(taskId, isChecked)
            }
        }
    }

    private fun applyStrikethrough(textView: TextView, isCompleted: Boolean) {
        textView.paintFlags = if (isCompleted) {
            textView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            textView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
    }

    private fun showTaskMenu(task: Task, anchorView: View) {
        val options = arrayOf("Info", "Edit", "Delete")
        AlertDialog.Builder(this).setTitle("Task Options").setItems(options) { _, which ->
            when (which) {
                0 -> showTaskInfo(task)
                1 -> editTask(task)
                2 -> deleteTask(task)
            }
        }.show()
    }

    private fun editTask(task: Task) {
        val intent = Intent(this, AddTaskActivity::class.java).apply {
            putExtra("EDIT_TASK_ID", task.id)
        }
        editTaskLauncher.launch(intent)
    }

    private fun deleteTask(task: Task) {
        AlertDialog.Builder(this)
            .setTitle("Delete Task")
            .setMessage("Are you sure you want to delete '${task.name}'?")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    taskRepository.deleteTask(task)
                    loadTasksFromDatabase() // Reload all data to ensure consistency
                    Toast.makeText(this@MainActivity, "Task deleted", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showTaskInfo(task: Task) {
        val dueDateStr = task.dueDate?.let {
            java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(it))
        } ?: "Not set"
        val priorityName = when (task.priority) {
            1 -> "Do First"
            2 -> "Schedule"
            3 -> "Delegate"
            else -> "Eliminate"
        }
        val infoMessage = """
            Status: ${if (task.isCompleted) "Completed" else "Pending"}
            Urgent: ${if (task.isUrgent) "Yes" else "No"}
            Important: ${if (task.isImportant) "Yes" else "No"}
            Priority: $priorityName
            Due Date: $dueDateStr
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle(task.name)
            .setMessage(infoMessage)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun calculatePriority(isUrgent: Boolean, isImportant: Boolean): Int {
        return when {
            isUrgent && isImportant -> 1
            !isUrgent && isImportant -> 2
            isUrgent && !isImportant -> 3
            else -> 4
        }
    }

    // **FIX**: Changed back to return a Triple
    private fun getSubtaskViews(index: Int): Triple<TextView, ImageButton, CheckBox> {
        return when (index) {
            0 -> Triple(minorTaskNameDisplay1, addMinorTaskBtn1, completeMinorTaskCheckBox1)
            1 -> Triple(minorTaskNameDisplay2, addMinorTaskBtn2, completeMinorTaskCheckBox2)
            else -> Triple(minorTaskNameDisplay3, addMinorTaskBtn3, completeMinorTaskCheckBox3)
        }
    }
}
