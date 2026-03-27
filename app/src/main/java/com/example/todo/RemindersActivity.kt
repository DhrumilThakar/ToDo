package com.example.todo

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class RemindersActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminders)

        setupNavigation()
    }

    private fun setupNavigation() {
        findViewById<LinearLayout>(R.id.homeBtn).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        findViewById<LinearLayout>(R.id.viewPriorityQuadrantBtn).setOnClickListener {
            startActivity(Intent(this, ActivityQuadrent::class.java))
            finish()
        }
        findViewById<LinearLayout>(R.id.statsBtn).setOnClickListener {
            startActivity(Intent(this, StatsActivity::class.java))
            finish()
        }
    }
}
