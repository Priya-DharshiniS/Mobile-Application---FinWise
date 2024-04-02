package com.example.finwise_app


import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry



class Pie : Fragment() {

    private lateinit var pieChart: PieChart

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_pie, container, false)
        pieChart = view.findViewById(R.id.pieChart)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Define custom colors
        val customColors = mutableListOf<Int>(
            Color.rgb(255, 102, 0),   // Orange
            Color.rgb(0, 128, 0),     // Green
            Color.rgb(102, 102, 255), // Purple
            Color.rgb(255, 0, 0),     // Red
            Color.rgb(0, 0, 255),     // Blue
            Color.rgb(255, 255, 0),   // Yellow
            Color.rgb(255, 0, 255)    // Magenta
        )

        // Data for the pie chart
        val records = ArrayList<PieEntry>()
        records.add(PieEntry(3800f, "Mon"))
        records.add(PieEntry(1400f, "Tue"))
        records.add(PieEntry(1000f, "Wed"))
        records.add(PieEntry(2000f, "Thu"))
        records.add(PieEntry(1200f, "Fri"))
        records.add(PieEntry(1200f, "Sat"))
        records.add(PieEntry(1200f, "Sun"))

        val dataSet = PieDataSet(records, "Day Report")
        dataSet.colors = customColors
        dataSet.valueTextColor = Color.LTGRAY
        dataSet.valueTextSize = 22f

        val pieData = PieData(dataSet)

        pieChart.data = pieData
        pieChart.description.isEnabled = false
        pieChart.description.text = "Quarterly Revenue"
        pieChart.description.textSize = 12f
        pieChart.description.textAlign = android.graphics.Paint.Align.RIGHT
        pieChart.description.textColor = Color.BLACK
        pieChart.description.xOffset = 10f
        pieChart.description.yOffset = 10f
        pieChart.description.textSize = 14f
        pieChart.setDrawEntryLabels(true)
        pieChart.setEntryLabelTextSize(12f)
        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.legend.isEnabled = true
        pieChart.animateXY(1000, 1000)
        pieChart.setHoleColor(android.R.color.transparent)
        pieChart.animate()
    }
}
