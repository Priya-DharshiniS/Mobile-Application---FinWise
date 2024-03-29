package com.example.finwise_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.ImageView
import android.widget.Switch
import android.app.PendingIntent
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale
import android.content.Context


class reminder_log : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var plusButton: ImageButton
    private lateinit var adapter: ReminderAdapter
    private val reminderList = mutableListOf<Reminder>()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var reminderAlarmManager: ReminderAlarmManager
    private lateinit var fragmentContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Assign the context to the fragmentContext variable
        fragmentContext = context
    }


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
        reminderAlarmManager = ReminderAlarmManager(fragmentContext)
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
        Log.d("FetchAndDisp", "I am working")

        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid
        val userName = currentUser?.displayName

        if (userId != null && userName != null) {
            val reminderRef = db.collection("reminder")
                .document(userId)
                .collection(userName)

            reminderRef.get()
                .addOnSuccessListener { result ->
                    reminderList.clear()
                    for (document in result) {
                        val id = document.id
                        val time = document.getString("Time") ?: ""
                        val date = document.getString("Date") ?: ""
                        val description = document.getString("Description") ?: ""
                        val label = document.getString("Label") ?: ""
                        Log.d("reminder_log", "Date: $date, Description: $description, Label: $label")
                        reminderList.add(Reminder(id,time,date, description, label))

                    }
                    Log.d("reminder_log", "Reminder List Size: ${reminderList.size}")

                    adapter.notifyDataSetChanged() // Notify adapter about data change
                }
                .addOnFailureListener { exception ->
                    Log.e("reminder_log", "Error fetching reminders", exception)
                }
        } else {
            Log.e("reminder_log", "User ID or User Name is null")
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
            private val alarmIconImageView: ImageView = itemView.findViewById(R.id.alarmIcon)
            private val alarmTitleTextView: TextView = itemView.findViewById(R.id.alarmTitle)
            private val alarmSwitch: Switch = itemView.findViewById(R.id.alarmSwitch)
            private val Date : TextView = itemView.findViewById(R.id.date)
            private lateinit var alarmPendingIntent: PendingIntent


            fun bind(reminder: Reminder) {
                alarmIconImageView.setImageResource(R.drawable.ic_alarm)
                alarmTitleTextView.text = reminder.label
                Date.text = "Due is on ${reminder.date}"

                alarmSwitch.isChecked = isAlarmSetForReminder(reminder)

                // Set OnClickListener for the switch to toggle alarm state
                alarmSwitch.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        setAlarmForReminder(reminder)
                    } else {
                        cancelAlarmForReminder(reminder)
                    }
                }
            }
            private fun setAlarmForReminder(reminder: Reminder) {
                val sdf = SimpleDateFormat("MMMM dd, yyyy h:mm a", Locale.getDefault())
                val reminderDate = sdf.parse("${reminder.date} ${reminder.time}")
                if (reminderDate != null) {
                    val calendar = Calendar.getInstance()
                    calendar.time = reminderDate

                    // Convert reminder time to milliseconds
                    val reminderTimeMillis = calendar.timeInMillis

                    // Call the setAlarmForReminder function from the ReminderAlarmManager instance
                    reminderAlarmManager.setAlarmForReminder(reminder.id, reminderTimeMillis)

                    // Set the next alarm for the same date next month
                    calendar.add(Calendar.MONTH, 1) // Move to next month
                    val nextMonthReminderTimeMillis = calendar.timeInMillis
                    reminderAlarmManager.setAlarmForNextMonth(reminder.id, nextMonthReminderTimeMillis)
                    alarmSwitch.isChecked = true
                } else {
                    Log.e("ReminderViewHolder", "Failed to parse reminder date and time")
                }
            }


            // Example of calling isAlarmSetForReminder function
            private fun isAlarmSetForReminder(reminder: Reminder): Boolean {
                // Call the isAlarmSetForReminder function from the ReminderAlarmManager instance
                return reminderAlarmManager.isAlarmSetForReminder(reminder.id)
            }

            // Example of calling cancelAlarmForReminder function
            private fun cancelAlarmForReminder(reminder: Reminder) {
                // Call the cancelAlarmForReminder function from the ReminderAlarmManager instance
                reminderAlarmManager.cancelAlarmForReminder(reminder.id)
            }
        }
    }

    data class Reminder(val id: String,val time: String,val date: String, val description: String, val label: String)
}
