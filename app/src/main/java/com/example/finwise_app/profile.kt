package com.example.finwise_app

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*



class profile : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var currentUser: FirebaseUser

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.activity_profile, container, false)
        firestore = FirebaseFirestore.getInstance()
        currentUser = FirebaseAuth.getInstance().currentUser!!

        val hiTextView = view.findViewById<TextView>(R.id.username)
        val addIncomeEditText = view.findViewById<EditText>(R.id.AddIncome)
        val addBudgetEditText = view.findViewById<EditText>(R.id.AddBudget)
        val totalSavingsEditText = view.findViewById<EditText>(R.id.TotalSavings)
        val saveButton = view.findViewById<Button>(R.id.Savebtn)


        hiTextView.text = "Hi ${currentUser.displayName} !!!"
        fetchFinanceData(addIncomeEditText, addBudgetEditText,totalSavingsEditText)

        saveButton.setOnClickListener {
            // Ensure views are not null before accessing them
            val income = addIncomeEditText.text.toString().toDoubleOrNull() ?: 0.0
            val budget = addBudgetEditText.text.toString().toDoubleOrNull() ?: 0.0
            val savings = income - budget
            totalSavingsEditText.setText(savings.toString())
            // Call function to update Firestore
            updateFinanceData(income, budget, savings)
        }

        return view
    }
    private fun fetchFinanceData(
        addIncomeEditText: EditText,
        addBudgetEditText: EditText,
        totalSavingsEditText: EditText // Add totalSavingsEditText as a parameter
    ) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR).toString()
        val month = SimpleDateFormat("MMMM", Locale.getDefault()).format(calendar.time)

        // Reference to the document in Firestore
        val financeDocRef = firestore.collection("users")
            .document(currentUser.uid)
            .collection("finance")
            .document(year)
            .collection(month)
            .document("fin")

        // Fetch finance data from Firestore
        financeDocRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val income = document.getDouble("income") ?: 0.0
                    val budget = document.getDouble("budget") ?: 0.0
                    val spending = document.getDouble("spending") ?: 0.0 // Fetch spending value
                    val savings = document.getDouble("savings") ?: 0.0 // Fetch savings value

                    // Calculate remaining budget
                    val remainingBudget = budget - spending

                    // Display income, budget, and savings
                    addIncomeEditText.setText(income.toString())
                    addBudgetEditText.setText(remainingBudget.toString()) // Set remaining budget
                    totalSavingsEditText.setText(savings.toString()) // Set savings value
                } else {
                    // If the document doesn't exist, set default values to zero
                    addIncomeEditText.setText("0.0")
                    addBudgetEditText.setText("0.0")
                    totalSavingsEditText.setText("0.0") // Set savings to zero
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error fetching finance data", e)
            }
    }

    private fun updateFinanceData(income: Double, budget: Double, savings: Double) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR).toString()
        val month = SimpleDateFormat("MMMM", Locale.getDefault()).format(calendar.time)

        // Reference to the document in Firestore
        val financeDocRef = firestore.collection("users")
            .document(currentUser.uid)
            .collection("finance")
            .document(year)
            .collection(month)
            .document("fin")

        // Fetch spending value from Firestore
        financeDocRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val spending = document.getDouble("spending") ?: 0.0 // Fetch spending value

                    // Calculate remaining budget
                    val remainingBudget = budget - spending

                    // Create data to be updated
                    val financeData = hashMapOf(
                        "income" to income,
                        "budget" to budget,
                        "savings" to savings
                    )

                    // Update Firestore document
                    financeDocRef.set(financeData)
                        .addOnSuccessListener {
                            // Handle success
                            Log.d(TAG, "Finance data updated successfully")
                            // Check if spending exceeds budget
                            if (spending > budget) {
                                // Send notification
                                sendNotification("You have exceeded your budget!")
                            }
                        }
                        .addOnFailureListener { e ->
                            // Handle failure
                            Log.e(TAG, "Error updating finance data", e)
                        }
                } else {
                    // Handle case when document doesn't exist
                    Log.e(TAG, "Finance document does not exist")
                }
            }
            .addOnFailureListener { e ->
                // Handle failure to fetch spending
                Log.e(TAG, "Error fetching spending data", e)
            }
    }

    private fun sendNotification(message: String) {
        // Implement logic to send notification here
        // For example, you can use Firebase Cloud Messaging to send push notifications
        // You would need to implement the necessary code to send notifications to the user
        // This might involve using Firebase Cloud Messaging or another notification service
        // You can refer to Firebase documentation for implementing push notifications
    }

    companion object {
        private const val TAG = "ProfileFragment"
    }

}