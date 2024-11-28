package com.example.finwise_app

import android.os.Bundle
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.android.gms.tasks.Task
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import java.util.Calendar
import java.util.Locale
import java.text.SimpleDateFormat
import androidx.appcompat.app.AppCompatActivity


class FetchActivity : AppCompatActivity(){
    private lateinit var pieChart: PieChart
    private val dbroot = FirebaseFirestore.getInstance()
    private val firebaseAuth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fetch)

        pieChart = findViewById(R.id.pieChart)

        fetchData()
    }
    private fun fetchData() {
        val currentUserUid = firebaseAuth.currentUser?.uid ?: return
        val currentUser = firebaseAuth.currentUser
        val userName = currentUser?.displayName ?: ""

        if (currentUserUid != null) {
            dbroot.collection("Expense").document(currentUserUid)
                .collection(userName)
                .get()
                .addOnCompleteListener { task: Task<QuerySnapshot> ->
                    if (task.isSuccessful) {
                        val documentSnapshots = task.result
                        if (documentSnapshots != null && !documentSnapshots.isEmpty) {
                            val expenseData = mutableMapOf<String, Double>()
                            val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
                            for (documentSnapshot in documentSnapshots) {
                                val category = documentSnapshot.getString("Category")
                                val date = documentSnapshot.getString("Date")
                                val expenseAmt = documentSnapshot.getDouble("ExpenseAmt")
                                if (category != null && date != null && expenseAmt != null) {
                                    val dateStr = documentSnapshot.getString("Date")
                                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                    val date = dateFormat.parse(dateStr)
                                    val expenseCalendar = Calendar.getInstance()
                                    expenseCalendar.time = date
                                    val expenseMonth = expenseCalendar.get(Calendar.MONTH) + 1
                                    if (expenseMonth == currentMonth) {
                                        expenseData[category] = (expenseData[category] ?: 0.0) + expenseAmt
                                    }
                                }
                            }
                            drawPieChart(expenseData)
                        } else {
                            pieChart.visibility = View.GONE
                        }
                    } else {
                        Toast.makeText(this, "Failed to fetch data", Toast.LENGTH_LONG).show()
                    }
                }
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_LONG).show()
        }
    }


    private fun drawPieChart(expenseData: Map<String, Double>) {
        val entries = ArrayList<PieEntry>()
        val labels = ArrayList<String>()
        val colors = ArrayList<Int>()

        var totalExpense = 0.0
        val customColors = intArrayOf(
                Color.rgb(255, 102, 0),   // Orange
                Color.rgb(51, 153, 255),  // Blue
                Color.rgb(255, 51, 153),  // Pink
                Color.rgb(102, 255, 51),  // Green
                Color.rgb(255, 153, 51),  // Amber
                Color.rgb(153, 51, 255),  // Purple
                Color.rgb(51, 255, 204),  // Turquoise
                Color.rgb(255, 204, 51),  // Yellow
                Color.rgb(102, 153, 255), // Light Blue
                Color.rgb(255, 51, 102),  // Red
                Color.rgb(51, 255, 102),  // Lime
                Color.rgb(204, 51, 255),  // Deep Purple
                Color.rgb(255, 204, 204), // Pink
                Color.rgb(204, 255, 51),  // Lime
                Color.rgb(255, 102, 153), // Deep Orange
                Color.rgb(51, 204, 255),  // Sky Blue
                Color.rgb(255, 204, 102), // Amber
                Color.rgb(102, 255, 153), // Light Green
                Color.rgb(204, 102, 255), // Indigo
                Color.rgb(255, 153, 204)  // Pink
        )
        // Generate entries, labels, and colors
        var colorIndex = 0
        for ((category, amount) in expenseData) {
            entries.add(PieEntry(amount.toFloat()))
            labels.add(category)
            totalExpense += amount

            // Add color from custom color array
            colors.add(customColors[colorIndex])

            // Increment color index and ensure it stays within the bounds of the custom color array
            colorIndex = (colorIndex + 1) % customColors.size
        }


        val dataSet = PieDataSet(entries, "Expenses")
        dataSet.colors = colors
        dataSet.valueTextSize = 12f


        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter(pieChart))
        pieChart.data = data

        pieChart.description.isEnabled = false
        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.animateY(1400)
        pieChart.legend.isEnabled = true
        pieChart.setEntryLabelTextSize(12f)
        pieChart.setUsePercentValues(true)
        pieChart.centerText = "Total\n${String.format("%.2f", totalExpense)}"
        pieChart.setCenterTextSize(18f)
        pieChart.setDrawEntryLabels(true)

        // Set labels for the entries
        data.setDrawValues(true)
        data.setValueFormatter(PercentFormatter(pieChart))
        data.setValueTextSize(12f)
        data.setValueTextColor(Color.BLACK)

        // Set labels for each entry
        for ((index, entry) in entries.withIndex()) {
            entry.label = labels[index]
        }

        pieChart.invalidate()
    }
}

