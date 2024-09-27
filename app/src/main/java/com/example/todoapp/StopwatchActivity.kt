package com.example.todoapp

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import java.util.Locale

class StopwatchActivity : AppCompatActivity() {

    private lateinit var tvTime: TextView
    private lateinit var btnStart: Button
    private lateinit var btnPause: Button
    private lateinit var btnReset: Button

    private var seconds = 0
    private var isRunning = false
    private var wasRunning = false

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stopwatch)

        window.statusBarColor = getColor(R.color.lightblue)

        tvTime = findViewById(R.id.tvTime)
        btnStart = findViewById(R.id.btnStart)
        btnPause = findViewById(R.id.btnPause)
        btnReset = findViewById(R.id.btnReset)

        runTimer()

        btnStart.setOnClickListener {
            isRunning = true
            btnStart.visibility = Button.GONE
            btnPause.visibility = Button.VISIBLE
            btnReset.visibility = Button.VISIBLE
        }

        btnPause.setOnClickListener {
            isRunning = false
            btnStart.visibility = Button.VISIBLE
            btnPause.visibility = Button.GONE
        }

        btnReset.setOnClickListener {
            isRunning = false
            seconds = 0
            btnStart.visibility = Button.VISIBLE
            btnPause.visibility = Button.GONE
            btnReset.visibility = Button.GONE
        }
    }

    private fun runTimer() {
        handler.post(object : Runnable {
            override fun run() {
                val hours = seconds / 3600
                val minutes = (seconds % 3600) / 60
                val secs = seconds % 60
                val time = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, secs)
                tvTime.text = time
                if (isRunning) {
                    seconds++
                }
                handler.postDelayed(this, 1000)
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("seconds", seconds)
        outState.putBoolean("isRunning", isRunning)
        outState.putBoolean("wasRunning", wasRunning)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        seconds = savedInstanceState.getInt("seconds")
        isRunning = savedInstanceState.getBoolean("isRunning")
        wasRunning = savedInstanceState.getBoolean("wasRunning")
    }
}