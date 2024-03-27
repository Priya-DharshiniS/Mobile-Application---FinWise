package com.example.finwise_app

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.core.content.ContextCompat
import android.graphics.PorterDuff
import android.app.DatePickerDialog
import java.util.Calendar
import android.content.Intent
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textfield.TextInputEditText
import android.util.Log
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts



class expense_log : AppCompatActivity() {

    companion object {
        const val REQUEST_CODE = 100 // Define your request code here
    }
    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val selectedCategoryImage = data?.getIntExtra("selectedCategoryImage", R.drawable.coin)
                val selectedCategoryName = data?.getStringExtra("selectedCategoryName")

                // Update UI with the selected category's information
                val selectedCategoryIcon = findViewById<ImageView>(R.id.selectedCategoryIcon)
                val selectedCategoryNameTextView = findViewById<TextView>(R.id.selectedCategoryName)

                selectedCategoryIcon.setImageResource(selectedCategoryImage ?: R.drawable.coin)
                selectedCategoryNameTextView.text = selectedCategoryName ?: ""
            }
        }

    private lateinit var editAmount: EditText
    private lateinit var editDate: EditText
    private lateinit var buttonDone: Button
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var addbtn : Button
    private var selectedImageView: ImageView? = null

    // Initialize selected category as Food by default
    private var selectedCategory = "Food"

    private val db = FirebaseFirestore.getInstance()
    private val expensesCollection = db.collection("Expense")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense_log)

        editAmount = findViewById(R.id.editAmount)
        editDate = findViewById(R.id.editDate)
        buttonDone = findViewById(R.id.buttonDone)
        firebaseAuth = FirebaseAuth.getInstance()
        addbtn = findViewById(R.id.buttonSelectCategory)


        setupDatePicker()

        buttonDone.setOnClickListener {
            // Get values from EditText fields
            val amount = editAmount.text.toString().toDoubleOrNull() ?: return@setOnClickListener
            val date = editDate.text.toString()

            // Get current user UID
            val currentUserUid = firebaseAuth.currentUser?.uid ?: return@setOnClickListener

            val currentUser = firebaseAuth.currentUser
            val userName = currentUser?.displayName ?: ""
            val selectedCategory = findViewById<TextView>(R.id.selectedCategoryName).text.toString()
            // Create a map with the data
            val expenseData = hashMapOf<String, Any>(
                "Category" to selectedCategory, // Use selected category
                "Date" to date,
                "ExpenseAmt" to amount
            )

            // Save data to Firestore
            saveExpenseData(expenseData)
        }


        addbtn.setOnClickListener {
            // Navigate to MainActivity3
            val intent = Intent(this, MainActivity3::class.java)
            startForResult.launch(intent)
        }
    }



    private fun saveExpenseData(expenseData: HashMap<String, Any>) {
        val currentUserUid = firebaseAuth.currentUser?.uid ?: return
        val currentUser = firebaseAuth.currentUser
        val userName = currentUser?.displayName ?: ""

        // Reference to the user's document in the database
        val userExpenseRef = db.collection("Expense").document(currentUserUid).collection(userName).document()

        userExpenseRef.set(expenseData)
            .addOnSuccessListener {
                Log.d("Firestore", "Expense data saved successfully")
                navigateBackToExpenseScreen()
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error adding expense data", e)
                // Handle failure, if needed
            }
    }

    private fun setupDatePicker() {
        Log.d("DatePicker", "Setting up date picker") // Add this line
        editDate.setOnClickListener {
            Log.d("DatePicker", "EditDate clicked") // Add this line
            showDatePickerDialog()
        }
    }


    private fun showDatePickerDialog() {
        Log.d("ExpenseActivity", "showDatePickerDialog called")
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this,
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                // Update the editDate EditText with the selected date
                val selectedDate = "$year-${monthOfYear + 1}-$dayOfMonth"
                editDate.setText(selectedDate)
            }, currentYear, currentMonth, currentDay)

        // Set the maximum date to the last day of the current month
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        datePickerDialog.datePicker.maxDate = calendar.timeInMillis

        // Set the minimum date to the first day of the current month
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        datePickerDialog.datePicker.minDate = calendar.timeInMillis

        datePickerDialog.show()
    }

    private fun navigateBackToExpenseScreen() {
        // Implement your logic to navigate back to the expense screen
        // For example:
        onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val selectedCategoryImage = data?.getIntExtra("selectedCategoryImage", R.drawable.coin) ?: R.drawable.coin
            val selectedCategoryName = data?.getStringExtra("selectedCategoryName") ?: ""

            // Update UI with the selected category's information
            val selectedCategoryIcon = findViewById<ImageView>(R.id.selectedCategoryIcon)
            val selectedCategoryNameTextView = findViewById<TextView>(R.id.selectedCategoryName)

            selectedCategoryIcon.setImageResource(selectedCategoryImage)
            selectedCategoryNameTextView.text = selectedCategoryName
        }
    }
}

