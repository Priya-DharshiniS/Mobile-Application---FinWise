package com.example.finwise_app

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
import com.google.firebase.FirebaseApp




class MainActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var userNameEditText: EditText
    private lateinit var signUpButton: Button
    private lateinit var mAuth: FirebaseAuth
    private lateinit var nameErrorTextView: TextView
    private lateinit var emailErrorTextView: TextView
    private lateinit var passwordErrorTextView: TextView
    private lateinit var alreadyHaveAccountTextView: TextView
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        FirebaseApp.initializeApp(this)

        mAuth = FirebaseAuth.getInstance()

            emailEditText = findViewById(R.id.email)
            passwordEditText = findViewById(R.id.Password)
            userNameEditText = findViewById(R.id.usern)
            signUpButton = findViewById(R.id.Sign)
            nameErrorTextView = findViewById(R.id.nameError)
            emailErrorTextView = findViewById(R.id.emailError)
            passwordErrorTextView = findViewById(R.id.passwordError)
            alreadyHaveAccountTextView = findViewById(R.id.Alreadyuser)
            db = FirebaseFirestore.getInstance()


            signUpButton.setOnClickListener {
                val email = emailEditText.text.toString()
                val password = passwordEditText.text.toString()
                val userName = userNameEditText.text.toString()

                if (validateInput(email, password, userName)) {
                    Log.d("MyActivity", "sign up clicked")

                    // Create a new user with email, password, and name
                    mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                // Sign up success, update UI with the signed-in user's information
                                val user = mAuth.currentUser
                                user?.updateProfile(
                                    UserProfileChangeRequest.Builder()
                                        .setDisplayName(userName)
                                        .build()
                                )
                                    ?.addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            // Optionally, you can navigate to another activity or do other actions here
                                            Toast.makeText(
                                                this,
                                                "Sign up successful!",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                            // Retrieve and display the user's display name
                                            val displayName = user?.displayName
                                            if (displayName != null) {
                                                // Display name is available, you can use it in your app
                                                Toast.makeText(
                                                    this,
                                                    "Welcome, $displayName!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            } else {
                                                // Display name is not set
                                                Toast.makeText(
                                                    this,
                                                    "Display name is not set",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }


                                            // Navigate to the expense screen
                                            val intent = Intent(this, Login::class.java)
                                            startActivity(intent)

                                        }
                                    }
                            } else {
                                val exception = task.exception
                                // Handle the exception (log, display error message, etc.)
                                Log.e("SignupError", "Error creating user", exception)
                                // If sign up fails, display a message to the user.
                                Toast.makeText(
                                    this,
                                    "Sign up failed. Please try again.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }


                        }
                }
            }
            alreadyHaveAccountTextView.setOnClickListener {
                // Open the login activity when the "Already Have an Account?" TextView is clicked
                val intent = Intent(this, Login::class.java)
                startActivity(intent)
            }
        }


    private fun validateInput(email: String, password: String, userName: String): Boolean {
        val passwordPattern = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{6,}$".toRegex()

        var isValid = true

        if (userName.isEmpty()) {
            nameErrorTextView.text = "Please enter your name"
            isValid = false
        } else {
            nameErrorTextView.text = ""
        }

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailErrorTextView.text = "Enter a valid email address"
            isValid = false
        } else {
            emailErrorTextView.text = ""
        }

        if (password.isEmpty() || password.length < 6 || !password.matches(passwordPattern)) {
            passwordErrorTextView.text = "Password must be at least 6 characters long and contain at least one digit, one capital letter, and one special character"
            isValid = false
        } else {
            passwordErrorTextView.text = ""
        }

        return isValid
    }

}
