package com.example.todo

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddTaskActivity : AppCompatActivity() {

    private lateinit var taskNameEditText: EditText
    private lateinit var taskDescriptionEditText: EditText
    private lateinit var urgencyRadioGroup: RadioGroup
    private lateinit var importanceRadioGroup: RadioGroup
    private lateinit var dueDateTextView: TextView
    private lateinit var determinedCategoryTextView: TextView
    private lateinit var addTaskButton: Button
    private lateinit var tagUrgent: TextView
    private lateinit var tagImportant: TextView

    private val calendar: Calendar = Calendar.getInstance()
    private var selectedDueDate: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        taskNameEditText = findViewById(R.id.edit_text_task_name)
        taskDescriptionEditText = findViewById(R.id.description)
        urgencyRadioGroup = findViewById(R.id.radio_group_urgency)
        importanceRadioGroup = findViewById(R.id.radio_group_importance)
        dueDateTextView = findViewById(R.id.text_view_due_date)
        determinedCategoryTextView = findViewById(R.id.text_view_determined_category)
        addTaskButton = findViewById(R.id.button_add_task)
        tagUrgent = findViewById(R.id.tag_urgent)
        tagImportant = findViewById(R.id.tag_important)

        // Set up due date picker
        dueDateTextView.setOnClickListener {
            showDatePickerDialog()
        }

        // Set up listeners for radio groups to update the determined quadrant
        val updateQuadrantListener = RadioGroup.OnCheckedChangeListener { _, _ ->
            updateDeterminedQuadrant()
        }
        urgencyRadioGroup.setOnCheckedChangeListener(updateQuadrantListener)
        importanceRadioGroup.setOnCheckedChangeListener(updateQuadrantListener)

        // Set the initial quadrant text and tags
        updateDeterminedQuadrant()

        addTaskButton.setOnClickListener {
            saveTask()
        }
    }

    private fun updateDeterminedQuadrant() {
        val isUrgent = urgencyRadioGroup.checkedRadioButtonId == R.id.radio_urgent
        val isImportant = importanceRadioGroup.checkedRadioButtonId == R.id.radio_important

        val quadrantText = when {
            isUrgent && isImportant -> "Urgent and Important"
            isUrgent && !isImportant -> "Urgent and Not Important"
            !isUrgent && isImportant -> "Not Urgent and Important"
            else -> "Not Urgent and Not Important"
        }
        determinedCategoryTextView.text = quadrantText

        tagUrgent.visibility = if (isUrgent) View.VISIBLE else View.GONE
        tagImportant.visibility = if (isImportant) View.VISIBLE else View.GONE
    }

    private fun showDatePickerDialog() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, monthOfYear)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            selectedDueDate = calendar.timeInMillis
            updateDueDateInView()
        }

        DatePickerDialog(
            this,
            dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateDueDateInView() {
        val myFormat = "dd/MM/yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        dueDateTextView.text = sdf.format(calendar.time)
    }

    private fun saveTask() {
        val taskName = taskNameEditText.text.toString().trim()

        if (taskName.isEmpty()) {
            taskNameEditText.error = "Task name cannot be empty"
            taskNameEditText.requestFocus()
            return
        }

        val isUrgent = urgencyRadioGroup.checkedRadioButtonId == R.id.radio_urgent
        val isImportant = importanceRadioGroup.checkedRadioButtonId == R.id.radio_important

        val resultIntent = Intent()
        resultIntent.putExtra("TASK_NAME", taskName)
        resultIntent.putExtra("IS_URGENT", isUrgent)
        resultIntent.putExtra("IS_IMPORTANT", isImportant)
        resultIntent.putExtra("DUE_DATE", selectedDueDate)

        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
}
