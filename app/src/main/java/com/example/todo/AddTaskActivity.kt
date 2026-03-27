package com.example.todo

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.todo.utils.NotificationHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddTaskActivity : AppCompatActivity() {

    private lateinit var taskNameEditText: EditText
    private lateinit var taskDescriptionEditText: EditText
    private lateinit var dueDateTextView: TextView
    private lateinit var addTaskButton: Button
    private lateinit var cancelButton: TextView

    // Urgency Buttons
    private lateinit var btnNotUrgent: TextView
    private lateinit var btnUrgent: TextView

    // Importance Buttons
    private lateinit var btnNotImportant: TextView
    private lateinit var btnImportant: TextView

    // Reminder Buttons
    private lateinit var btnReminderNone: TextView
    private lateinit var btnReminder1Hr: TextView
    private lateinit var btnReminder1Day: TextView

    // Quadrant Detected Views
    private lateinit var quadrantDetectedCard: View
    private lateinit var detectedQuadrantTitle: TextView
    private lateinit var detectedQuadrantDesc: TextView

    private val calendar: Calendar = Calendar.getInstance()
    private var selectedDueDate: Long = 0L

    private var isUrgent: Boolean = false
    private var isImportant: Boolean = false
    private var selectedReminder: Int = 0 // 0: None, 1: 1hr, 2: 1day

    private var editingTaskId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        initializeViews()
        setupListeners()

        // Check if we're editing an existing task
        editingTaskId = intent.getLongExtra("EDIT_TASK_ID", -1L)
        if (editingTaskId != -1L) {
            loadTaskData()
        }

        updateUI()
    }

    private fun initializeViews() {
        taskNameEditText = findViewById(R.id.edit_text_task_name)
        taskDescriptionEditText = findViewById(R.id.description)
        dueDateTextView = findViewById(R.id.text_view_due_date)
        addTaskButton = findViewById(R.id.button_add_task)
        cancelButton = findViewById(R.id.cancelButton)

        btnNotUrgent = findViewById(R.id.btn_not_urgent)
        btnUrgent = findViewById(R.id.btn_urgent)

        btnNotImportant = findViewById(R.id.btn_not_important)
        btnImportant = findViewById(R.id.btn_important)

        btnReminderNone = findViewById(R.id.reminder_none)
        btnReminder1Hr = findViewById(R.id.reminder_1hr)
        btnReminder1Day = findViewById(R.id.reminder_1day)

        quadrantDetectedCard = findViewById(R.id.quadrantDetectedCard)
        detectedQuadrantTitle = findViewById(R.id.detectedQuadrantTitle)
        detectedQuadrantDesc = findViewById(R.id.detectedQuadrantDesc)
    }

    private fun setupListeners() {
        cancelButton.setOnClickListener { finish() }

        btnNotUrgent.setOnClickListener { 
            isUrgent = false
            updateUI()
        }
        btnUrgent.setOnClickListener { 
            isUrgent = true
            updateUI()
        }

        btnNotImportant.setOnClickListener { 
            isImportant = false
            updateUI()
        }
        btnImportant.setOnClickListener { 
            isImportant = true
            updateUI()
        }

        btnReminderNone.setOnClickListener { 
            selectedReminder = 0
            updateUI()
        }
        btnReminder1Hr.setOnClickListener { 
            selectedReminder = 1
            updateUI()
        }
        btnReminder1Day.setOnClickListener { 
            selectedReminder = 2
            updateUI()
        }

        dueDateTextView.setOnClickListener { showDatePickerDialog() }

        addTaskButton.setOnClickListener { saveTask() }
    }

    private fun loadTaskData() {
        val taskName = intent.getStringExtra("EDIT_TASK_NAME")
        isUrgent = intent.getBooleanExtra("EDIT_TASK_URGENT", false)
        isImportant = intent.getBooleanExtra("EDIT_TASK_IMPORTANT", false)
        val dueDate = intent.getLongExtra("EDIT_TASK_DUE_DATE", 0L)
        
        taskNameEditText.setText(taskName)
        
        if (dueDate > 0) {
            selectedDueDate = dueDate
            calendar.timeInMillis = dueDate
            updateDueDateInView()
        }
        
        addTaskButton.text = "Update Task"
    }

    private fun updateUI() {
        // Update Urgency Buttons
        updateSelectableButton(btnNotUrgent, !isUrgent)
        updateSelectableButton(btnUrgent, isUrgent, if (isUrgent) "#FF6B6B" else "#CFCFCF")

        // Update Importance Buttons
        updateSelectableButton(btnNotImportant, !isImportant)
        updateSelectableButton(btnImportant, isImportant, if (isImportant) "#FFA500" else "#CFCFCF")

        // Update Reminder Buttons
        updateSelectableButton(btnReminderNone, selectedReminder == 0)
        updateSelectableButton(btnReminder1Hr, selectedReminder == 1)
        updateSelectableButton(btnReminder1Day, selectedReminder == 2)

        // Update Quadrant Card
        updateQuadrantCard()
    }

    private fun updateSelectableButton(textView: TextView, isSelected: Boolean, activeColor: String? = null) {
        textView.isSelected = isSelected
        if (isSelected) {
            val color = if (activeColor != null) android.graphics.Color.parseColor(activeColor) 
                        else ContextCompat.getColor(this, R.color.accent)
            textView.setTextColor(color)
        } else {
            textView.setTextColor(ContextCompat.getColor(this, R.color.secondary_text))
        }
    }

    private fun updateQuadrantCard() {
        val (title, desc, color, bgColor) = when {
            isUrgent && isImportant -> QuadInfo("Do first", "Urgent and important", "#FF6B6B", "#1A6B3E3E")
            !isUrgent && isImportant -> QuadInfo("Schedule", "Not urgent + important", "#FFA500", "#1A4D3A1A")
            isUrgent && !isImportant -> QuadInfo("Delegate", "Urgent + not important", "#03DAC6", "#1A013A35")
            else -> QuadInfo("Eliminate", "Neither", "#CFCFCF", "#1A333333")
        }

        detectedQuadrantTitle.text = title
        detectedQuadrantDesc.text = desc
        
        val textColor = android.graphics.Color.parseColor(color)
        detectedQuadrantTitle.setTextColor(textColor)
        
        val shape = quadrantDetectedCard.background as android.graphics.drawable.GradientDrawable
        shape.setStroke(1, (textColor and 0x4DFFFFFF) or (textColor and 0x00FFFFFF))
        shape.setColor(android.graphics.Color.parseColor(bgColor))
        
        val iconFrame = (quadrantDetectedCard as android.view.ViewGroup).getChildAt(0)
        val iconText = (iconFrame as android.view.ViewGroup).getChildAt(0) as TextView
        iconText.setTextColor(textColor)
        iconFrame.backgroundTintList = ColorStateList.valueOf((textColor and 0x33FFFFFF) or (textColor and 0x00FFFFFF))
    }

    private data class QuadInfo(val title: String, val desc: String, val color: String, val bgColor: String)

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
        val myFormat = "dd-MM-yyyy"
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

        val resultIntent = Intent()
        if (editingTaskId != -1L) {
            resultIntent.putExtra("EDIT_TASK_ID", editingTaskId)
        }
        resultIntent.putExtra("TASK_NAME", taskName)
        resultIntent.putExtra("IS_URGENT", isUrgent)
        resultIntent.putExtra("IS_IMPORTANT", isImportant)
        resultIntent.putExtra("DUE_DATE", selectedDueDate)
        resultIntent.putExtra("REMINDER_TYPE", selectedReminder)

        // Schedule notification if a reminder is set
        if (selectedReminder > 0 && selectedDueDate > 0) {
            val reminderTime = when (selectedReminder) {
                1 -> selectedDueDate - (60 * 60 * 1000) // 1 hr before
                2 -> selectedDueDate - (24 * 60 * 60 * 1000) // 1 day before
                else -> 0L
            }
            if (reminderTime > System.currentTimeMillis()) {
                NotificationHelper.scheduleReminder(this, System.currentTimeMillis(), taskName, reminderTime)
            }
        }

        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
}
