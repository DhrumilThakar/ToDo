package com.example.todo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class AddTaskActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        // Correct IDs to match your layout file
        val taskInput = findViewById<EditText>(R.id.edit_text_task_name)
        val saveButton = findViewById<Button>(R.id.button_add_task)

        saveButton.setOnClickListener {
            val taskText = taskInput.text.toString()
            if (taskText.isNotEmpty()) {
                val result = Intent()
                result.putExtra("TASK_NAME", taskText)
                setResult(RESULT_OK, result)
                finish()
            }
        }
    }
}