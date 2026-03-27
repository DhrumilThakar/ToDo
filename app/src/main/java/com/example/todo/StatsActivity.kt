package com.example.todo

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class StatsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

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
        findViewById<LinearLayout>(R.id.remindersBtn).setOnClickListener {
            startActivity(Intent(this, RemindersActivity::class.java))
            finish()
        }
    }
}
