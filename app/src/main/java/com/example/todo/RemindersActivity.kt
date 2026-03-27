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
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startNoAnimActivity(intent)
        }
        findViewById<LinearLayout>(R.id.viewPriorityQuadrantBtn).setOnClickListener {
            startNoAnimActivity(Intent(this, ActivityQuadrent::class.java))
        }
        findViewById<LinearLayout>(R.id.statsBtn).setOnClickListener {
            startNoAnimActivity(Intent(this, StatsActivity::class.java))
        }
    }

    private fun startNoAnimActivity(intent: Intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        startActivity(intent)
        overridePendingTransition(0, 0)
        finish()
    }
}
