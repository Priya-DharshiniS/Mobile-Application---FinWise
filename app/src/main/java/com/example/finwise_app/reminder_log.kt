package com.example.finwise_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.TextView

class reminder_log : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var plusButton: ImageButton
    private lateinit var adapter: ReminderAdapter
    private val reminderList = mutableListOf<Reminder>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("onCreateView","im called")
        val view = inflater.inflate(R.layout.activity_reminder_log, container, false)
        recyclerView = view.findViewById(R.id.recyclerView3)
        plusButton = view.findViewById(R.id.plusButton)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = ReminderAdapter(reminderList)
        recyclerView.adapter = adapter
        setupPlusButton()
        fetchAndDisplayReminders()
        return view
    }

    private fun setupPlusButton() {
        plusButton.setOnClickListener {
            val intent = Intent(requireContext(), SetReminder::class.java)
            startActivity(intent)
        }
    }

    private fun fetchAndDisplayReminders() {
        Log.d("FetchAndDisp","I a m working")
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { user ->
            val userId = user.uid
            val reminderRef = db.collection("reminder").document(userId).collection("uname")
            reminderRef.get()
                .addOnSuccessListener { result ->
                    reminderList.clear()
                    for (document in result) {
                        val date = document.getString("Date") ?: ""
                        val description = document.getString("Description") ?: ""
                        val label = document.getString("Label") ?: ""
                        Log.d("reminder_log", "Date: $date, Description: $description, Label: $label")
                        reminderList.add(Reminder(date, description, label))
                    }
                    Log.d("reminder_log", "Reminder List Size: ${reminderList.size}")

                    val adapter = ReminderAdapter(reminderList)
                    recyclerView.adapter = adapter
                }
                .addOnFailureListener { exception ->
                    Log.e("reminder_log", "Error fetching reminders", exception)
                }
        }
    }

    inner class ReminderAdapter(private val reminderList: List<Reminder>) :
        RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>() {


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_alarm, parent, false)
            return ReminderViewHolder(view)
        }

        override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
            val reminder = reminderList[position]
            holder.bind(reminder)
        }

        override fun getItemCount(): Int = reminderList.size

        inner class ReminderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
            private val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
            private val labelTextView: TextView = itemView.findViewById(R.id.labelTextView)

            fun bind(reminder: Reminder) {
                dateTextView.text = reminder.date
                descriptionTextView.text = reminder.description
                labelTextView.text = reminder.label
            }
        }
    }

    data class Reminder(val date: String, val description: String, val label: String)
}
