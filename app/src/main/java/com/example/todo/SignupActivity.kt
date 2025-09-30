package com.example.todo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class SignupActivity : AppCompatActivity() {

    private lateinit var textFieldFullNameLayout: TextInputLayout
    private lateinit var editTextFullName: TextInputEditText
    private lateinit var textFieldEmailLayoutSignup: TextInputLayout
    private lateinit var editTextEmailSignup: TextInputEditText
    private lateinit var textFieldPasswordLayoutSignup: TextInputLayout
    private lateinit var editTextPasswordSignup: TextInputEditText
    private lateinit var textFieldConfirmPasswordLayout: TextInputLayout
    private lateinit var editTextConfirmPassword: TextInputEditText
    private lateinit var checkboxTerms: CheckBox
    private lateinit var buttonCreateAccount: Button
    private lateinit var textViewLoginLink: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup) // Assuming your XML is activity_signup.xml

        // Initialize Views
        textFieldFullNameLayout = findViewById(R.id.textFieldFullName)
        editTextFullName = findViewById(R.id.editTextFullName)
        textFieldEmailLayoutSignup = findViewById(R.id.textFieldEmailSignup)
        editTextEmailSignup = findViewById(R.id.editTextEmailSignup)
        textFieldPasswordLayoutSignup = findViewById(R.id.textFieldPasswordSignup)
        editTextPasswordSignup = findViewById(R.id.editTextPasswordSignup)
        textFieldConfirmPasswordLayout = findViewById(R.id.textFieldConfirmPassword)
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword)
        checkboxTerms = findViewById(R.id.checkboxTerms)
        buttonCreateAccount = findViewById(R.id.buttonCreateAccount)
        textViewLoginLink = findViewById(R.id.textViewLoginLink)

        setupListeners()
    }

    private fun setupListeners() {
        buttonCreateAccount.setOnClickListener {
            handleSignup()
        }

        textViewLoginLink.setOnClickListener {
            // Navigate back to LoginActivity
            // Consider if you want to clear this activity from the stack
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP // Clears activities on top of LoginActivity
            startActivity(intent)
            finish() // Finish SignupActivity
        }
    }

    private fun handleSignup() {
        val fullName = editTextFullName.text.toString().trim() // Optional field
        val email = editTextEmailSignup.text.toString().trim()
        val password = editTextPasswordSignup.text.toString().trim()
        val confirmPassword = editTextConfirmPassword.text.toString().trim()
        val termsAccepted = checkboxTerms.isChecked

        // --- Basic Validation ---
        var isValid = true

        if (email.isEmpty()) {
            textFieldEmailLayoutSignup.error = "Email cannot be empty"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            textFieldEmailLayoutSignup.error = "Enter a valid email address"
            isValid = false
        } else {
            textFieldEmailLayoutSignup.error = null
        }

        if (password.isEmpty()) {
            textFieldPasswordLayoutSignup.error = "Password cannot be empty"
            isValid = false
        } else if (password.length < 6) { // Example: Minimum password length
            textFieldPasswordLayoutSignup.error = "Password must be at least 6 characters"
            isValid = false
        } else {
            textFieldPasswordLayoutSignup.error = null
        }

        if (confirmPassword.isEmpty()) {
            textFieldConfirmPasswordLayout.error = "Confirm password cannot be empty"
            isValid = false
        } else if (password != confirmPassword) {
            textFieldConfirmPasswordLayout.error = "Passwords do not match"
            isValid = false
        } else {
            textFieldConfirmPasswordLayout.error = null
        }

        if (!termsAccepted) {
            Toast.makeText(this, "Please accept the Terms and Conditions", Toast.LENGTH_SHORT).show()
            // You might want a more prominent error indication for the checkbox
            isValid = false
        }

        if (!isValid) {
            return // Stop if validation fails
        }

        // --- TODO: Implement your user registration logic here ---
        // 1. Show a loading indicator
        // 2. Make an API call to your backend to register the user
        // 3. On success:
        //    - You might automatically log the user in OR
        //    - Navigate to LoginActivity with a success message (e.g., "Registration successful, please login")
        //    - Or, navigate directly to MainActivity if auto-login is implemented.
        // 4. On failure (e.g., email already exists, server error):
        //    - Show an appropriate error message
        //    - Hide loading indicator

        Log.d("SignupActivity", "Name: $fullName, Email: $email, Pwd: $password, Terms: $termsAccepted")
        Toast.makeText(this, "Signup attempt", Toast.LENGTH_SHORT).show()

        // Example: Navigate to LoginActivity after successful signup
        // if (signupSuccessful) {
        //     Toast.makeText(this, "Registration successful! Please log in.", Toast.LENGTH_LONG).show()
        //     val intent = Intent(this, LoginActivity::class.java)
        //     intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        //     startActivity(intent)
        //     finish()
        // }
    }
}
