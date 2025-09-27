package com.example.todo // Make sure this matches your package name

import android.app.Activity
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    // Main Task Views
    private lateinit var addMainTaskBtn: ImageButton
    private lateinit var mainTaskNameDisplay: TextView
    private lateinit var completeMainTaskCheckBox: CheckBox

    // Minor Task Views
    private lateinit var addMinorTaskBtn1: ImageButton
    private lateinit var minorTaskNameDisplay1: TextView
    private lateinit var completeMinorTaskCheckBox1: CheckBox
    private lateinit var addMinorTaskBtn2: ImageButton
    private lateinit var minorTaskNameDisplay2: TextView
    private lateinit var completeMinorTaskCheckBox2: CheckBox
    private lateinit var addMinorTaskBtn3: ImageButton
    private lateinit var minorTaskNameDisplay3: TextView
    private lateinit var completeMinorTaskCheckBox3: CheckBox

    // List Item Task Views (in ScrollView)
    private lateinit var addTaskBtn1: ImageButton
    private lateinit var taskNameDisplay1: TextView
    private lateinit var taskCheckbox1: CheckBox
    private lateinit var addTaskBtn2: ImageButton
    private lateinit var taskNameDisplay2: TextView
    private lateinit var taskCheckbox2: CheckBox
    private lateinit var addTaskBtn3: ImageButton
    private lateinit var taskNameDisplay3: TextView
    private lateinit var taskCheckbox3: CheckBox
    private lateinit var addTaskBtn4: ImageButton
    private lateinit var taskNameDisplay4: TextView
    private lateinit var taskCheckbox4: CheckBox
    private lateinit var addTaskBtn5: ImageButton
    private lateinit var taskNameDisplay5: TextView
    private lateinit var taskCheckbox5: CheckBox

    // Navigation buttons
    private lateinit var homeButton: ImageButton
    private lateinit var calendarButton: ImageButton
    private lateinit var bambooButton: ImageButton


    // To identify which "+" button was clicked
    private var clickedTaskSourceTag: String? = null

    private val addTaskLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { data ->
                val taskName = data.getStringExtra("TASK_NAME")
                // val taskCategory = data.getStringExtra("TASK_CATEGORY") // Retrieve if needed

                if (!taskName.isNullOrBlank()) {
                    Log.d("MainActivity", "Received task: $taskName for source: $clickedTaskSourceTag")
                    when (clickedTaskSourceTag) {
                        "main_task" -> {
                            updateTaskDisplay(mainTaskNameDisplay, taskName, addMainTaskBtn, completeMainTaskCheckBox)
                        }
                        "minor_task_1" -> {
                            updateTaskDisplay(minorTaskNameDisplay1, taskName, addMinorTaskBtn1, completeMinorTaskCheckBox1)
                        }
                        "minor_task_2" -> {
                            updateTaskDisplay(minorTaskNameDisplay2, taskName, addMinorTaskBtn2, completeMinorTaskCheckBox2)
                        }
                        "minor_task_3" -> {
                            updateTaskDisplay(minorTaskNameDisplay3, taskName, addMinorTaskBtn3, completeMinorTaskCheckBox3)
                        }
                        "list_item_1" -> {
                            updateTaskDisplay(taskNameDisplay1, taskName, addTaskBtn1, taskCheckbox1)
                        }
                        "list_item_2" -> {
                            updateTaskDisplay(taskNameDisplay2, taskName, addTaskBtn2, taskCheckbox2)
                        }
                        "list_item_3" -> {
                            updateTaskDisplay(taskNameDisplay3, taskName, addTaskBtn3, taskCheckbox3)
                        }
                        "list_item_4" -> {
                            updateTaskDisplay(taskNameDisplay4, taskName, addTaskBtn4, taskCheckbox4)
                        }
                        "list_item_5" -> {
                            updateTaskDisplay(taskNameDisplay5, taskName, addTaskBtn5, taskCheckbox5)
                        }
                        else -> {
                            Log.w("MainActivity", "Task added without a specific source tag or tag not recognized.")
                            // Optionally, implement a fallback if needed, e.g., find the first available slot.
                        }
                    }
                } else {
                    Log.w("MainActivity", "Received null or blank task name.")
                }
            }
        }
        clickedTaskSourceTag = null // Reset tag after use
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

        // Initialize List Item Task Views
        addTaskBtn1 = findViewById(R.id.add_task_btn_1)
        taskNameDisplay1 = findViewById(R.id.task_name_display_1)
        taskCheckbox1 = findViewById(R.id.task_checkbox_1)
        addTaskBtn2 = findViewById(R.id.add_task_btn_2)
        taskNameDisplay2 = findViewById(R.id.task_name_display_2)
        taskCheckbox2 = findViewById(R.id.task_checkbox_2)
        addTaskBtn3 = findViewById(R.id.add_task_btn_3)
        taskNameDisplay3 = findViewById(R.id.task_name_display_3)
        taskCheckbox3 = findViewById(R.id.task_checkbox_3)
        addTaskBtn4 = findViewById(R.id.add_task_btn_4)
        taskNameDisplay4 = findViewById(R.id.task_name_display_4)
        taskCheckbox4 = findViewById(R.id.task_checkbox_4)
        addTaskBtn5 = findViewById(R.id.add_task_btn_5)
        taskNameDisplay5 = findViewById(R.id.task_name_display_5)
        taskCheckbox5 = findViewById(R.id.task_checkbox_5)

        // Initialize Navigation Buttons
        homeButton = findViewById(R.id.home)
        calendarButton = findViewById(R.id.calendar)
        bambooButton = findViewById(R.id.bamboo)


        // Setup click listeners for all "+" buttons
        addMainTaskBtn.setOnClickListener { startAddTaskActivity("main_task") }
        addMinorTaskBtn1.setOnClickListener { startAddTaskActivity("minor_task_1") }
        addMinorTaskBtn2.setOnClickListener { startAddTaskActivity("minor_task_2") }
        addMinorTaskBtn3.setOnClickListener { startAddTaskActivity("minor_task_3") }

        addTaskBtn1.setOnClickListener { startAddTaskActivity("list_item_1") }
        addTaskBtn2.setOnClickListener { startAddTaskActivity("list_item_2") }
        addTaskBtn3.setOnClickListener { startAddTaskActivity("list_item_3") }
        addTaskBtn4.setOnClickListener { startAddTaskActivity("list_item_4") }
        addTaskBtn5.setOnClickListener { startAddTaskActivity("list_item_5") }

        // Setup CheckBox Listeners for strikethrough effect
        setupCheckBoxListener(completeMainTaskCheckBox, mainTaskNameDisplay)
        setupCheckBoxListener(completeMinorTaskCheckBox1, minorTaskNameDisplay1)
        setupCheckBoxListener(completeMinorTaskCheckBox2, minorTaskNameDisplay2)
        setupCheckBoxListener(completeMinorTaskCheckBox3, minorTaskNameDisplay3)
        setupCheckBoxListener(taskCheckbox1, taskNameDisplay1)
        setupCheckBoxListener(taskCheckbox2, taskNameDisplay2)
        setupCheckBoxListener(taskCheckbox3, taskNameDisplay3)
        setupCheckBoxListener(taskCheckbox4, taskNameDisplay4)
        setupCheckBoxListener(taskCheckbox5, taskNameDisplay5)


        // Navigation Listeners
        homeButton.setOnClickListener {
            Log.d("Navigation", "Home clicked")
            // Example: Refresh MainActivity or go to a defined "home state"
            Toast.makeText(this, "Home Clicked", Toast.LENGTH_SHORT).show()
            // To truly refresh, you might restart the activity:
            // val intent = Intent(this, MainActivity::class.java)
            // intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            // startActivity(intent)
            // finish()
        }

        calendarButton.setOnClickListener {
            Log.d("Navigation", "Calendar clicked")
            // Ensure you have CalendarActivity.kt and it's declared in AndroidManifest.xml
            val intent = Intent(this, ActivityQuadrent::class.java)
            startActivity(intent)
        }

        bambooButton.setOnClickListener {
            Log.d("Navigation", "Bamboo/List Check clicked")
            // For now, just a Toast. Replace with Intent if you create BambooActivity.
            Toast.makeText(this, "List Check (Bamboo) Clicked", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startAddTaskActivity(sourceTag: String) {
        clickedTaskSourceTag = sourceTag // Store which button/task source initiated this
        val intent = Intent(this, AddTaskActivity::class.java)
        // Optionally pass the sourceTag to AddTaskActivity if it needs to customize its behavior
        // intent.putExtra("SOURCE_TAG", sourceTag)
        Log.d("MainActivity", "Starting AddTaskActivity for source: $sourceTag")
        addTaskLauncher.launch(intent)
    }

    private fun updateTaskDisplay(textView: TextView, taskName: String, addButton: ImageButton, checkBox: CheckBox) {
        textView.text = taskName
        textView.visibility = View.VISIBLE
        checkBox.isChecked = false // Ensure checkbox is unchecked when a new task is added
        checkBox.visibility = View.VISIBLE
        addButton.visibility = View.GONE // Hide the "+" button for this slot
        Log.i("MainActivity", "Updated display for slot. TextView: ${resources.getResourceEntryName(textView.id)}, Task: $taskName")
    }

    private fun setupCheckBoxListener(checkBox: CheckBox, textView: TextView) {
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                textView.paintFlags = textView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                Log.d("Checkbox", "Task '${textView.text}' marked complete.")
            } else {
                textView.paintFlags = textView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                Log.d("Checkbox", "Task '${textView.text}' marked incomplete.")
            }
            // Future: Here you would also update your data model and persist the change if needed.
        }
    }
}
