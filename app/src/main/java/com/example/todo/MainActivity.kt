package com.example.todo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var addNewTaskButton: LinearLayout
    private lateinit var taskContainer: LinearLayout

    // This is the modern way to handle activity results
    private val addTaskLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val taskName = result.data?.getStringExtra("TASK_NAME") ?: return@registerForActivityResult

            // Inflate task_item.xml and add it to the container
            val taskView = LayoutInflater.from(this).inflate(R.layout.task_item, taskContainer, false)
            val taskText = taskView.findViewById<TextView>(R.id.taskText)
            val taskRadio = taskView.findViewById<RadioButton>(R.id.taskRadio)

            taskText.text = taskName
            taskContainer.addView(taskView)

            // Mark task as complete when the radio button is checked
            taskRadio.setOnCheckedChangeListener { _, isChecked ->
                taskText.paint.isStrikeThruText = isChecked
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        addNewTaskButton = findViewById(R.id.add_new_task_rectangle)
        taskContainer = findViewById(R.id.taskContainer)

        // Launch the AddTaskActivity when the button is clicked
        addNewTaskButton.setOnClickListener {
            val intent = Intent(this, AddTaskActivity::class.java)
            addTaskLauncher.launch(intent)
        }
    }
}