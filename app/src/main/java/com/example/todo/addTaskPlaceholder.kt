package com.example.todo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.semantics.text
//import androidx.glance.visibility
import com.google.android.material.textfield.TextInputEditText // Assuming you use this for input

import com.example.todo.R

class addTaskPlaceholder : AppCompatActivity() {

    private lateinit var tasksContainer: LinearLayout
    private lateinit var addTaskPlaceholder: LinearLayout
    private lateinit var addTaskDivider: View

    // A counter for unique IDs if you need them, or use a proper data model
    private var taskCounter = 0

    // This would be the request code for starting your "add task" activity/dialog
    private val ADD_TASK_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Ensure this matches your XML file name

        tasksContainer = findViewById(R.id.tasksContainer)
        addTaskPlaceholder = findViewById(R.id.addTaskPlaceholder)
        addTaskDivider = findViewById(R.id.addTaskDivider)

        addTaskPlaceholder.setOnClickListener {
            // Option 1: Launch a new Activity to get task details
            // val intent = Intent(this, AddTaskActivity::class.java)
            // startActivityForResult(intent, ADD_TASK_REQUEST_CODE)

            // Option 2: Show a DialogFragment to get task details
            // val dialog = AddTaskDialogFragment()
            // dialog.show(supportFragmentManager, "AddTaskDialog")

            // For this example, let's simulate getting a task name directly
            // In a real app, you'd get this from the new Activity/Dialog
            showAddTaskDialog()
        }
    }

    private fun showAddTaskDialog() {
        // This is a simplified example of how you might get task input.
        // You'd typically use a proper AlertDialog with an EditText.
        val editText = TextInputEditText(this)
        editText.hint = "Enter task name"

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("New Task")
            .setView(editText)
            .setPositiveButton("Add") { dialog, _ ->
                val taskName = editText.text.toString()
                if (taskName.isNotBlank()) {
                    addNewTaskView(taskName)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }


    // Call this method when you have the new task details (e.g., from onActivityResult or a dialog callback)
    private fun addNewTaskView(taskName: String) {
        val inflater = LayoutInflater.from(this)
        // Inflate your custom layout for a single task item
        // Let's assume you create a new XML file for this: R.layout.list_item_task.xml
        val taskView = inflater.inflate(R.layout.list_item_task, tasksContainer, false)

        val taskNameDisplay = taskView.findViewById<TextView>(R.id.task_name_display)
        val taskCheckbox = taskView.findViewById<CheckBox>(R.id.task_checkbox)
        // Potentially an edit or delete button if you have them in list_item_task.xml
        // val editButton = taskView.findViewById<ImageButton>(R.id.edit_task_btn)
        // val deleteButton = taskView.findViewById<ImageButton>(R.id.delete_task_btn)


        taskNameDisplay.text = taskName
        taskNameDisplay.visibility = View.VISIBLE
        taskCheckbox.visibility = View.VISIBLE
        // Configure checkbox listener, edit/delete listeners etc.

        // --- Crucial part: Adding the new task and repositioning the "Add Task" placeholder ---
        // 1. Remove the "Add Task" placeholder and its divider
        tasksContainer.removeView(addTaskPlaceholder)
        tasksContainer.removeView(addTaskDivider)

        // 2. Add the new task view
        tasksContainer.addView(taskView)

        // 3. Add a new divider below the task (optional, for visual separation)
        val newDivider = View(this)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            (1 * resources.displayMetrics.density).toInt() // height in pixels (1dp equivalent, consider converting dp to px)
        )
        // You might want to convert dp to pixels for consistency
        val marginTopInDp = 0 // Or some other value like 4dp
        val marginTopInPx = (marginTopInDp * resources.displayMetrics.density).toInt()
        layoutParams.setMargins(0, marginTopInPx, 0, 0)
        newDivider.layoutParams = layoutParams
        newDivider.setBackgroundColor(resources.getColor(R.color.task_divider_color, null)) // Define this color
        tasksContainer.addView(newDivider)


        // 4. Re-add the "Add Task" placeholder and its divider at the bottom
        tasksContainer.addView(addTaskPlaceholder)
        tasksContainer.addView(addTaskDivider)

        taskCounter++
    }

    // If you use startActivityForResult:
    /*
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_TASK_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val taskName = data?.getStringExtra("TASK_NAME_EXTRA")
            if (!taskName.isNullOrEmpty()) {
                addNewTaskView(taskName)
            }
        }
    }
    */

    // If you use a DialogFragment, you'd typically have an interface callback from the DialogFragment to the Activity.
}
