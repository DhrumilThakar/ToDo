package com.example.todo // Your package name

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
import android.util.Log // For logging

class AddTaskActivity : AppCompatActivity() {

    private lateinit var taskNameEditText: EditText
    private lateinit var taskDescriptionEditText: EditText
    private lateinit var categoryRadioGroup: RadioGroup
    private lateinit var dueDateTextView: TextView
    private lateinit var subCategoryLayout: View // The LinearLayout for sub-categories
    private lateinit var subCategoryRadioGroup: RadioGroup
    private lateinit var addTaskButton: Button

    private val calendar: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task) // From your shared XML [1]

        taskNameEditText = findViewById(R.id.edit_text_task_name)
        taskDescriptionEditText = findViewById(R.id.description) // Assuming this is the ID
        categoryRadioGroup = findViewById(R.id.radio_group_category)
        dueDateTextView = findViewById(R.id.text_view_due_date)
        subCategoryLayout = findViewById(R.id.sub_category_layout)
        subCategoryRadioGroup = findViewById(R.id.radio_group_sub_category)
        addTaskButton = findViewById(R.id.button_add_task)

        // Set up due date picker
        dueDateTextView.setOnClickListener {
            showDatePickerDialog()
        }

        // Show/hide sub-category based on due date (example logic)
        // You might want more sophisticated logic for this
        // For simplicity, let's assume if a due date is selected and it's not "today",
        // then show sub-categories. This is a placeholder for your actual logic.
        // categoryRadioGroup.setOnCheckedChangeListener { group, checkedId ->
        //     // Potentially show/hide based on main category too
        // }


        addTaskButton.setOnClickListener {
            saveTask()
        }
    }

    private fun showDatePickerDialog() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, monthOfYear)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDueDateInView()

            // Example: Show sub-category if date is not today
            // This is a simple check, refine as needed.
            if (!isToday(calendar)) {
                subCategoryLayout.visibility = View.VISIBLE
            } else {
                subCategoryLayout.visibility = View.GONE
            }
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
        val myFormat = "dd/MM/yyyy" // Or "EEE, MMM d, yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        dueDateTextView.text = sdf.format(calendar.time)
    }

    private fun isToday(cal: Calendar): Boolean {
        val today = Calendar.getInstance()
        return today.get(Calendar.YEAR) == cal.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR)
    }


    private fun saveTask() {
        val taskName = taskNameEditText.text.toString().trim()
        val taskDescription = taskDescriptionEditText.text.toString().trim()

        if (taskName.isEmpty()) {
            taskNameEditText.error = "Task name cannot be empty"
            taskNameEditText.requestFocus()
            return
        }

        val selectedCategoryId = categoryRadioGroup.checkedRadioButtonId
        val category = when (selectedCategoryId) {
            R.id.radio_major -> "Major"
            R.id.radio_minor -> "Minor"
            R.id.radio_other -> "Other"
            else -> "" // Should not happen if one is checked by default
        }

//        val dueDate = if (dueDateTextView.text.toString() != getString(R.string.select_due_date_default_text)) { // Assuming you have a default string
//            dueDateTextView.text.toString()
//        } else {
//            null
//        }
        // If your default text in XML is "Select Due Date" directly, use that:
         val dueDate = if(dueDateTextView.text.toString() != "Select Due Date") {
             dueDateTextView.text.toString()
         }
        else
         {
             null
         }


        var subCategory: String? = null
        if (subCategoryLayout.visibility == View.VISIBLE) {
            val selectedSubCategoryId = subCategoryRadioGroup.checkedRadioButtonId
            subCategory = when (selectedSubCategoryId) {
                R.id.radio_urgent_important -> "Urgent and Important"
                R.id.radio_urgent_not_important -> "Urgent and Not Important"
                R.id.radio_not_urgent_important -> "Not Urgent and Important"
                R.id.radio_not_urgent_not_important -> "Not Urgent and Not Important"
                else -> null
            }
        }


        val resultIntent = Intent()
        resultIntent.putExtra("TASK_NAME", taskName)
        resultIntent.putExtra("TASK_DESCRIPTION", taskDescription)
        resultIntent.putExtra("TASK_CATEGORY", category)
        resultIntent.putExtra("TASK_DUE_DATE", dueDate)
        subCategory?.let { resultIntent.putExtra("TASK_SUB_CATEGORY", it) }

        setResult(Activity.RESULT_OK, resultIntent)
        finish() // Close AddTaskActivity and return to MainActivity
    }
}
