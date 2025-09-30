package com.example.todo // Make sure this matches your package name

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
import androidx.activity.result.ActivityResultLauncher // Import this
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.todo.AddTaskActivity
import com.example.todo.ActivityQuadrent
//import com.example.todo.addTaskPlaceholder
// Remove: import androidx.appcompat.app.AlertDialog
// Remove: import com.google.android.material.textfield.TextInputEditText

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

    // --- Views for dynamic task list ---
    private lateinit var tasksContainer: LinearLayout
    private lateinit var addTaskPlaceholder: LinearLayout
    private lateinit var addTaskDivider: View

    // Navigation buttons
    private lateinit var homeButton: ImageButton
    private lateinit var calendarButton: ImageButton
    private lateinit var bambooButton: ImageButton

    // To identify which "+" button was clicked (for fixed main/minor tasks)
    private var clickedFixedTaskSourceTag: String? = null

    // Launcher for getting results from AddTaskActivity (for FIXED main/minor tasks)
    private val addFixedTaskLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { data ->
                val taskName = data.getStringExtra("TASK_NAME")
                if (!taskName.isNullOrBlank()) {
                    Log.d("MainActivity", "Received task: $taskName for fixed source: $clickedFixedTaskSourceTag")
                    when (clickedFixedTaskSourceTag) {
                        "main_task" -> {
                            updateFixedTaskDisplay(mainTaskNameDisplay, taskName, addMainTaskBtn, completeMainTaskCheckBox)
                        }
                        "minor_task_1" -> {
                            updateFixedTaskDisplay(minorTaskNameDisplay1, taskName, addMinorTaskBtn1, completeMinorTaskCheckBox1)
                        }
                        "minor_task_2" -> {
                            updateFixedTaskDisplay(minorTaskNameDisplay2, taskName, addMinorTaskBtn2, completeMinorTaskCheckBox2)
                        }
                        "minor_task_3" -> {
                            updateFixedTaskDisplay(minorTaskNameDisplay3, taskName, addMinorTaskBtn3, completeMinorTaskCheckBox3)
                        }
                        else -> {
                            Log.w("MainActivity", "Fixed task source tag not recognized: $clickedFixedTaskSourceTag")
                        }
                    }
                } else {
                    Log.w("MainActivity", "Received null or blank task name for fixed tasks.")
                }
            }
        }
        clickedFixedTaskSourceTag = null // Reset tag after use
    }

    // --- NEW: Launcher for getting results from AddTaskActivity for the DYNAMIC LIST ---
    private val addDynamicTaskLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.let { data ->
                    val taskName = data.getStringExtra("TASK_NAME")
                    if (!taskName.isNullOrBlank()) {
                        Log.d("MainActivity", "Received task for dynamic list: $taskName")
                        addNewTaskViewToDynamicList(taskName)
                    } else {
                        Log.w("MainActivity", "Received null or blank task name for dynamic list.")
                    }
                }
            }
        }
    // --- END NEW LAUNCHER ---

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

        // Initialize dynamic task list views
        tasksContainer = findViewById(R.id.tasksContainer)
        addTaskPlaceholder = findViewById(R.id.addTaskPlaceholder)
        addTaskDivider = findViewById(R.id.addTaskDivider)

        // --- MODIFIED: addTaskPlaceholder click listener ---
        addTaskPlaceholder.setOnClickListener {
            Log.d("MainActivity", "Add New Task placeholder clicked, launching AddTaskActivity for dynamic list.")
            val intent = Intent(this, AddTaskActivity::class.java)
            // Optionally, you can pass an extra to AddTaskActivity if it needs to know
            // it's being called for the dynamic list vs a fixed slot.
            // intent.putExtra("TASK_TYPE", "dynamic_list_item")
            addDynamicTaskLauncher.launch(intent)
        }
        // --- END MODIFICATION ---

        // Initialize Navigation Buttons
        homeButton = findViewById(R.id.home)
        calendarButton = findViewById(R.id.calendar)
        bambooButton = findViewById(R.id.bamboo)

        // Setup click listeners for fixed "+" buttons (main/minor tasks)
        addMainTaskBtn.setOnClickListener { startAddTaskActivityForFixedTask("main_task") }
        addMinorTaskBtn1.setOnClickListener { startAddTaskActivityForFixedTask("minor_task_1") }
        addMinorTaskBtn2.setOnClickListener { startAddTaskActivityForFixedTask("minor_task_2") }
        addMinorTaskBtn3.setOnClickListener { startAddTaskActivityForFixedTask("minor_task_3") }

        // Setup CheckBox Listeners for strikethrough effect (for fixed tasks)
        setupCheckBoxListener(completeMainTaskCheckBox, mainTaskNameDisplay)
        setupCheckBoxListener(completeMinorTaskCheckBox1, minorTaskNameDisplay1)
        setupCheckBoxListener(completeMinorTaskCheckBox2, minorTaskNameDisplay2)
        setupCheckBoxListener(completeMinorTaskCheckBox3, minorTaskNameDisplay3)

        // Navigation Listeners
        homeButton.setOnClickListener {
            Log.d("Navigation", "Home clicked")
            Toast.makeText(this, "Home Clicked", Toast.LENGTH_SHORT).show()
        }

        calendarButton.setOnClickListener {
            Log.d("Navigation", "Calendar clicked")
            val intent = Intent(this, ActivityQuadrent::class.java)
            startActivity(intent)
        }

        bambooButton.setOnClickListener {
            Log.d("Navigation", "Bamboo/List Check clicked")
            Toast.makeText(this, "List Check (Bamboo) Clicked", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startAddTaskActivityForFixedTask(sourceTag: String) {
        clickedFixedTaskSourceTag = sourceTag // Use the renamed variable
        val intent = Intent(this, AddTaskActivity::class.java)
        // Optionally pass the sourceTag to AddTaskActivity if it needs to customize its behavior
        intent.putExtra("SOURCE_TAG", sourceTag) // Good practice to inform AddTaskActivity
        Log.d("MainActivity", "Starting AddTaskActivity for fixed task source: $sourceTag")
        addFixedTaskLauncher.launch(intent) // Use the renamed launcher
    }

    private fun updateFixedTaskDisplay(textView: TextView, taskName: String, addButton: ImageButton, checkBox: CheckBox) {
        textView.text = taskName
        textView.visibility = View.VISIBLE
        checkBox.isChecked = false
        checkBox.visibility = View.VISIBLE
        addButton.visibility = View.GONE
        Log.i("MainActivity", "Updated display for fixed slot. TextView: ${resources.getResourceEntryName(textView.id)}, Task: $taskName")
    }

    // REMOVED: showAddTaskDialogForDynamicList() as we are now using AddTaskActivity

    private fun addNewTaskViewToDynamicList(taskName: String) {
        val inflater = LayoutInflater.from(this)
        val taskView = inflater.inflate(R.layout.list_item_task, tasksContainer, false)

        val taskNameDisplay = taskView.findViewById<TextView>(R.id.task_name_display)
        val taskCheckbox = taskView.findViewById<CheckBox>(R.id.task_checkbox)

        taskNameDisplay.text = taskName
        taskCheckbox.isChecked = false
        setupCheckBoxListener(taskCheckbox, taskNameDisplay)

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
        newDivider.setBackgroundColor(getColor(R.color.task_divider_color)) // Make sure R.color.task_divider_color is defined
        newDivider.layoutParams = layoutParams
        tasksContainer.addView(newDivider)

        tasksContainer.addView(addTaskPlaceholder)
        tasksContainer.addView(addTaskDivider)

        Log.i("MainActivity", "Added new dynamic task: $taskName")
    }

    private fun setupCheckBoxListener(checkBox: CheckBox, textView: TextView) {
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                textView.paintFlags = textView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                textView.paintFlags = textView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
            Log.d("Checkbox", "Task '${textView.text}' status changed to: ${if (isChecked) "complete" else "incomplete"}")
        }
    }
}
