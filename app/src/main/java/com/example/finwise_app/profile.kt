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
import androidx.core.app.NotificationManagerCompat
import android.content.Intent
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import java.util.*


class profile : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var currentUser: FirebaseUser
    private val db = FirebaseFirestore.getInstance()
    private lateinit var addIncomeEditText: EditText
    private lateinit var addBudgetEditText: EditText
    private lateinit var totalSavingsEditText: EditText


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


        hiTextView.text = "Hi ${currentUser.displayName}"
        fetchFinanceData(addIncomeEditText, addBudgetEditText, totalSavingsEditText)

        saveButton.setOnClickListener {
            // Ensure views are not null before accessing them
            val income = addIncomeEditText.text.toString().toDoubleOrNull() ?: 0.0
            val budget = addBudgetEditText.text.toString().toDoubleOrNull() ?: 0.0

            val savings = if (budget < income) {
                0.0 // Set savings to zero if budget exceeds income
            } else {
                income - budget // Calculate savings normally if budget does not exceed income
            }
            totalSavingsEditText.setText(savings.toString())
            // Call function to update Firestore
            updateFinanceData(income, budget, savings)
        }
        val editSymbol: ImageView = view.findViewById(R.id.edit_symbol)

        editSymbol.setOnClickListener {
            // Start Profile_Edit activity when the edit_symbol ImageView is clicked
            val intent = Intent(requireContext(), Profile_Edit::class.java)
            startActivityForResult(intent, PROFILE_EDIT_REQUEST_CODE)
        }

//        Call updateSavingsAndResetBudgetAtEndOfMonth if it's the last day of the month
//        val calendar = Calendar.getInstance()
//        if (isLastDayOfMonth(calendar)) {
//            updateSavingsAndResetBudgetAtEndOfMonth()
//        }


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
                            // Show toast message
                            Toast.makeText(
                                requireContext(),
                                "Finance data updated successfully",
                                Toast.LENGTH_SHORT
                            ).show()
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

    private fun isLastDayOfMonth(calendar: Calendar): Boolean {
        val lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val today = calendar.get(Calendar.DAY_OF_MONTH)
        return today == lastDay
    }
//    private fun updateSavingsAndResetBudgetAtEndOfMonth() {
//        val calendar = Calendar.getInstance()
//        val year = calendar.get(Calendar.YEAR).toString()
//        val month = SimpleDateFormat("MMMM", Locale.getDefault()).format(calendar.time)
//
//        // Fetch income, budget, and current savings from UI
//        val income = addIncomeEditText.text.toString().toDoubleOrNull() ?: 0.0
//        val budget = addBudgetEditText.text.toString().toDoubleOrNull() ?: 0.0
//        val currentSavings = totalSavingsEditText.text.toString().toDoubleOrNull() ?: 0.0
//
//        // Calculate savings
//        val savings = income - budget
//
//        // Update savings in Firestore
//        val financeDocRef = firestore.collection("users")
//            .document(currentUser.uid)
//            .collection("finance")
//            .document(year)
//            .collection(month)
//            .document("fin")
//
//        financeDocRef.update("savings", currentSavings + savings)
//            .addOnSuccessListener {
//                Log.d(TAG, "Savings updated successfully for the end of the month")
//                // Update savings field in UI
//                Toast.makeText(requireContext(), "Savings updated successfully for the end of the month", Toast.LENGTH_SHORT).show()
//                // Show toast message
//                totalSavingsEditText.setText((currentSavings + savings).toString())
//            }
//            .addOnFailureListener { e ->
//                Log.e(TAG, "Error updating savings at the end of the month", e)
//            }
//    }

    private fun sendNotification(message: String) {
        val notificationId = 1
        val builder = NotificationCompat.Builder(requireContext(), CHANNEL_ID)
            .setSmallIcon(R.drawable.sharp_add_alert_24) // Set your notification icon
            .setContentTitle("Budget Limit Exceeded ;_; ")
            .setContentText("Dear User, Please me mindful about what you spend")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        // Show the notification
        with(NotificationManagerCompat.from(requireContext())) {
            // notificationId is a unique int for each notification that you must define
            notify(notificationId, builder.build())
        }
    }

    companion object {
        private const val TAG = "ProfileFragment"
        private const val CHANNEL_ID = "100"
        private const val PROFILE_EDIT_REQUEST_CODE=101

    }
}
