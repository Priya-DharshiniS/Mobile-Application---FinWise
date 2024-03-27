package com.example.finwise_app


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import java.text.SimpleDateFormat

class Login : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        usernameEditText = findViewById(R.id.username)
        passwordEditText = findViewById(R.id.password)
        loginButton = findViewById(R.id.loginButton)


        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (validateInput(username, password)) {
                loginUser(username, password)
            }
        }
    }

    private fun validateInput(username: String, password: String): Boolean {
        if (username.isEmpty()) {
            usernameEditText.error = "Please enter your username"
            usernameEditText.requestFocus()
            return false
        }

        if (password.isEmpty()) {
            passwordEditText.error = "Please enter your password"
            passwordEditText.requestFocus()
            return false
        }

        return true
    }

    private fun loginUser(username: String, password: String) {
        mAuth.signInWithEmailAndPassword(username, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = mAuth.currentUser
                    // Check if the user is not null
                    if (user != null) {
                        // Display a toast message with the user's name
                        Toast.makeText(this, "Welcome, ${user.displayName}!", Toast.LENGTH_SHORT).show()
                        // Check and create year and month collections
                        checkAndCreateYearAndMonthCollections(user.uid)
                        // Navigate to the expense screen
                        navigateToExpenseScreen()
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(this, "Authentication failed. Please try again.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkAndCreateYearAndMonthCollections(userId: String) {
        val userFinRef = db.collection("users").document(userId)
            .collection("finance")

        // Get the current year
        val currentYear = getCurrentYear()

        // Check if the year collection already exists
        userFinRef.document(currentYear)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (!documentSnapshot.exists()) {
                    // Year collection doesn't exist, create it along with month collections
                    createYearAndMonthCollections(userFinRef, currentYear)
                }
            }
            .addOnFailureListener { exception ->
                // Error checking if year collection exists
                Toast.makeText(
                    this,
                    "Error checking if year collection exists: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
    private fun createYearAndMonthCollections(userFinRef: CollectionReference, currentYear: String) {
        // Create the year collection
        userFinRef.document(currentYear)
            .set(hashMapOf<String, Any>()) // You can set empty data or omit this line if you don't need any fields

        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)

        // Iterate through the next 12 months
        for (i in 0 until 12) {
            // Calculate the month index (0 to 11, wrapping around if necessary)
            val monthIndex = (currentMonth + i) % 12

            // Get the month name
            calendar.set(Calendar.MONTH, monthIndex)
            val monthName = SimpleDateFormat("MMMM", Locale.getDefault()).format(calendar.time)

            // Create the monthly records reference
            val monthlyRecordsRef = userFinRef.document(currentYear)
                .collection(monthName)

            // Example initial data for monthly records
            val initialMonthlyData = hashMapOf(
                "spending" to 0,
                "income" to 0,
                "savings" to 0,
                "budget" to 0
            )

            // Set initial data for month-wise records
            monthlyRecordsRef.document("fin").set(initialMonthlyData)
                .addOnSuccessListener {
                    // Monthly records created successfully
                    Toast.makeText(
                        this,
                        "Monthly records for $monthName initialized successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener { exception ->
                    // Error creating monthly records
                    Toast.makeText(
                        this,
                        "Error initializing monthly records for $monthName: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun getCurrentYear(): String {
        return Calendar.getInstance().get(Calendar.YEAR).toString()
    }

        private fun navigateToExpenseScreen() {
        val intent = Intent(this, Navigation::class.java)
        startActivity(intent)
    }

}
