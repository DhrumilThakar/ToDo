package com.example.todo

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class BambooTargetActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set a simple layout for demonstration
        val textView = TextView(this).apply {
            text = "This is the Bamboo Target Activity"
            textSize = 24f
            gravity = android.view.Gravity.CENTER
        }
        setContentView(textView)

        // Remember to declare BambooTargetActivity in AndroidManifest.xml
    }
}