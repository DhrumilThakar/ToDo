package com.example.todo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.CredentialManager
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

// For Google Sign-In (using Credential Manager - Recommended)
// Add these dependencies to your build.gradle (Module :app)
// implementation("androidx.credentials:credentials:1.3.0-alpha01")
// implementation("androidx.credentials:credentials-play-services-auth:1.3.0-alpha01")
 import androidx.credentials.GetCredentialRequest
 import androidx.credentials.exceptions.GetCredentialException
 import com.google.android.libraries.identity.googleid.GetGoogleIdOption
 import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
 import kotlinx.coroutines.launch
 import androidx.lifecycle.lifecycleScope
//import androidx.privacysandbox.tools.core.generator.build


class LoginActivity : AppCompatActivity() {
    private lateinit var credentialManager: CredentialManager

    private lateinit var textFieldEmailLayout: TextInputLayout
    private lateinit var editTextEmail: TextInputEditText
    private lateinit var textFieldPasswordLayout: TextInputLayout
    private lateinit var editTextPassword: TextInputEditText
    private lateinit var buttonLogin: Button
    private lateinit var textViewForgotPassword: TextView
    private lateinit var buttonSignInWithGoogle: Button
    private lateinit var textViewSignUp: TextView

    // Replace with your Web Client ID from Google Cloud Console for Google Sign-In
     private val GOOGLE_WEB_CLIENT_ID =  "AIzaSyA2yy_xXhytC_pzGnecitaU4LOSKS_HGQA"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login) // Assuming your XML is activity_login.xml

        // Initialize Views
        textFieldEmailLayout = findViewById(R.id.textFieldEmail)
        editTextEmail = findViewById(R.id.editTextEmail)
        textFieldPasswordLayout = findViewById(R.id.textFieldPassword)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonLogin = findViewById(R.id.buttonLogin)
        textViewForgotPassword = findViewById(R.id.textViewForgotPassword)
        buttonSignInWithGoogle = findViewById(R.id.buttonSignInWithGoogle)
        textViewSignUp = findViewById(R.id.textViewSignUp)

        setupListeners()
    }

    private fun setupListeners() {
        buttonLogin.setOnClickListener {
            handleEmailPasswordLogin()
        }

        buttonSignInWithGoogle.setOnClickListener {
            handleGoogleSignIn()
        }

        textViewSignUp.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
            // finish() // Optional: finish login if you don't want it in backstack when signing up
        }

        textViewForgotPassword.setOnClickListener {
            // TODO: Navigate to Forgot Password Activity/Fragment
            Toast.makeText(this, "Forgot Password Clicked", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleEmailPasswordLogin() {
        val email = editTextEmail.text.toString().trim()
        val password = editTextPassword.text.toString().trim()

        // Basic Validation (implement more robust validation)
        if (email.isEmpty()) {
            textFieldEmailLayout.error = "Email cannot be empty"
            return
        } else {
            textFieldEmailLayout.error = null // Clear error
        }

        if (password.isEmpty()) {
            textFieldPasswordLayout.error = "Password cannot be empty"
            return
        } else {
            textFieldPasswordLayout.error = null // Clear error
        }

        // --- TODO: Implement your authentication logic here ---
        // 1. Show a loading indicator
        // 2. Make an API call to your backend to verify credentials
        // 3. On success:
        //    - Save user session/token
        //    - Navigate to MainActivity or your app's home screen
        //    - Example: startActivity(Intent(this, MainActivity::class.java))
        //    - Example: finish() // Close LoginActivity
        // 4. On failure:
        //    - Show an appropriate error message (e.g., "Invalid credentials")
        //    - Hide loading indicator

        Log.d("LoginActivity", "Email: $email, Password: $password")
        Toast.makeText(this, "Login attempt with Email/Password", Toast.LENGTH_SHORT).show()

        // Example: Navigate to MainActivity on successful login
        // if (loginSuccessful) {
        //     val intent = Intent(this, MainActivity::class.java)
        //     intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        //     startActivity(intent)
        //     finish()
        // }
    }

    private fun handleGoogleSignIn() {
        Toast.makeText(this, "Google Sign-In Clicked", Toast.LENGTH_SHORT).show()
        Log.d("LoginActivity", "Attempting Google Sign-In")

        // Example structure for Credential Manager
        lifecycleScope.launch {
            val googleIdOption: com.google.android.libraries.identity.googleid.GetGoogleIdOption =
                com.google.android.libraries.identity.googleid.GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false) // Show all Google accounts
                .setServerClientId(GOOGLE_WEB_CLIENT_ID) // Now it's used!
                // .setAutoSelectEnabled(true) // Optional: attempt auto sign-in
                .build()

            val request: GetCredentialRequest = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            try {
                // Ensure credentialManager is initialized (e.g., in onCreate)
                val result = credentialManager.getCredential(
                    context = this@LoginActivity,
                    request = request
                )
                val credential = result.credential
                if (credential is com.google.android.libraries.identity.googleid.GoogleIdTokenCredential) {
                    val googleIdToken = credential.idToken
                    // TODO: Send this token to your backend for verification and user session creation
                    // For Firebase, you would use this token to sign in with Firebase:
                    // val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    // FirebaseAuth.getInstance().signInWithCredential(firebaseCredential).addOnCompleteListener { task -> ... }

                    Log.d("LoginActivity", "Google ID Token: $googleIdToken")
                    Toast.makeText(this@LoginActivity, "Google Sign-In Success (Token Acquired)", Toast.LENGTH_LONG).show()

                    // Example: Navigate to MainActivity
                    // val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    // intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    // startActivity(intent)
                    // finish()

                } else {
                    Log.e("LoginActivity", "Unexpected credential type: ${credential::class.java.name}")
                    Toast.makeText(this@LoginActivity, "Google Sign-In: Unexpected credential type", Toast.LENGTH_SHORT).show()
                }
            } catch (e: GetCredentialException) { // Catch specific exceptions if needed
                Log.e("LoginActivity", "Google Sign-In failed", e)

                // You might want to handle specific exceptions like:
                // is androidx.credentials.exceptions.NoCredentialException -> // User has no google accounts or cancelled
                // is androidx.credentials.exceptions.domerrors.UserCancelledError -> // User explicitly cancelled
                // ... and others

                Toast.makeText(this@LoginActivity, "Google Sign-In Failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

}
