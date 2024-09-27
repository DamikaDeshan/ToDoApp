package com.example.todoapp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var tasksArray: JSONArray

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        window.statusBarColor = getColor(R.color.lightblue)

        sharedPreferences = getSharedPreferences("TaskPrefs", MODE_PRIVATE)
        tasksArray = getTasksArray()

        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewTasks)
        recyclerView.layoutManager = LinearLayoutManager(this)

        taskAdapter = TaskAdapter(tasksArray,
            onUpdateClick = { position -> showUpdateTaskDialog(position) },
            onDeleteClick = { position -> deleteTask(position) }
        )
        recyclerView.adapter = taskAdapter

        val addTaskButton: Button = findViewById(R.id.button2)
        addTaskButton.setOnClickListener {
            showAddTaskDialog()
        }

        val btnGoToStopwatch: Button = findViewById(R.id.btnGoToStopwatch)
        btnGoToStopwatch.setOnClickListener {
            val intent = Intent(this@HomeActivity, StopwatchActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showAddTaskDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.addtask, null)
        val taskTitle = dialogView.findViewById<EditText>(R.id.taskTitle)
        val taskDescription = dialogView.findViewById<EditText>(R.id.taskDescription)
        val setReminderButton = dialogView.findViewById<Button>(R.id.setReminder)

        var reminderDateTime: Long = 0

        val builder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)

        val dialog = builder.create()

        setReminderButton.setOnClickListener {
            showDateTimePicker { selectedDateTimeMillis ->
                reminderDateTime = selectedDateTimeMillis
                val formattedDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(reminderDateTime))
                Toast.makeText(this, "Reminder set for: $formattedDate", Toast.LENGTH_SHORT).show()
            }
        }

        dialogView.findViewById<Button>(R.id.saveTaskButton).setOnClickListener {
            val title = taskTitle.text.toString()
            val description = taskDescription.text.toString()

            if (title.isNotEmpty()) {
                saveTask(title, description, reminderDateTime)
                taskAdapter.setTasks(tasksArray)
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Please enter a task title", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun saveTask(title: String, description: String, reminder: Long) {
        val newTask = JSONObject().apply {
            put("title", title)
            put("description", description)
            put("reminder", reminder)
        }
        tasksArray.put(newTask)
        saveTasks()
    }

    private fun deleteTask(position: Int) {
        tasksArray.remove(position)
        saveTasks()
        taskAdapter.setTasks(tasksArray)
    }

    private fun showUpdateTaskDialog(position: Int) {
        val task = tasksArray.getJSONObject(position)
        val dialogView = LayoutInflater.from(this).inflate(R.layout.addtask, null)
        val taskTitle = dialogView.findViewById<EditText>(R.id.taskTitle)
        val taskDescription = dialogView.findViewById<EditText>(R.id.taskDescription)
        val setReminderButton = dialogView.findViewById<Button>(R.id.setReminder)

        taskTitle.setText(task.getString("title"))
        taskDescription.setText(task.getString("description"))

        var reminderDateTime = task.getLong("reminder")

        setReminderButton.setOnClickListener {
            showDateTimePicker { selectedDateTimeMillis ->
                reminderDateTime = selectedDateTimeMillis
            }
        }

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                task.put("title", taskTitle.text.toString())
                task.put("description", taskDescription.text.toString())
                task.put("reminder", reminderDateTime)
                saveTasks()
                taskAdapter.setTasks(tasksArray)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun getTasksArray(): JSONArray {
        val tasksString = sharedPreferences.getString("tasks", null)
        return if (tasksString.isNullOrEmpty()) {
            JSONArray()
        } else {
            JSONArray(tasksString)
        }
    }

    private fun saveTasks() {
        sharedPreferences.edit().putString("tasks", tasksArray.toString()).apply()
    }

    private fun showDateTimePicker(onDateTimeSelected: (Long) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, dayOfMonth ->
            TimePickerDialog(this, { _, hourOfDay, minute ->
                calendar.set(year, month, dayOfMonth, hourOfDay, minute)
                onDateTimeSelected(calendar.timeInMillis) 
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }
}
