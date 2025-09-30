package com.example.todo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class ActivityQuadrent : AppCompatActivity() {

    companion object {
        const val NAV_ACTION_KEY = "NAV_ACTION"
        const val ACTION_GO_TO_BAMBOO_TARGET = "GO_TO_BAMBOO_TARGET"
        // const val ACTION_SHOW_QUADRANT = "SHOW_QUADRANT" // Optional explicit action
    }

    // ... (declarations for containers, tasks, etc.)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val navAction = intent.getStringExtra(NAV_ACTION_KEY)

        if (ACTION_GO_TO_BAMBOO_TARGET == navAction) {
            Log.d("ActivityQuadrent", "Redirecting to Bamboo Target Activity immediately.")
            // Replace BambooTargetActivity::class.java with your actual target Activity
            val targetIntent = Intent(this, BambooTargetActivity::class.java)
            startActivity(targetIntent)
            finish() // Finish ActivityQuadrent so it's not in the back stack
            return // Important: return to prevent further execution of onCreate
        }

        // If not redirecting, proceed to load the quadrant layout
        setContentView(R.layout.activity_quadrent)
        Log.d("ActivityQuadrent", "Showing Quadrant layout.")
        setupNavigation()
    }

    private fun setupNavigation() {
        val homeButton = findViewById<ImageButton>(R.id.home)
        // ... (other navigation buttons) ...

        homeButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

    }
}
