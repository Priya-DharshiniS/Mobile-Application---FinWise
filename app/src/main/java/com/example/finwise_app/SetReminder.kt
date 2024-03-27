package com.example.finwise_app

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class SetReminder : AppCompatActivity() {

    private lateinit var dateEditText: EditText
    private lateinit var timeEditText: EditText
    private lateinit var labelEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var saveButton: Button

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_reminder)

        // Initialize views
        dateEditText = findViewById(R.id.dateEditText)
        timeEditText = findViewById(R.id.timeEditText)
        labelEditText = findViewById(R.id.labelEditText)
        descriptionEditText = findViewById(R.id.descriptionEditText)
        saveButton = findViewById(R.id.setButton)

        // Set OnClickListener for dateEditText to show DatePickerDialog
        dateEditText.setOnClickListener {
            showDatePicker()
        }

        // Set OnClickListener for timeEditText to show TimePickerDialog
        timeEditText.setOnClickListener {
            showTimePicker()
        }

        // Set OnClickListener for saveButton
        saveButton.setOnClickListener {
            saveReminderToFirestore()
        }
    }

    private fun showDatePicker() {
        val currentDate = Calendar.getInstance()
        val year = currentDate.get(Calendar.YEAR)
        val month = currentDate.get(Calendar.MONTH)
        val day = currentDate.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(selectedYear, selectedMonth, selectedDay)
                val sdf = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
                dateEditText.setText(sdf.format(selectedDate.time))
            }, year, month, day
        )

        datePicker.show()
    }

    private fun showTimePicker() {
        val currentTime = Calendar.getInstance()
        val hour = currentTime.get(Calendar.HOUR_OF_DAY)
        val minute = currentTime.get(Calendar.MINUTE)

        val timePicker = TimePickerDialog(this,
            { _, selectedHour, selectedMinute ->
                val selectedTime = Calendar.getInstance()
                selectedTime.set(Calendar.HOUR_OF_DAY, selectedHour)
                selectedTime.set(Calendar.MINUTE, selectedMinute)
                val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                timeEditText.setText(sdf.format(selectedTime.time))
            }, hour, minute, false
        )

        timePicker.show()
    }

    private fun saveReminderToFirestore() {
        val date = dateEditText.text.toString().trim()
        val time = timeEditText.text.toString().trim()
        val label = labelEditText.text.toString().trim()
        val description = descriptionEditText.text.toString().trim()

        // Get the current user's UID and name
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid
        val userName = currentUser?.displayName

        if (userId != null && userName != null) {
            val reminder = hashMapOf(
                "Date" to date,
                "Time" to time,
                "Label" to label,
                "Description" to description
            )

            db.collection("reminder")
                .document(userId)
                .collection(userName)
                .add(reminder)
                .addOnSuccessListener { documentReference ->
                    // Handle success
                    Toast.makeText(this, "Reminder saved successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    // Handle failures
                    Toast.makeText(this, "Failed to save reminder!", Toast.LENGTH_SHORT).show()
                }
        } else {
            // User is not authenticated or user's UID is null
            Toast.makeText(this, "User not authenticated!", Toast.LENGTH_SHORT).show()
        }
    }



}