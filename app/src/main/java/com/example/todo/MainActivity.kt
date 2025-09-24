package com.example.todo // Your package name

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout // Keep this for taskContainer if you manually add views
import android.widget.RadioButton
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
// import androidx.compose.ui.semantics.text // REMOVED - Unused import
import androidx.recyclerview.widget.RecyclerView

// Simple data class for a Task
data class Task(
    val id: Long = System.currentTimeMillis(),
    val name: String,
    val description: String?,
    val category: String?,
    val dueDate: String?,
    val subCategory: String?,
    var isCompleted: Boolean = false,
    val taskType: String = "list_item"
)

// RecyclerView Adapter - This adapter is here but might not be used if R.id.taskContainer
// is your only list-like section and it's a LinearLayout.
class TaskAdapter(
    private val tasks: MutableList<Task>,
    private val onTaskClicked: (Task) -> Unit,
    private val onTaskCheckedChanged: (Task, Boolean) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val taskNameTextView: TextView = itemView.findViewById(R.id.taskText)
        val taskRadioButton: RadioButton = itemView.findViewById(R.id.taskRadio)

        fun bind(task: Task) {
            taskNameTextView.text = task.name
            taskRadioButton.isChecked = task.isCompleted
            taskNameTextView.paint.isStrikeThruText = task.isCompleted

            itemView.setOnClickListener { onTaskClicked(task) }
            taskRadioButton.setOnCheckedChangeListener { _, isChecked ->
                taskNameTextView.paint.isStrikeThruText = isChecked
                onTaskCheckedChanged(task, isChecked)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.task_item, parent, false) // Ensure R.layout.task_item exists
        return TaskViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(tasks[position])
    }

    override fun getItemCount() = tasks.size

    fun addTask(task: Task) {
        tasks.add(0, task)
        notifyItemInserted(0)
    }

    fun updateTask(updatedTask: Task) {
        val index = tasks.indexOfFirst { it.id == updatedTask.id }
        if (index != -1) {
            tasks[index] = updatedTask
            notifyItemChanged(index)
        }
    }
}


class MainActivity : AppCompatActivity() {

    private lateinit var addMainTaskBtn: ImageButton
    private lateinit var addMinorTaskBtn1: ImageButton
    private lateinit var addMinorTaskBtn2: ImageButton
    private lateinit var addMinorTaskBtn3: ImageButton
    private lateinit var addTaskBtn1: ImageButton
    private lateinit var addTaskBtn2: ImageButton
    private lateinit var addTaskBtn3: ImageButton
    private lateinit var addTaskBtn4: ImageButton
    private lateinit var addTaskBtn5: ImageButton

    private lateinit var taskContainerLayout: LinearLayout

    private var currentTaskTypeToAdd: String? = null

    private val addTaskLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { data ->
                val taskName = data.getStringExtra("TASK_NAME")
                val taskDescription = data.getStringExtra("TASK_DESCRIPTION")
                // Add other extras: TASK_CATEGORY, TASK_DUE_DATE, TASK_SUB_CATEGORY
                val taskCategory = data.getStringExtra("TASK_CATEGORY")
                val taskDueDate = data.getStringExtra("TASK_DUE_DATE")
                val taskSubCategory = data.getStringExtra("TASK_SUB_CATEGORY")


                if (taskName != null) {
                    val newTask = Task(
                        name = taskName,
                        description = taskDescription,
                        category = taskCategory,
                        dueDate = taskDueDate,
                        subCategory = taskSubCategory,
                        taskType = currentTaskTypeToAdd ?: "placeholder"
                    )

                    Log.d("MainActivity", "New task received: $newTask, for type: $currentTaskTypeToAdd")

                    when (currentTaskTypeToAdd) {
                        "main_task" -> {
                            // Find the TextView associated with main_task and update it.
                            // Example: findViewById<TextView>(R.id.main_task_name_display).text = newTask.name
                            Log.i("MainActivity", "Main task added/updated: ${newTask.name}")
                        }
                        "minor_task_1" -> {
                            // Example: findViewById<TextView>(R.id.minor_task_1_name_display).text = newTask.name
                            Log.i("MainActivity", "Minor task 1 added/updated: ${newTask.name}")
                        }
                        "minor_task_2" -> {
                            Log.i("MainActivity", "Minor task 2 added/updated: ${newTask.name}")
                        }
                        "minor_task_3" -> {
                            Log.i("MainActivity", "Minor task 3 added/updated: ${newTask.name}")
                        }
                        "list_item_1" -> {
                            updateTaskNameInContainer(addTaskBtn1.parent as View, newTask.name)
                            Log.i("MainActivity", "Task for item 1: ${newTask.name}")
                        }
                        "list_item_2" -> {
                            updateTaskNameInContainer(addTaskBtn2.parent as View, newTask.name)
                            Log.i("MainActivity", "Task for item 2: ${newTask.name}")
                        }
                        "list_item_3" -> {
                            updateTaskNameInContainer(addTaskBtn3.parent as View, newTask.name)
                            Log.i("MainActivity", "Task for item 3: ${newTask.name}")
                        }
                        "list_item_4" -> {
                            updateTaskNameInContainer(addTaskBtn4.parent as View, newTask.name)
                            Log.i("MainActivity", "Task for item 4: ${newTask.name}")
                        }
                        "list_item_5" -> {
                            updateTaskNameInContainer(addTaskBtn5.parent as View, newTask.name)
                            Log.i("MainActivity", "Task for item 5: ${newTask.name}")
                        }
                        else -> {
                            Log.w("MainActivity", "Unhandled task type or generic task added: ${newTask.name}")
                        }
                    }
                    currentTaskTypeToAdd = null
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        addMainTaskBtn = findViewById(R.id.add_main_task_btn)
        addMinorTaskBtn1 = findViewById(R.id.add_minor_task_btn_1)
        addMinorTaskBtn2 = findViewById(R.id.add_minor_task_btn_2)
        addMinorTaskBtn3 = findViewById(R.id.add_minor_task_btn_3)

        addTaskBtn1 = findViewById(R.id.add_task_btn_1)
        addTaskBtn2 = findViewById(R.id.add_task_btn_2)
        addTaskBtn3 = findViewById(R.id.add_task_btn_3)
        addTaskBtn4 = findViewById(R.id.add_task_btn_4)
        addTaskBtn5 = findViewById(R.id.add_task_btn_5)

        taskContainerLayout = findViewById(R.id.taskContainer)

        Log.i("MainActivity", "R.id.taskContainer is a LinearLayout. Views inside it will be managed manually or are static.")

        val addButtons = listOf(
            addMainTaskBtn to "main_task",
            addMinorTaskBtn1 to "minor_task_1",
            addMinorTaskBtn2 to "minor_task_2",
            addMinorTaskBtn3 to "minor_task_3",
            addTaskBtn1 to "list_item_1",
            addTaskBtn2 to "list_item_2",
            addTaskBtn3 to "list_item_3",
            addTaskBtn4 to "list_item_4",
            addTaskBtn5 to "list_item_5"
        )

        for ((button, taskType) in addButtons) {
            button.setOnClickListener {
                Log.d("MainActivity", "Add button clicked for type: $taskType")
                currentTaskTypeToAdd = taskType
                val intent = Intent(this, AddTaskActivity::class.java)
                addTaskLauncher.launch(intent)
            }
        }

        findViewById<ImageButton>(R.id.home).setOnClickListener { /* Handle home click */ }
        findViewById<ImageButton>(R.id.calendar).setOnClickListener { /* Handle calendar click */ }
        findViewById<ImageButton>(R.id.bamboo).setOnClickListener { /* Handle bamboo click */ }

        loadInitialData()
    }

    private fun loadInitialData() {
        // Load any existing tasks or setup initial UI states
    }

    // Helper function to update or add a TextView for task name in the LinearLayout items
    private fun updateTaskNameInContainer(itemView: View, taskName: String) {
        // itemView is the LinearLayout containing the ImageButton (add_task_btn_X)
        // We need to find or add a TextView within this itemView
        var taskNameTextView = itemView.findViewWithTag<TextView>("taskNameTag")

        if (taskNameTextView == null && itemView is ViewGroup) {
            taskNameTextView = TextView(this).apply {
                tag = "taskNameTag" // Tag to find it later
                text = taskName
                setTextColor(resources.getColor(android.R.color.white, null)) // Example color
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginStart = 16 // Add some margin
                }
            }
            itemView.addView(taskNameTextView)
        } else {
            taskNameTextView?.text = taskName
        }
    }


    // If you need to dynamically add more complex task views to the taskContainerLayout:
    private fun addTaskViewToContainer(task: Task) {
        // Example: Inflate a simple layout for the task and add it to taskContainerLayout
        // This is more advanced and requires a separate layout file (e.g., simple_task_view_for_container.xml)
        // val taskView = LayoutInflater.from(this).inflate(R.layout.simple_task_view_for_container, taskContainerLayout, false)
        // val taskNameTextView = taskView.findViewById<TextView>(R.id.taskName) // Assuming ID in the inflated layout
        // taskNameTextView.text = task.name
        // taskContainerLayout.addView(taskView)
    }
}
