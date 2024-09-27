package com.example.todoapp

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class TaskAdapter(
    private var tasks: JSONArray,
    private val onUpdateClick: (Int) -> Unit,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    private val handler = Handler(Looper.getMainLooper())
    private val runnable: Runnable = object : Runnable {
        override fun run() {
            notifyDataSetChanged()
            handler.postDelayed(this, 1000)
        }
    }

    init {
        handler.post(runnable)
    }

    class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val taskTitle: TextView = view.findViewById(R.id.taskTitle)
        val taskDescription: TextView = view.findViewById(R.id.taskDescription)
        val taskReminder: TextView = view.findViewById(R.id.taskReminder)
        val updateTask: Button = view.findViewById(R.id.updateTask)
        val deleteTask: Button = view.findViewById(R.id.deleteTask)
    }

    fun setTasks(newTasks: JSONArray) {
        tasks = newTasks
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.task_item, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task: JSONObject = tasks.getJSONObject(position)

        holder.taskTitle.text = task.getString("title")
        holder.taskDescription.text = task.getString("description")


        val reminderTime = task.optLong("reminder", 0L)
        if (reminderTime > 0) {
            val currentTime = System.currentTimeMillis()
            val remainingTimeMillis = reminderTime - currentTime


            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val reminderDate = Date(reminderTime)
            val formattedDate = dateFormat.format(reminderDate)
            val formattedTime = timeFormat.format(reminderDate)

            if (remainingTimeMillis > 0) {
                val hours = TimeUnit.MILLISECONDS.toHours(remainingTimeMillis)
                val minutes = TimeUnit.MILLISECONDS.toMinutes(remainingTimeMillis) % 60
                val seconds = TimeUnit.MILLISECONDS.toSeconds(remainingTimeMillis) % 60

                holder.taskReminder.text = "Reminder on: $formattedDate at $formattedTime\nIn: $hours hrs $minutes mins $seconds sec"
            } else {
                holder.taskReminder.text = "Reminder on: $formattedDate at $formattedTime\nReminder expired"
            }
        } else {
            holder.taskReminder.text = "No reminder set"
        }

        holder.updateTask.setOnClickListener {
            onUpdateClick(position)
        }

        holder.deleteTask.setOnClickListener {
            onDeleteClick(position)
        }
    }

    override fun getItemCount(): Int {
        return tasks.length()
    }

    fun stopUpdates() {
        handler.removeCallbacks(runnable)
    }
}
