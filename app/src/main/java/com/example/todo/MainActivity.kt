package com.example.todo

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Paint
import android.os.Build
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.todo.models.Task
import com.example.todo.repository.TaskRepository
import com.example.todo.utils.NotificationHelper
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    // --- VIEWS ---
    private lateinit var tasksDoneBadge: TextView
    private lateinit var mainTaskNameDisplay: TextView
    private lateinit var tagUrgent: TextView
    private lateinit var tagImportant: TextView
    private lateinit var tagDueDate: TextView
    private lateinit var subtasksContainer: LinearLayout
    private lateinit var mainTaskCard: View
    private lateinit var addMainTaskBtn: ImageButton
    private lateinit var completeMainTaskCheckBox: CheckBox
    
    private lateinit var minorTaskNameDisplay1: TextView
    private lateinit var completeMinorTaskCheckBox1: CheckBox
    private lateinit var minorTaskNameDisplay2: TextView
    private lateinit var completeMinorTaskCheckBox2: CheckBox
    private lateinit var minorTaskNameDisplay3: TextView
    private lateinit var completeMinorTaskCheckBox3: CheckBox
    
    private lateinit var tasksContainer: LinearLayout
    private lateinit var scrollViewTasks: ScrollView
    private lateinit var homeButton: LinearLayout
    private lateinit var quadrantButton: LinearLayout
    private lateinit var remindersButton: LinearLayout
    private lateinit var statsButton: LinearLayout
    private lateinit var addNewTaskButton: com.google.android.material.floatingactionbutton.FloatingActionButton

    // --- STATE & DATA ---
    private lateinit var taskRepository: TaskRepository
    private var currentMainTaskId: Long? = null
    private var currentSubtaskIds: MutableList<Long?> = mutableListOf(null, null, null)
    private var clickedFixedTaskSourceTag: String? = null

    // --- PERMISSIONS ---
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (!isGranted) {
            Toast.makeText(this, "Notifications disabled. Reminders won't work.", Toast.LENGTH_SHORT).show()
        }
    }

    // --- ACTIVITY RESULT LAUNCHERS ---

    private val addFixedTaskLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { data ->
                val taskName = data.getStringExtra("TASK_NAME")
                val isUrgent = data.getBooleanExtra("IS_URGENT", false)
                val isImportant = data.getBooleanExtra("IS_IMPORTANT", false)
                val dueDate = data.getLongExtra("DUE_DATE", 0L)
                val reminderType = data.getIntExtra("REMINDER_TYPE", 0)

                if (!taskName.isNullOrBlank()) {
                    lifecycleScope.launch {
                        val priority = calculatePriority(isUrgent, isImportant)
                        val newTask = when (clickedFixedTaskSourceTag) {
                            "main_task" -> Task(name = taskName, isUrgent = isUrgent, isImportant = isImportant, dueDate = if (dueDate > 0) dueDate else null, isMainTask = true, priority = priority, parentId = null)
                            else -> Task(name = taskName, isUrgent = isUrgent, isImportant = isImportant, dueDate = if (dueDate > 0) dueDate else null, isSubtask = true, priority = priority, parentId = currentMainTaskId)
                        }
                        
                        val newId = taskRepository.insertTask(newTask)
                        handleReminderScheduling(newId, taskName, dueDate, reminderType)
                        loadTasksFromDatabase()
                    }
                }
            }
        }
        clickedFixedTaskSourceTag = null
    }

    private val addDynamicTaskLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { data ->
                val taskName = data.getStringExtra("TASK_NAME")
                val isUrgent = data.getBooleanExtra("IS_URGENT", false)
                val isImportant = data.getBooleanExtra("IS_IMPORTANT", false)
                val dueDate = data.getLongExtra("DUE_DATE", 0L)
                val reminderType = data.getIntExtra("REMINDER_TYPE", 0)

                if (!taskName.isNullOrBlank()) {
                    lifecycleScope.launch {
                        val priority = calculatePriority(isUrgent, isImportant)
                        val newTask = Task(name = taskName, isUrgent = isUrgent, isImportant = isImportant, dueDate = if (dueDate > 0) dueDate else null, priority = priority)
                        val newId = taskRepository.insertTask(newTask)
                        handleReminderScheduling(newId, taskName, dueDate, reminderType)
                        loadTasksFromDatabase(scrollToBottom = true)
                    }
                }
            }
        }
    }

    private val editTaskLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { data ->
                val taskId = data.getLongExtra("EDIT_TASK_ID", -1L)
                val taskName = data.getStringExtra("TASK_NAME")
                val isUrgent = data.getBooleanExtra("IS_URGENT", false)
                val isImportant = data.getBooleanExtra("IS_IMPORTANT", false)
                val dueDate = data.getLongExtra("DUE_DATE", 0L)
                val reminderType = data.getIntExtra("REMINDER_TYPE", 0)

                if (taskId != -1L && !taskName.isNullOrBlank()) {
                    lifecycleScope.launch {
                        val existingTask = taskRepository.getTaskById(taskId)
                        existingTask?.let {
                            val updatedTask = it.copy(
                                name = taskName,
                                isUrgent = isUrgent,
                                isImportant = isImportant,
                                dueDate = if (dueDate > 0) dueDate else null,
                                priority = calculatePriority(isUrgent, isImportant)
                            )
                            taskRepository.updateTask(updatedTask)
                            handleReminderScheduling(taskId, taskName, dueDate, reminderType)
                            loadTasksFromDatabase()
                        }
                    }
                }
            }
        }
    }

    // --- LIFECYCLE METHODS ---

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()
        setupListeners()
        updateCurrentDate()
        checkNotificationPermission()
    }

    override fun onResume() {
        super.onResume()
        loadTasksFromDatabase()
    }

    // --- INITIALIZATION ---

    private fun initializeViews() {
        taskRepository = TaskRepository(this)

        tasksDoneBadge = findViewById(R.id.tasksDoneBadge)
        mainTaskCard = findViewById(R.id.mainTaskCard)
        mainTaskNameDisplay = findViewById(R.id.main_task_name_display)
        tagUrgent = findViewById(R.id.tagUrgent)
        tagImportant = findViewById(R.id.tagImportant)
        tagDueDate = findViewById(R.id.tagDueDate)
        subtasksContainer = findViewById(R.id.subtasksContainer)
        addMainTaskBtn = findViewById(R.id.add_main_task_btn)
        completeMainTaskCheckBox = findViewById(R.id.completeMainTaskCheckBox)

        minorTaskNameDisplay1 = findViewById(R.id.minor_task_name_display_1)
        completeMinorTaskCheckBox1 = findViewById(R.id.complete1)
        minorTaskNameDisplay2 = findViewById(R.id.minor_task_name_display_2)
        completeMinorTaskCheckBox2 = findViewById(R.id.complete2)
        minorTaskNameDisplay3 = findViewById(R.id.minor_task_name_display_3)
        completeMinorTaskCheckBox3 = findViewById(R.id.complete3)

        tasksContainer = findViewById(R.id.tasksContainer)
        scrollViewTasks = findViewById(R.id.scrollViewTasks)
        
        homeButton = findViewById(R.id.homeBtn)
        quadrantButton = findViewById(R.id.viewPriorityQuadrantBtn)
        remindersButton = findViewById(R.id.remindersBtn)
        statsButton = findViewById(R.id.statsBtn)
        addNewTaskButton = findViewById(R.id.addNewTaskBtn)
    }

    private fun setupListeners() {
        mainTaskCard.setOnClickListener {
            if (currentMainTaskId == null) {
                startAddTaskActivityForFixedTask("main_task")
            } else {
                lifecycleScope.launch {
                    taskRepository.getTaskById(currentMainTaskId!!)?.let { editTask(it) }
                }
            }
        }

        completeMainTaskCheckBox.setOnCheckedChangeListener { _, isChecked ->
            applyStrikethrough(mainTaskNameDisplay, isChecked)
            lifecycleScope.launch {
                currentMainTaskId?.let { 
                    taskRepository.updateTaskCompletion(it, isChecked) 
                    updateDoneBadge()
                }
            }
        }
        
        findViewById<View>(R.id.subtask1Layout).setOnClickListener {
            if (currentSubtaskIds[0] == null) handleSubtaskAddClick("minor_task_1")
            else lifecycleScope.launch { taskRepository.getTaskById(currentSubtaskIds[0]!!)?.let { editTask(it) } }
        }
        findViewById<View>(R.id.subtask2Layout).setOnClickListener {
            if (currentSubtaskIds[1] == null) handleSubtaskAddClick("minor_task_2")
            else lifecycleScope.launch { taskRepository.getTaskById(currentSubtaskIds[1]!!)?.let { editTask(it) } }
        }
        findViewById<View>(R.id.subtask3Layout).setOnClickListener {
            if (currentSubtaskIds[2] == null) handleSubtaskAddClick("minor_task_3")
            else lifecycleScope.launch { taskRepository.getTaskById(currentSubtaskIds[2]!!)?.let { editTask(it) } }
        }

        setupCheckBoxListener(completeMinorTaskCheckBox1, minorTaskNameDisplay1, 1)
        setupCheckBoxListener(completeMinorTaskCheckBox2, minorTaskNameDisplay2, 2)
        setupCheckBoxListener(completeMinorTaskCheckBox3, minorTaskNameDisplay3, 3)

        homeButton.setOnClickListener { /* Already Home */ }
        addNewTaskButton.setOnClickListener { addDynamicTaskLauncher.launch(Intent(this, AddTaskActivity::class.java)) }
        quadrantButton.setOnClickListener { startNoAnimActivity(ActivityQuadrent::class.java) }
        remindersButton.setOnClickListener { startNoAnimActivity(RemindersActivity::class.java) }
        statsButton.setOnClickListener { startNoAnimActivity(StatsActivity::class.java) }
    }

    private fun startNoAnimActivity(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        startActivity(intent)
        overridePendingTransition(0, 0)
    }

    private suspend fun updateDoneBadge() {
        val completedCount = taskRepository.getCompletedTasksCount()
        tasksDoneBadge.text = "$completedCount tasks done"
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun handleReminderScheduling(taskId: Long, taskName: String, dueDate: Long, reminderType: Int) {
        if (reminderType > 0 && dueDate > 0) {
            val reminderTime = when (reminderType) {
                1 -> dueDate - (60 * 60 * 1000) // 1 hr before
                2 -> dueDate - (24 * 60 * 60 * 1000) // 1 day before
                else -> 0L
            }
            if (reminderTime > System.currentTimeMillis()) {
                NotificationHelper.scheduleReminder(this, taskId, taskName, reminderTime)
            }
        }
    }

    private fun updateCurrentDate() {
        val sdf = SimpleDateFormat("EEEE, MMM dd", Locale.getDefault())
        findViewById<TextView>(R.id.currentDate).text = sdf.format(Date())
    }

    // --- DATA LOADING & UI UPDATING ---

    private fun loadTasksFromDatabase(scrollToBottom: Boolean = false) {
        resetFixedTaskDisplay()
        lifecycleScope.launch {
            updateDoneBadge()

            taskRepository.getMainTask()?.let { mainTask ->
                currentMainTaskId = mainTask.id
                mainTaskNameDisplay.text = mainTask.name
                addMainTaskBtn.visibility = View.GONE
                completeMainTaskCheckBox.visibility = View.VISIBLE
                completeMainTaskCheckBox.isChecked = mainTask.isCompleted
                applyStrikethrough(mainTaskNameDisplay, mainTask.isCompleted)
                
                tagUrgent.visibility = if (mainTask.isUrgent) View.VISIBLE else View.GONE
                tagImportant.visibility = if (mainTask.isImportant) View.VISIBLE else View.GONE
                tagDueDate.visibility = if (mainTask.dueDate != null) View.VISIBLE else View.GONE
                mainTask.dueDate?.let {
                    tagDueDate.text = "Due " + SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(it))
                }

                val subtasks = taskRepository.getSubtasksAsList(mainTask.id)
                subtasks.forEachIndexed { index, subtask ->
                    if (index < 3) {
                        currentSubtaskIds[index] = subtask.id
                        val (display, checkbox) = getSubtaskViews(index)
                        display.text = subtask.name
                        checkbox.visibility = View.VISIBLE
                        checkbox.isChecked = subtask.isCompleted
                        applyStrikethrough(display, subtask.isCompleted)
                    }
                }
            }
            loadDynamicTasks(scrollToBottom)
        }
    }

    private fun loadDynamicTasks(scrollToBottom: Boolean = false) {
        lifecycleScope.launch {
            val tasks = taskRepository.getDynamicTasksAsList()
            tasksContainer.removeAllViews()
            tasks.forEach { addNewTaskViewToDynamicList(it) }

            if (scrollToBottom) {
                scrollViewTasks.post { scrollViewTasks.fullScroll(View.FOCUS_DOWN) }
            }
        }
    }

    private fun resetFixedTaskDisplay() {
        mainTaskNameDisplay.text = "Tap to add a focus task"
        addMainTaskBtn.visibility = View.VISIBLE
        completeMainTaskCheckBox.visibility = View.GONE
        tagUrgent.visibility = View.GONE
        tagImportant.visibility = View.GONE
        tagDueDate.visibility = View.GONE
        currentMainTaskId = null
        currentSubtaskIds = mutableListOf(null, null, null)

        minorTaskNameDisplay1.text = "Tap to add subtask"
        completeMinorTaskCheckBox1.visibility = View.GONE
        minorTaskNameDisplay2.text = "Tap to add subtask"
        completeMinorTaskCheckBox2.visibility = View.GONE
        minorTaskNameDisplay3.text = "Tap to add subtask"
        completeMinorTaskCheckBox3.visibility = View.GONE
        
        applyStrikethrough(minorTaskNameDisplay1, false)
        applyStrikethrough(minorTaskNameDisplay2, false)
        applyStrikethrough(minorTaskNameDisplay3, false)
        applyStrikethrough(mainTaskNameDisplay, false)
    }

    private fun addNewTaskViewToDynamicList(task: Task) {
        val taskView = LayoutInflater.from(this).inflate(R.layout.list_item_task, tasksContainer, false)
        val taskNameDisplay = taskView.findViewById<TextView>(R.id.task_name_display)
        val taskDetails = taskView.findViewById<TextView>(R.id.task_details)
        val taskCheckbox = taskCheckbox(taskView)
        val menuButton = taskView.findViewById<TextView>(R.id.task_menu_btn)
        val priorityIndicator = taskView.findViewById<View>(R.id.priorityIndicator)

        taskNameDisplay.text = task.name
        taskCheckbox.isChecked = task.isCompleted
        applyStrikethrough(taskNameDisplay, task.isCompleted)

        val colorRes = when (task.priority) {
            1 -> R.color.do_first
            2 -> R.color.schedule
            3 -> R.color.delegate
            else -> R.color.eliminate
        }
        priorityIndicator.setBackgroundColor(ContextCompat.getColor(this, colorRes))

        val priorityName = when (task.priority) {
            1 -> "Do first"
            2 -> "Schedule"
            3 -> "Delegate"
            else -> "Eliminate"
        }
        val dueDateStr = task.dueDate?.let { " • Due " + SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(it)) } ?: ""
        taskDetails.text = "$priorityName$dueDateStr"

        setupDynamicCheckBoxListener(taskCheckbox, taskNameDisplay, task.id)
        menuButton.setOnClickListener { showTaskMenu(task, it) }
        taskView.setOnClickListener { editTask(task) }

        tasksContainer.addView(taskView)
    }

    private fun taskCheckbox(taskView: View): CheckBox = taskView.findViewById(R.id.task_checkbox)

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

    private fun setupCheckBoxListener(checkBox: CheckBox, textView: TextView, taskIndex: Int) {
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            applyStrikethrough(textView, isChecked)
            lifecycleScope.launch {
                val taskId = if (taskIndex == 0) currentMainTaskId else currentSubtaskIds.getOrNull(taskIndex - 1)
                taskId?.let { 
                    taskRepository.updateTaskCompletion(it, isChecked) 
                    updateDoneBadge()
                }
            }
        }
    }

    private fun setupDynamicCheckBoxListener(checkBox: CheckBox, textView: TextView, taskId: Long) {
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            applyStrikethrough(textView, isChecked)
            lifecycleScope.launch {
                taskRepository.updateTaskCompletion(taskId, isChecked)
                updateDoneBadge()
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
        val options = arrayOf("Edit", "Delete")
        AlertDialog.Builder(this).setTitle("Task Options").setItems(options) { _, which ->
            when (which) {
                0 -> editTask(task)
                1 -> deleteTask(task)
            }
        }.show()
    }

    private fun editTask(task: Task) {
        val intent = Intent(this, AddTaskActivity::class.java).apply {
            putExtra("EDIT_TASK_ID", task.id)
            putExtra("EDIT_TASK_NAME", task.name)
            putExtra("EDIT_TASK_URGENT", task.isUrgent)
            putExtra("EDIT_TASK_IMPORTANT", task.isImportant)
            putExtra("EDIT_TASK_DUE_DATE", task.dueDate ?: 0L)
        }
        editTaskLauncher.launch(intent)
    }

    private fun deleteTask(task: Task) {
        AlertDialog.Builder(this)
            .setTitle("Delete Task")
            .setMessage("Are you sure you want to delete '${task.name}'?")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    taskRepository.deleteTaskById(task.id)
                    loadTasksFromDatabase()
                    Toast.makeText(this@MainActivity, "Task deleted", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
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

    private fun getSubtaskViews(index: Int): Pair<TextView, CheckBox> {
        return when (index) {
            0 -> Pair(minorTaskNameDisplay1, completeMinorTaskCheckBox1)
            1 -> Pair(minorTaskNameDisplay2, completeMinorTaskCheckBox2)
            else -> Pair(minorTaskNameDisplay3, completeMinorTaskCheckBox3)
        }
    }
}
